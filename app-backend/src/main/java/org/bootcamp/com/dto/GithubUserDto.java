package org.bootcamp.com.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GithubUserDto {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String profileUrl;
    private String bio;
    private String avatarUrl;
    private Integer followers;
    private Integer following;
    private String mostStarredProjectName;
    private String projectUrl;
}
