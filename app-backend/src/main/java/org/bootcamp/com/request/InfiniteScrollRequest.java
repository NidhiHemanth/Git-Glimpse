package org.bootcamp.com.request;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface InfiniteScrollRequest {
    Long getLastId();
    Integer getSize();

    @JsonIgnore
    String getTableName();
}
