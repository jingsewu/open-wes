package org.openwes.wes.config.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.common.utils.dictionary.IEnum;
import org.openwes.common.utils.http.Response;
import org.openwes.wes.api.config.IDictionaryApi;
import org.openwes.wes.api.config.dto.DictionaryDTO;
import org.openwes.wes.config.domain.entity.Dictionary;
import org.openwes.wes.config.domain.repository.DictionaryRepository;
import org.openwes.wes.config.domain.transfer.DictionaryTransfer;
import org.reflections.Reflections;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Validated
@RestController
@RequestMapping("config/dictionary")
@RequiredArgsConstructor
@Tag(name = "Wms Module Api")
public class DictionaryController {

    private final IDictionaryApi dictionaryApi;
    private final DictionaryRepository dictionaryRepository;
    private final DictionaryTransfer dictionaryTransfer;

    @PostMapping("createOrUpdate")
    public Object createOrUpdate(@RequestBody @Valid DictionaryDTO dictionaryDTO) {
        if (dictionaryDTO.getId() != null && dictionaryDTO.getId() > 0) {
            dictionaryApi.update(dictionaryDTO);
            return Response.success();
        }
        dictionaryApi.save(dictionaryDTO);
        return Response.success();
    }

    @PostMapping("get/{id}")
    public Object getById(@PathVariable Long id) {
        Dictionary dictionary = dictionaryRepository.findById(id);
        return dictionaryTransfer.toDTO(dictionary);
    }

    @PostMapping("getAll")
    public Object getAll() {
        List<Dictionary> dictionaries = dictionaryRepository.getAll();
        List<DictionaryDTO> dictionaryDTOS = dictionaries.stream().map(dictionaryTransfer::toDTO).toList();

        Map<String, List<Map<String, String>>> result = new HashMap<>();
        Map<String, List<DictionaryDTO.DictionaryItem>> codeMap = dictionaryDTOS.stream()
                .collect(Collectors.toMap(DictionaryDTO::getCode, DictionaryDTO::getItems));
        codeMap.forEach((k, v) -> {
            List<Map<String, String>> items = v.stream()
                    .sorted(Comparator.comparingInt(DictionaryDTO.DictionaryItem::getOrder))
                    .map(item -> {
                        Map<String, String> map = new HashMap<>();
                        map.put("label", item.getShowContent());
                        map.put("value", item.getValue());
                        return map;
                    }).toList();
            result.put(k, items);
        });
        return result;
    }

    @PostMapping("refresh")
    public Object refresh() {
        Reflections reflections = new Reflections("org.openwes");
        Set<Class<? extends IEnum>> dictionaryEnums = reflections.getSubTypesOf(IEnum.class);

        List<DictionaryDTO> dictionaryDTOS = dictionaryEnums.stream()
                .map(cClass -> {
                    Object[] enumConstants = cClass.getEnumConstants();
                    if (enumConstants == null) {
                        return null;
                    }
                    AtomicInteger index = new AtomicInteger(0);
                    List<DictionaryDTO.DictionaryItem> items = Arrays.stream(enumConstants).map(enumConstant -> {
                        DictionaryDTO.DictionaryItem item = new DictionaryDTO.DictionaryItem();
                        String value = null;
                        String label = null;
                        try {
                            Method labelM = cClass.getMethod("getLabel");
                            Method valueM = cClass.getMethod("getValue");
                            value = valueM.invoke(enumConstant).toString();
                            label = labelM.invoke(enumConstant).toString();
                        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                            log.error("execute function error:", e);
                        }
                        item.setDefaultItem(index.getAndIncrement() == 0);
                        item.setShowContent(label);
                        item.setValue(value);
                        return item;
                    }).toList();


                    String simpleName = cClass.getSimpleName();
                    DictionaryDTO dictionaryDTO = new DictionaryDTO();
                    dictionaryDTO.setCode(simpleName.substring(0, simpleName.indexOf("Enum")));
                    dictionaryDTO.setName(simpleName);

                    //name
                    Method method = ReflectionUtils.findMethod(cClass, "getName");
                    if (method != null) {
                        try {
                            Object invoke = method.invoke(enumConstants[0]);
                            dictionaryDTO.setName(String.valueOf(invoke));
                        } catch (InvocationTargetException | IllegalAccessException e) {
                            log.warn("method: getName invoke error: ", e);
                        }
                    }

                    dictionaryDTO.setEditable(true);
                    dictionaryDTO.setItems(items);

                    return dictionaryDTO;
                }).filter(Objects::nonNull).toList();
        dictionaryRepository.saveAll(dictionaryTransfer.toDOs(dictionaryDTOS));
        return Response.success();
    }

}
