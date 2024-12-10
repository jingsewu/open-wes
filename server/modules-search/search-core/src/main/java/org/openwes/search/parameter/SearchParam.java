package org.openwes.search.parameter;

import org.openwes.common.utils.utils.JsonUtils;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchParam {

    // identify search
    @NotEmpty
    private String searchIdentity;

    @NotEmpty
    private List<Column> showColumns;

    private SearchObject searchObject;

    @Data
    public static class Column {

        private String dbField;
        @NotEmpty
        private String name;
        private String javaType;

        public String getJavaType() {
            return javaType == null ? "java.lang.String" : javaType;
        }
    }

    @Data
    public static class SearchObject {
        private String dataSource;
        private String tables;
        private String where;
        private String groupBy;
        private String orderBy;
    }

    public static SearchParam buildSearch(Map<String, Object> parameters) {
        if (parameters == null) {
            return SearchParam.builder().build();
        }

        SearchParam searchParam = SearchParam.builder().searchIdentity(parameters.getOrDefault("searchIdentity", "").toString())
                .showColumns(JsonUtils.string2List(JsonUtils.obj2String(parameters.get("showColumns")), Column.class))
                .searchObject(JsonUtils.string2Object(JsonUtils.obj2String(parameters.get("searchObject")), SearchObject.class))
                .build();

        parameters.remove("searchIdentity");
        parameters.remove("showColumns");
        parameters.remove("searchObject");

        return searchParam;
    }

}
