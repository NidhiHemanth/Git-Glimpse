package org.bootcamp.com.utils;

import org.bootcamp.com.request.filter.StringColumnFilter;

import java.util.Map;
import java.util.Optional;

import static org.bootcamp.com.utils.GithubUserMapper.FIELD_TO_COLUMN_MAP;

public class StringSqlBuilderUtil {
    public static final StringSqlBuilderUtil INSTANCE = new StringSqlBuilderUtil();

    public static final Map<StringColumnFilter.Operator, String> sqlOperator = Map.of(
            StringColumnFilter.Operator.EQUALS, "=",
            StringColumnFilter.Operator.NOT_EQUALS, "<>",
            StringColumnFilter.Operator.CONTAINS, "LIKE"
    );

    private StringSqlBuilderUtil() {}

    public String buildSql(StringColumnFilter columnFilter) {
        final String columnName = Optional.ofNullable(FIELD_TO_COLUMN_MAP.get(columnFilter.getColumn()))
                                          .orElse(columnFilter.getColumn());
        
        if(columnFilter.getOperator() == StringColumnFilter.Operator.BLANK) {
            return String.format("(%s IS NULL OR %s = '')", columnName, columnName);
        } else if (columnFilter.getOperator() == StringColumnFilter.Operator.NOT_BLANK) {
            return String.format("(%s IS NOT NULL AND %s <> '')", columnName, columnName);
        }

        final String value = columnFilter.getOperator() == StringColumnFilter.Operator.CONTAINS ?
                "%" + columnFilter.getValue() + "%" : columnFilter.getValue();
        return columnName + " " + sqlOperator.get(columnFilter.getOperator()) + " '" + value + "'";
    }
}
