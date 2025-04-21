package org.bootcamp.com.request.filter;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/*
 Polymorphic Deserialization - Jakarta helps us with this using @JsonTypeInfo & @JsonSubTypes
 https://medium.com/@david.truong510/jackson-polymorphic-deserialization-91426e39b96a
 */

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "columnFilterType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = StringColumnFilter.class, name = "stringColumnFilter"),
        @JsonSubTypes.Type(value = NumericColumnFilter.class, name = "numericColumnFilter")
})
public class ColumnFilter {
}
