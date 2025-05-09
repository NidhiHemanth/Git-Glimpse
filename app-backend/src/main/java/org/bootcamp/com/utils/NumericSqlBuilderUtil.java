package org.bootcamp.com.utils;

import org.bootcamp.com.request.filter.NumericColumnFilter;

import java.util.Map;

public class NumericSqlBuilderUtil {
    public static final NumericSqlBuilderUtil INSTANCE = new NumericSqlBuilderUtil();

    public static final Map<NumericColumnFilter.Operator, String> sqlOperator = Map.of(
            NumericColumnFilter.Operator.EQUALS, "=",
            NumericColumnFilter.Operator.NOT_EQUALS, "<>",
            NumericColumnFilter.Operator.GREATER_THAN, "<",
            NumericColumnFilter.Operator.LESSER_THAN, ">"
    );

    private NumericSqlBuilderUtil() {}

    public String buildSql(NumericColumnFilter columnFilter) {
        if(columnFilter.getOperator() == NumericColumnFilter.Operator.IN_RANGE) {
            return columnFilter.getColumn() + " BETWEEN " + columnFilter.getValue() + " AND " + columnFilter.getToValue();
        }
        return columnFilter.getColumn() + " " + sqlOperator.get(columnFilter.getOperator()) + " " + columnFilter.getValue();
    }
}
