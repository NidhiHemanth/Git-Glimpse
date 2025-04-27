package org.bootcamp.com.request.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NumericColumnFilter extends ColumnFilter {
    private String column;
    private Integer value;
    private Integer toValue;
    private Operator operator;

    public enum Operator {
        @JsonProperty("equal")
        EQUALS,
        @JsonProperty("notEqual")
        NOT_EQUALS,
        @JsonProperty("lesserThan")
        GREATER_THAN,
        @JsonProperty("greaterThan")
        LESSER_THAN,
        @JsonProperty("toValue")
        IN_RANGE,
    }

    public NumericColumnFilter(String column, Integer value, Operator operator) {
        this(column, value, null, operator);
    }
}
