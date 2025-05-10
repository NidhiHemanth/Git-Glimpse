package org.bootcamp.com.repository.jdbc;

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
public class SqlQueryBuilder {
    public String buildSqlQuery(GithubUserFetchRequest fetchRequest) {
        return selectQuery(fetchRequest.getTableName()) +
                filterQuery(fetchRequest.getColumnFilters(), fetchRequest.getLastId()) +
                sortQuery(fetchRequest.getSortColumns()) +
                limitQuery(fetchRequest.getSize());
    }

    private String selectQuery(String tableName) {
        return "SELECT * FROM " + tableName + "\n";
    }

    private String filterQuery(List<ColumnFilter> columnFilters, Long lastId) {
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

        // for first fetch, since frontend doesn't have any lastId yet
        if(lastId == -1) {
            return whereStatement.isEmpty() ? "" : "WHERE " + whereStatement + "\n";
        }

        final String filterByLastId = "WHERE id < " + lastId;
        return whereStatement.isEmpty() ? filterByLastId + "\n" : filterByLastId + " AND " + whereStatement + "\n";
    }

    private String sortQuery(List<SortColumn> sortColumns) {
        final String sortById = "id DESC";
        final String sortColumnList = sortColumns.stream()
                                        .map(sortColumn -> {
                                            final String columnName = Optional.ofNullable(FIELD_TO_COLUMN_MAP.get(sortColumn.getColumn()))
                                                    .orElse(sortColumn.getColumn());
                                            return columnName + " " + sortColumn.getSortDirection().getSqlValue();
                                        })
                                        .collect(Collectors.joining(", "));

        return String.format("ORDER BY %s \n", sortColumnList.isEmpty() ? sortById : sortColumnList);
    }

    private String limitQuery(Integer size) {
        return "LIMIT " + size + ";";
    }
}
