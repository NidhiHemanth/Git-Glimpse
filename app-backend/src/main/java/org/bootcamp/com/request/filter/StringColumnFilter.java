package org.bootcamp.com.request.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StringColumnFilter extends ColumnFilter {
    private String column;
    private String value;
    private Operator operator;

    public enum Operator {
        @JsonProperty("equal")
        EQUALS,
        @JsonProperty("notEqual")
        NOT_EQUALS,
        @JsonProperty("contains")
        CONTAINS
    }
}
