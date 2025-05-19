package org.bootcamp.com.request;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface InfiniteScrollRequest {
    Integer getStartRow();
    Integer getEndRow();

    @JsonIgnore
    String getTableName();
}
