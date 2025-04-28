package org.bootcamp.com.utils;

import lombok.extern.slf4j.Slf4j;
import org.bootcamp.com.constants.Constants;
import org.bootcamp.com.dto.GithubApiRepoDto;
import org.bootcamp.com.dto.GithubApiUserDto;
import org.bootcamp.com.dto.GithubUserDto;
import org.bootcamp.com.exception.RowMappingException;
import org.bootcamp.com.model.GithubProject;
import org.bootcamp.com.model.GithubUser;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/*
 Singleton-style utility mapper
 */

@Component
@Slf4j
public class GithubUserMapper {
    public static final GithubUserMapper INSTANCE = new GithubUserMapper();

    // private constructor
    private GithubUserMapper() {}

    public GithubUser toEntity(GithubApiUserDto githubApiUserDto, GithubApiRepoDto githubApiRepoDto, Integer followers, Integer following) {
        if(null == githubApiUserDto) {
            return null;
        }

        final GithubUser githubUser = new GithubUser();
        githubUser.setUsername(githubApiUserDto.login());
        githubUser.setFullName(githubApiUserDto.name());
        githubUser.setAvatarUrl(githubApiUserDto.avatar_url());
        githubUser.setProfileUrl(githubApiUserDto.html_url());
        githubUser.setFollowers(followers);
        githubUser.setFollowing(following);
        githubUser.setBio(githubApiUserDto.bio());
        githubUser.setMostStarredProject(new GithubProject());
        githubUser.getMostStarredProject().setProjectName(githubApiRepoDto.name());
        githubUser.getMostStarredProject().setDescription(githubApiRepoDto.description());
        githubUser.getMostStarredProject().setLanguage(githubApiRepoDto.language());
        githubUser.getMostStarredProject().setForksCount(githubApiRepoDto.forks_count());
        githubUser.getMostStarredProject().setHtmlUrl(githubApiRepoDto.html_url());
        githubUser.getMostStarredProject().setStargazersCount(githubApiRepoDto.stargazers_count());
        return githubUser;
    }

    public GithubUserDto toDto(GithubUser githubUser) {
        if(null == githubUser) {
            return null;
        }

        final GithubUserDto githubUserDto = new GithubUserDto();
        githubUserDto.setId(githubUser.getId());
        githubUserDto.setUsername(githubUser.getUsername());
        githubUserDto.setFullName(githubUser.getFullName());
        githubUserDto.setFollowers(githubUser.getFollowers());
        githubUserDto.setFollowing(githubUser.getFollowing());
        githubUserDto.setBio(githubUser.getBio());
        githubUserDto.setMostStarredProjectName(githubUser.getMostStarredProject().getProjectName());
        githubUserDto.setProjectUrl(githubUser.getMostStarredProject().getHtmlUrl());
        githubUserDto.setProfileUrl(githubUser.getProfileUrl());
        githubUserDto.setAvatarUrl(githubUser.getAvatarUrl());
        return githubUserDto;
    }

    public GithubUser fromResultSet(ResultSet rs, String username) {
        final GithubUser githubUser = new GithubUser();

        try {
            githubUser.setId(rs.getLong(Constants.ID));
            githubUser.setUsername(rs.getString(Constants.USERNAME));
            githubUser.setFullName(rs.getString(Constants.FULL_NAME));
            githubUser.setAvatarUrl(rs.getString(Constants.AVATAR_URL));
            githubUser.setProfileUrl(rs.getString(Constants.PROFILE_URL));
            githubUser.setFollowers(rs.getInt(Constants.FOLLOWERS));
            githubUser.setFollowing(rs.getInt(Constants.FOLLOWING));
            githubUser.setBio(rs.getString(Constants.BIO));
            githubUser.setMostStarredProject(new GithubProject());
            githubUser.getMostStarredProject().setProjectName(rs.getString(Constants.PROJECT_NAME));
            githubUser.getMostStarredProject().setHtmlUrl(rs.getString(Constants.PROJECT_URL));
        } catch (SQLException e) {
            log.error("ROW MAPPING EXCEPTION while processing {}", username);
            throw new RowMappingException(e);
        }

        return githubUser;
    }

    public Map<String, ?> toParameterSource(GithubUser githubUser) {
        final GithubProject project = Optional.ofNullable(githubUser.getMostStarredProject()).orElse(new GithubProject());

        // Map.of() can only be used to insert up-to 10 items
        final Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put(Constants.USERNAME, githubUser.getUsername());
        paramsMap.put(Constants.FULL_NAME, githubUser.getFullName());
        paramsMap.put(Constants.AVATAR_URL, githubUser.getAvatarUrl());
        paramsMap.put(Constants.BIO, githubUser.getBio());
        paramsMap.put(Constants.EMAIL, githubUser.getEmail());
        paramsMap.put(Constants.FOLLOWERS, githubUser.getFollowers());
        paramsMap.put(Constants.FOLLOWING, githubUser.getFollowing());
        paramsMap.put(Constants.PROJECT_NAME, project.getProjectName());
        paramsMap.put(Constants.PROJECT_DESCRIPTION, project.getDescription());
        paramsMap.put(Constants.PROJECT_LANGUAGE, project.getLanguage());
        paramsMap.put(Constants.PROJECT_STARGAZERS, project.getStargazersCount());
        paramsMap.put(Constants.PROJECT_FORKS, project.getForksCount());
        paramsMap.put(Constants.PROJECT_URL, project.getHtmlUrl());
        paramsMap.put(Constants.PROFILE_URL, githubUser.getProfileUrl());

        return paramsMap;
    }
}
