package org.openwes.search.controller;

import cn.zhxu.bs.MapSearcher;
import cn.zhxu.bs.SearchResult;
import cn.zhxu.bs.util.MapUtils;
import com.google.common.collect.Maps;
import org.openwes.search.parameter.SearchPageResult;
import org.openwes.search.parameter.SearchParam;
import org.openwes.search.utils.SearchUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import javassist.CannotCompileException;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestMapping("search")
@RestController
@RequiredArgsConstructor
@Tag(name = "Search Module Api")
public class SearchController {

    private final MapSearcher beanSearcher;

    @PostMapping
    public Object search(HttpServletRequest request, @Validated @RequestBody Map<String, Object> requestMap)
            throws CannotCompileException, ClassNotFoundException {

        SearchParam searchParam = SearchParam.buildSearch(requestMap);

        Map<String, Object> paramMap = MapUtils.flat(request.getParameterMap());
        requestMap.putAll(paramMap);

        SearchResult<Map<String, Object>> search = beanSearcher
                .search(SearchUtils.createClass(searchParam), SearchUtils.handleSpecialParams(requestMap));

        return SearchPageResult.builder().items(search.getDataList())
                .total(search.getTotalCount().intValue()).build();
    }

    @PostMapping("searchSelectResult")
    public Object searchSelectResult(HttpServletRequest request, @Validated @RequestBody SearchParam searchParam)
            throws CannotCompileException, ClassNotFoundException {
        Map<String, Object> requestMap = MapUtils.flat(request.getParameterMap());

        SearchResult<Map<String, Object>> searchResult = beanSearcher
                .search(SearchUtils.createClass(searchParam), SearchUtils.handleSpecialParams(requestMap));
        return Maps.immutableEntry("options", searchResult.getDataList());
    }

    @PostMapping("clearSearchMetaDataCache")
    public void clearSearchMetaDataCache() {
        SearchUtils.clearSearchMetaData();
    }

}

