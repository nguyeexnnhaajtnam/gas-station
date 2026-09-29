package vn.gasstation.shared.domain;

import java.util.List;

public record PageResult<T>(List<T> items, int page, int size, long totalItems, boolean hasNext) {
    public PageResult { items = List.copyOf(items); }
}

