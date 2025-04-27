package org.bootcamp.com.service;

import org.bootcamp.com.dto.GithubUserDto;
import org.bootcamp.com.request.GithubUserFetchRequest;
import org.bootcamp.com.response.GithubUserFetchResponse;

import java.util.Optional;

public interface GithubUserService {
    Optional<GithubUserDto> findByUsername(String username);
    int deleteByUsername(String username);
    GithubUserFetchResponse fetchUsers(GithubUserFetchRequest fetchRequest);
}
