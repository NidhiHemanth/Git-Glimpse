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
        final String value = columnFilter.getOperator() == StringColumnFilter.Operator.CONTAINS ?
                "%" + columnFilter.getValue() + "%" : columnFilter.getValue();
        return columnFilter.getColumn() + " " + sqlOperator.get(columnFilter.getOperator()) + " '" + value + "'";
    }
}
