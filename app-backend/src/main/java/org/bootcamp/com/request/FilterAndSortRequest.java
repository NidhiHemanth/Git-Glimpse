package org.bootcamp.com.request;

import org.bootcamp.com.request.filter.ColumnFilter;
import org.bootcamp.com.request.sort.SortColumn;

import java.util.List;

public interface FilterAndSortRequest {
    List<ColumnFilter> getColumnFilters();
    List<SortColumn> getSortColumns();
}
