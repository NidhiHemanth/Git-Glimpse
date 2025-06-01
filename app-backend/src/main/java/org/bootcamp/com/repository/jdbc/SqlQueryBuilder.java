package org.bootcamp.com.repository.jdbc;

import lombok.extern.slf4j.Slf4j;
import org.bootcamp.com.request.GithubUserFetchRequest;
import org.bootcamp.com.request.filter.ColumnFilter;
import org.bootcamp.com.request.filter.NumericColumnFilter;
import org.bootcamp.com.request.filter.StringColumnFilter;
import org.bootcamp.com.request.sort.SortColumn;
import org.bootcamp.com.utils.NumericSqlBuilderUtil;
import org.bootcamp.com.utils.StringSqlBuilderUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.bootcamp.com.utils.GithubUserMapper.FIELD_TO_COLUMN_MAP;

@Component
@Slf4j
public class SqlQueryBuilder {
    public String buildSqlQuery(GithubUserFetchRequest fetchRequest) {
        final String query = selectQuery(fetchRequest.getTableName()) +
                             filterQuery(fetchRequest.getColumnFilters()) +
                             sortQuery(fetchRequest.getSortColumns()) +
                             offsetQuery(fetchRequest.getStartRow(), fetchRequest.getEndRow());
        log.debug("SQL query executed : \n{}", query);
        return query;
    }

    private String selectQuery(String tableName) {
        return "SELECT * FROM " + tableName + "\n";
    }

    private String filterQuery(List<ColumnFilter> columnFilters) {
        final String whereStatement = columnFilters.stream()
                .map((columnFilter -> {
                    if(columnFilter instanceof StringColumnFilter) {
                        return StringSqlBuilderUtil.INSTANCE.buildSql((StringColumnFilter) columnFilter);
                    } else if (columnFilter instanceof NumericColumnFilter) {
                        return NumericSqlBuilderUtil.INSTANCE.buildSql((NumericColumnFilter) columnFilter);
                    }
                    return "";
                }))
                .collect(Collectors.joining(" AND "));

        return whereStatement.isEmpty() ? "" : "WHERE " + whereStatement + "\n";
    }

    private String sortQuery(List<SortColumn> sortColumns) {
        final String sortColumnList = sortColumns.stream()
                                        .map(sortColumn -> {
                                            final String columnName = Optional.ofNullable(FIELD_TO_COLUMN_MAP.get(sortColumn.getColumn()))
                                                    .orElse(sortColumn.getColumn());
                                            return columnName + " " + sortColumn.getSortDirection().getSqlValue();
                                        })
                                        .collect(Collectors.joining(", "));

        return sortColumnList.isEmpty() ? "" : String.format("ORDER BY %s \n", sortColumnList);
    }

    private String offsetQuery(Integer start, Integer end) {
        return "OFFSET " + start + " ROWS FETCH NEXT " + (end - start) + " ROWS ONLY;";
    }
}
