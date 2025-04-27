package org.bootcamp.com.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bootcamp.com.dto.GithubUserDto;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GithubUserFetchResponse {
    private List<GithubUserDto> githubUsers;

    // Data from the database is fetched with IDs sorted in DESC order
    // So the value of lastId will keep decreasing with each subsequent API call
    private Long lastId;
}
