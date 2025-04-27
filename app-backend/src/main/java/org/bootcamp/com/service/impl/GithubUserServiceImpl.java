package org.bootcamp.com.service.impl;

import org.bootcamp.com.dto.GithubUserDto;
import org.bootcamp.com.model.GithubUser;
import org.bootcamp.com.repository.GithubJpaRepository;
import org.bootcamp.com.service.GithubUserService;
import org.bootcamp.com.utils.GithubUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GithubUserServiceImpl implements GithubUserService {
    private final GithubJpaRepository githubJpaRepository;

    @Autowired
    public GithubUserServiceImpl(GithubJpaRepository githubJpaRepository) {
        this.githubJpaRepository = githubJpaRepository;
    }

    @Override
    public Optional<GithubUserDto> findByUsername(String username) {
        final GithubUser githubUser = githubJpaRepository.findByUsername(username);

        if(null == githubUser)
            return Optional.empty();

        return Optional.of(GithubUserMapper.INSTANCE.toDto(githubUser));
    }
}
