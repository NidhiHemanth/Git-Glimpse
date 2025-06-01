package org.bootcamp.com.service.impl;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.bootcamp.com.config.ApplicationProperties;
import org.bootcamp.com.constants.Constants;
import org.bootcamp.com.dto.GithubApiRepoDto;
import org.bootcamp.com.dto.GithubApiUserDto;
import org.bootcamp.com.dto.GithubUserDto;
import org.bootcamp.com.model.GithubUser;
import org.bootcamp.com.repository.GithubJdbcRepository;
import org.bootcamp.com.repository.GithubJpaRepository;
import org.bootcamp.com.request.GithubUserFetchRequest;
import org.bootcamp.com.response.GithubUserFetchResponse;
import org.bootcamp.com.service.GithubUserService;
import org.bootcamp.com.utils.GithubUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Service
@Slf4j
public class GithubUserServiceImpl implements GithubUserService {
    private final GithubJpaRepository githubJpaRepository;
    private final GithubJdbcRepository githubJdbcRepository;
    private final RestTemplate restTemplate;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Value("${github.api.token:}")
    private String githubApiToken;

    @Autowired
    public GithubUserServiceImpl(GithubJpaRepository githubJpaRepository,
                                 GithubJdbcRepository githubJdbcRepository,
                                 RestTemplateBuilder builder) {
        this.githubJpaRepository = githubJpaRepository;
        this.githubJdbcRepository = githubJdbcRepository;
        this.restTemplate = builder.build();
    }

    @Override
    public Optional<GithubUserDto> findByUsername(String username) {
        GithubUser githubUser = null;

        if(applicationProperties.isUseSqlQueries()) {
            log.info("\t => PROCESSING findByUsername using JDBC Template");
            try {
                githubUser = githubJdbcRepository.findByUsername(username);
            } catch (DataAccessException e) {
                log.debug("\t\t User not found for {} in findByUsername", username);
            }
        } else {
            log.info("\t => PROCESSING findByUsername using JPA Repository");
            githubUser = githubJpaRepository.findByUsername(username);
        }

        return Optional.ofNullable(githubUser)
                .map(GithubUserMapper.INSTANCE::toDto);
    }

    @Override
    @Transactional
    public int deleteByUsername(String username) {
        final int rowsAffected;

        if(applicationProperties.isUseSqlQueries()) {
            log.info("\t => PROCESSING deleteByUsername using JDBC Template");
            rowsAffected = githubJdbcRepository.deleteByUsername(username);
        } else {
            log.info("\t => PROCESSING deleteByUsername using JPA Repository");
            rowsAffected = githubJpaRepository.deleteByUsername(username);
        }

        if(rowsAffected > 0) {
            log.info("\t\t User {} deleted successfully!", username);
        } else {
            log.debug("\t\t User not found for {} in deleteByUsername", username);
        }

        return rowsAffected;
    }

    @Override
    public GithubUserFetchResponse fetchUsers(GithubUserFetchRequest fetchRequest) {
        if(applicationProperties.isUseSqlQueries()) {
            log.info("\t => PROCESSING fetchUsers using JDBC Template");
            return githubJdbcRepository.fetchUsers(fetchRequest);
        } else {
            log.info("\t => PROCESSING fetchUsers using JPA Repository");
            return githubJpaRepository.fetchUsers(fetchRequest);
        }
    }

    @Override
    public int addUser(String username) {
        // Checking if username is valid & user exists in GitHub
        final GithubUser githubUser = fetchNewGithubUser(username);
        if(null == githubUser) {
            log.debug("Username {} is invalid, and user doesn't exist", username);
            return 0;
        }

        int rowsAdded;

        log.debug("ADDING new GITHUB USER : {}", githubUser);
        try {
            if(applicationProperties.isUseSqlQueries()) {
                log.info("\t => PROCESSING addUser using JDBC Template");
                rowsAdded = githubJdbcRepository.addUser(githubUser);
            } else {
                log.info("\t => PROCESSING addUser using JPA Repository");
                githubJpaRepository.save(githubUser);
                rowsAdded = 1;
            }
        } catch (DataIntegrityViolationException e) {                       // catches DuplicateKeyException
            log.debug("User {} already exists in database", username);
            return -1;
        }

        return rowsAdded;
    }

    private GithubUser fetchNewGithubUser(String username) {
        final GithubApiUserDto githubApiUserDto;
        try {
            githubApiUserDto = getGithubUser(username);
        } catch (HttpClientErrorException e) {
            return null;                            // Username invalid or not found
        }

        final Integer followersCount = getListSize(Constants.GITHUB_FOLLOWERS_URL, username);
        final Integer followingCount = getListSize(Constants.GITHUB_FOLLOWING_URL, username);
        final GithubApiRepoDto githubApiRepoDto = getMostStarredProject(username);
        return GithubUserMapper.INSTANCE.toEntity(githubApiUserDto, githubApiRepoDto, followersCount, followingCount);
    }

    private GithubApiUserDto getGithubUser(String username) {
        final String url = String.format(Constants.GITHUB_USER_URL, username);

        if(githubApiToken.isEmpty()) {
            return restTemplate.getForObject(url, GithubApiUserDto.class);
        }

        return restTemplate.exchange(
            url,
            HttpMethod.GET,
            getAuthorizationHeader(url),
            GithubApiUserDto.class
        ).getBody();
    }

    private int getListSize(String url, String username) {
        final String getUrl = String.format(url, username);
        final ResponseEntity<Object[]> response;

        if(githubApiToken.isEmpty()) {
            response = restTemplate.getForEntity(getUrl, Object[].class);
        } else {
            response = restTemplate.exchange(
                getUrl,
                HttpMethod.GET,
                getAuthorizationHeader(getUrl),
                Object[].class
            );
        }

        return response.getBody() != null ? response.getBody().length : 0;
    }

    private GithubApiRepoDto getMostStarredProject(String username) {
        int page = 1;
        List<GithubApiRepoDto> repoList = new ArrayList<>();

        while(true) {
            final String url = String.format(Constants.GITHUB_REPOS_URL, username, page);
            final ResponseEntity<GithubApiRepoDto[]> response;

            if(githubApiToken.isEmpty()) {
                response = restTemplate.getForEntity(
                    url,
                    GithubApiRepoDto[].class
                );
            } else {
                response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    getAuthorizationHeader(url),
                    GithubApiRepoDto[].class
                );
            }

            GithubApiRepoDto[] repos = response.getBody();
            if(repos == null || repos.length == 0) {
                break;
            }

            repoList.addAll(Arrays.asList(repos));
            page += 1;
        }

        log.info("User {} has a total of {} projects", username, repoList.size());
        return repoList.stream()
                .max(Comparator.comparingInt(GithubApiRepoDto::stargazers_count))
                .orElse(null);
    }

    private HttpEntity<String> getAuthorizationHeader(String url) {
        log.info("\tAccessing {} Github API with Access Token (Authenticated)", url);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + githubApiToken);
        return new HttpEntity<>(headers);
    }
}
