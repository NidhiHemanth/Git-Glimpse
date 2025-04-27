package org.bootcamp.com.service;

import org.bootcamp.com.dto.GithubUserDto;

import java.util.Optional;

public interface GithubUserService {
    Optional<GithubUserDto> findByUsername(String username);
    Optional<GithubUserDto> deleteByUsername(String username);
}
