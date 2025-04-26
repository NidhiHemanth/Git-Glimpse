package org.bootcamp.com.utils;

import org.bootcamp.com.dto.GithubUserDto;
import org.bootcamp.com.model.GithubProject;
import org.bootcamp.com.model.GithubUser;
import org.springframework.stereotype.Component;

/*
 Singleton-style utility mapper
 */

@Component
public class GithubUserMapper {
    public static final GithubUserMapper INSTANCE = new GithubUserMapper();

    // private constructor
    private GithubUserMapper() {}

    public GithubUser toEntity(GithubUserDto githubUserDto) {
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
}
