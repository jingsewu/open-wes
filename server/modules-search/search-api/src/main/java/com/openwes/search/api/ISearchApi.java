package com.openwes.search.api;

import java.util.List;
import java.util.Map;

public interface ISearchApi {

    <T> List<T> search(Class<T> beanClass, Map<String, Object> requestMap);

    <T> List<T> search(Class<T> beanClass, Object requestMap);

}
