package org.openwes.common.imp.domain.service;

import cn.afterturn.easypoi.handler.inter.IExcelDataHandler;
import org.openwes.common.imp.ExcelSymbol;
import org.openwes.common.imp.ImportService;
import org.openwes.common.imp.annotation.Importable;
import org.openwes.common.imp.constants.CommonImportConstants;
import org.openwes.common.imp.domain.model.SymbolCache;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.CommonErrorDescEnum;
import org.reflections.Reflections;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.objenesis.instantiator.util.ClassUtils;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

@Component
public class SymbolCacheFactory implements InitializingBean {

    private static Map<ExcelSymbol, SymbolCache> map = new EnumMap<>(ExcelSymbol.class);

    @Autowired
    private ApplicationContext applicationContext;

    public static SymbolCache get(ExcelSymbol symbol) {
        if (!map.containsKey(symbol)) {
            throw WmsException.throwWmsException(CommonErrorDescEnum.UNKNOWN_SYMBOL, symbol);
        }

        return map.get(symbol);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Reflections reflections = new Reflections(CommonImportConstants.SCANNER_PATH);
        Set<Class<?>> importableParams = reflections.getTypesAnnotatedWith(Importable.class);

        importableParams.forEach(clazz -> {
            Importable annotation = clazz.getAnnotation(Importable.class);
            ExcelSymbol symbol = annotation.symbol();

            Class<? extends IExcelDataHandler> dataHandlerClass = annotation.dataHandler();
            IExcelDataHandler dataHandler = ClassUtils.newInstance(dataHandlerClass);

            Class<? extends ImportService> service = annotation.service();
            ImportService bean = applicationContext.getBean(service);

            map.put(symbol, new SymbolCache(clazz, dataHandler, bean));
        });
    }
}
