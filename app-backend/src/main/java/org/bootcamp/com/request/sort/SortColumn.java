package org.bootcamp.com.request.sort;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SortColumn {
    private String column;
    private Direction sortDirection;

    @Getter
    public enum Direction {
        @JsonProperty("ascending")
        ASCENDING("ASC"),
        @JsonProperty("descending")
        DESCENDING("DESC");

        private final String sqlValue;

        Direction(String sqlValue) {
            this.sqlValue = sqlValue;
        }
    }
}
