package org.openwes.search.parameter;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class SearchPageResult {
    private int total;
    private List<Map<String, Object>> items;
}
