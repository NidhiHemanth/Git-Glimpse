package org.bootcamp.com.utils;

import lombok.extern.slf4j.Slf4j;
import org.bootcamp.com.dto.GithubUserDto;
import org.bootcamp.com.exception.RowMappingException;
import org.bootcamp.com.model.GithubProject;
import org.bootcamp.com.model.GithubUser;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

/*
 Singleton-style utility mapper
 */

@Component
@Slf4j
public class GithubUserMapper {
    public static final GithubUserMapper INSTANCE = new GithubUserMapper();

    // private constructor
    private GithubUserMapper() {}

    public GithubUser toEntity(GithubUserDto githubUserDto) {
        if(null == githubUserDto) {
            return null;
        }

        final GithubUser githubUser = new GithubUser();
        githubUser.setUsername(githubUserDto.getUsername());
        githubUser.setFullName(githubUserDto.getFullName());
        githubUser.setAvatarUrl(githubUserDto.getAvatarUrl());
        githubUser.setProfileUrl(githubUserDto.getProfileUrl());
        githubUser.setFollowers(githubUserDto.getFollowers());
        githubUser.setFollowing(githubUserDto.getFollowing());
        githubUser.setBio(githubUserDto.getBio());
        githubUser.setMostStarredProject(new GithubProject());
        githubUser.getMostStarredProject().setProjectName(githubUserDto.getMostStarredProjectName());
        githubUser.getMostStarredProject().setHtmlUrl(githubUserDto.getProjectUrl());
        return githubUser;
    }

    public GithubUserDto toDto(GithubUser githubUser) {
        if(null == githubUser) {
            return null;
        }

        final GithubUserDto githubUserDto = new GithubUserDto();
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
            githubUser.setUsername(rs.getString("username"));
            githubUser.setFullName(rs.getString("full_name"));
            githubUser.setAvatarUrl(rs.getString("avatar_url"));
            githubUser.setProfileUrl(rs.getString("profile_url"));
            githubUser.setFollowers(rs.getInt("followers"));
            githubUser.setFollowing(rs.getInt("following"));
            githubUser.setBio(rs.getString("bio"));
            githubUser.setMostStarredProject(new GithubProject());
            githubUser.getMostStarredProject().setProjectName(rs.getString("project_name"));
            githubUser.getMostStarredProject().setHtmlUrl(rs.getString("project_url"));
        } catch (SQLException e) {
            log.error("ROW MAPPING EXCEPTION while processing {}", username);
            throw new RowMappingException(e);
        }

        return githubUser;
    }
}
