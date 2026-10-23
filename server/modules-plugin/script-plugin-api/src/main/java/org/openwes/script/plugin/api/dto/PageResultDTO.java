package org.openwes.script.plugin.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResultDTO<T> {
    private List<T> items;
    private long total;
    private int page;
    private int pageSize;

    public static <T> PageResultDTO<T> of(List<T> items, long total, int page, int pageSize) {
        return new PageResultDTO<>(items, total, page, pageSize);
    }
}
