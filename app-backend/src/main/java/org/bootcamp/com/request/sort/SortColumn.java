package org.bootcamp.com.request.sort;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SortColumn {
    private String column;
    private Direction sortDirection;

    public enum Direction {
        @JsonProperty("ascending")
        ASCENDING,
        @JsonProperty("descending")
        DESCENDING
    }
}
