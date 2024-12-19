package org.openwes.search.application;

import cn.zhxu.bs.BeanSearcher;
import cn.zhxu.bs.SearchResult;
import com.openwes.search.api.ISearchApi;
import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.utils.JsonUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SearchApiImpl implements ISearchApi {

    private final BeanSearcher beanSearcher;

    @Override
    public <T> List<T> search(Class<T> beanClass, Map<String, Object> requestMap) {
        SearchResult<T> searchResult = beanSearcher.search(beanClass, requestMap);
        return searchResult.getDataList();
    }

    @Override
    public <T> List<T> search(Class<T> beanClass, Object requestObj) {
        Map<String, Object> requsetMap = JsonUtils.string2MapObject(JsonUtils.obj2String(requestObj));
        return search(beanClass, requsetMap);
    }
}
