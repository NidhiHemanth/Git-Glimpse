package org.bootcamp.com.repository.jdbc;

import org.bootcamp.com.model.GithubUser;
import org.bootcamp.com.repository.GithubJdbcRepository;
import org.bootcamp.com.request.GithubUserFetchRequest;
import org.bootcamp.com.response.GithubUserFetchResponse;
import org.bootcamp.com.utils.GithubUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;

@Repository
public class GithubJdbcRepositoryImpl implements GithubJdbcRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public GithubJdbcRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public GithubUser findByUsername(String username) {
        return jdbcTemplate.queryForObject(
                "SELECT * FROM github_user WHERE username = :username",
                new MapSqlParameterSource("username", username),
                (ResultSet rs, int rowNum) -> GithubUserMapper.INSTANCE.fromResultSet(rs, username)
        );
    }

    @Override
    public GithubUserFetchResponse fetchUsers(GithubUserFetchRequest fetchRequest) {
        return null;
    }

    @Override
    public int deleteByUsername(String username) {
        return jdbcTemplate.update(
                "DELETE FROM github_user WHERE username = :username",
                new MapSqlParameterSource("username", username)
        );
    }

    @Override
    public GithubUser addUser(String username) {
        return null;
    }
}
