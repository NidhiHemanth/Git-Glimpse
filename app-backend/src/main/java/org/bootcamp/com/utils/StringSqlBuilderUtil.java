package org.bootcamp.com.utils;

import org.bootcamp.com.request.filter.StringColumnFilter;

import java.util.Map;

public class StringSqlBuilderUtil {
    public static final StringSqlBuilderUtil INSTANCE = new StringSqlBuilderUtil();

    public static final Map<StringColumnFilter.Operator, String> sqlOperator = Map.of(
            StringColumnFilter.Operator.EQUALS, "=",
            StringColumnFilter.Operator.NOT_EQUALS, "<>",
            StringColumnFilter.Operator.CONTAINS, "LIKE"
    );

    private StringSqlBuilderUtil() {}

    public String buildSql(StringColumnFilter columnFilter) {
        if(columnFilter.getOperator() == StringColumnFilter.Operator.BLANK) {
            return String.format("%s IS NULL OR %s = ''", columnFilter.getColumn(), columnFilter.getColumn());
        } else if (columnFilter.getOperator() == StringColumnFilter.Operator.NOT_BLANK) {
            return String.format("%s IS NOT NULL AND %s <> ''", columnFilter.getColumn(), columnFilter.getColumn());
        }

        final String value = columnFilter.getOperator() == StringColumnFilter.Operator.CONTAINS ?
                "%" + columnFilter.getValue() + "%" : columnFilter.getValue();
        return columnFilter.getColumn() + " " + sqlOperator.get(columnFilter.getOperator()) + " '" + value + "'";
    }
}
