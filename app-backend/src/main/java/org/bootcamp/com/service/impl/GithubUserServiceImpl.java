package org.bootcamp.com.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.bootcamp.com.config.ApplicationProperties;
import org.bootcamp.com.dto.GithubUserDto;
import org.bootcamp.com.model.GithubUser;
import org.bootcamp.com.repository.GithubJdbcRepository;
import org.bootcamp.com.repository.GithubJpaRepository;
import org.bootcamp.com.service.GithubUserService;
import org.bootcamp.com.utils.GithubUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class GithubUserServiceImpl implements GithubUserService {
    private final GithubJpaRepository githubJpaRepository;
    private final GithubJdbcRepository githubJdbcRepository;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    public GithubUserServiceImpl(GithubJpaRepository githubJpaRepository, GithubJdbcRepository githubJdbcRepository) {
        this.githubJpaRepository = githubJpaRepository;
        this.githubJdbcRepository = githubJdbcRepository;
    }

    @Override
    public Optional<GithubUserDto> findByUsername(String username) {
        GithubUser githubUser = null;

        if(applicationProperties.isUseSqlQueries()) {
            log.info("\t => PROCESSING using JDBC Template");
            try {
                githubUser = githubJdbcRepository.findByUsername(username);
            } catch (DataAccessException e) {
                log.debug("\t\t User not found for {}", username);
            }
        } else {
            log.info("\t => PROCESSING using JPA Repository");
            githubUser = githubJpaRepository.findByUsername(username);
        }

        return Optional.ofNullable(githubUser)
                .map(GithubUserMapper.INSTANCE::toDto);
    }
}
