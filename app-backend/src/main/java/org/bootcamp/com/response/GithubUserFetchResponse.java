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
    private Integer lastRow;
}
