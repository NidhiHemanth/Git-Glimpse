package org.bootcamp.com.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bootcamp.com.constants.Constants;
import org.bootcamp.com.request.filter.ColumnFilter;
import org.bootcamp.com.request.sort.SortColumn;

import java.util.ArrayList;
import java.util.List;

/*
 @Data - includes all the boilerplate code
            @Getter
            @Setter
            @ToString etc...
 @Builder - used here so that Request object can be constructed easily
         either in test environment / by any other microservice
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GithubUserFetchRequest implements InfiniteScrollRequest, FilterAndSortRequest {
    // This helps implement Infinite Scrolling with Cursor Pagination
    // The ID of the last element is retained and is used to fetch next {size} records
    @Builder.Default
    private Long lastId = 0L;

    // The request will return only this size chunk of records
    @Builder.Default
    private Integer size = 0;

    // A Column filter can either be a String filter / Numeric filter
    @Builder.Default
    private List<ColumnFilter> columnFilters = new ArrayList<>();

    @Builder.Default
    private List<SortColumn> sortColumns = new ArrayList<>();

    @JsonIgnore
    private String tableName = Constants.TABLE_NAME;
}
