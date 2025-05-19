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

 NOTE: Cursor Pagination does not work well with Sorting of data
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GithubUserFetchRequest implements InfiniteScrollRequest, FilterAndSortRequest {
    // Required parameters for implementing infinite scroll using regular Pagination
    @Builder.Default
    private Integer startRow = 0;
    @Builder.Default
    private Integer endRow = 0;

    // A Column filter can either be a String filter / Numeric filter
    @Builder.Default
    private List<ColumnFilter> columnFilters = new ArrayList<>();

    @Builder.Default
    private List<SortColumn> sortColumns = new ArrayList<>();

    @JsonIgnore
    private String tableName = Constants.TABLE_NAME;
}
