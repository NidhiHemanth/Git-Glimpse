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
        CONTAINS,
        @JsonProperty("blank")
        BLANK,
        @JsonProperty("notBlank")
        NOT_BLANK
    }
}
