package org.bootcamp.com.repository.jdbc;

import org.bootcamp.com.constants.Constants;
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
import java.util.List;

@Repository
public class GithubJdbcRepositoryImpl implements GithubJdbcRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SqlQueryBuilder sqlQueryBuilder;

    @Autowired
    public GithubJdbcRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate, SqlQueryBuilder sqlQueryBuilder) {
        this.jdbcTemplate = jdbcTemplate;
        this.sqlQueryBuilder = sqlQueryBuilder;
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
        final List<GithubUser> users = jdbcTemplate.query(
                sqlQueryBuilder.buildSqlQuery(fetchRequest),
                (ResultSet rs, int rowNum) -> GithubUserMapper.INSTANCE.fromResultSet(rs, "fetchUsers")
        );
        final Long lastId = users.isEmpty() ? 0L : users.getLast().getId();
        return new GithubUserFetchResponse(users.stream().map(GithubUserMapper.INSTANCE::toDto).toList(), lastId);
    }

    @Override
    public int deleteByUsername(String username) {
        return jdbcTemplate.update(
                "DELETE FROM github_user WHERE username = :username",
                new MapSqlParameterSource("username", username)
        );
    }

    @Override
    public int addUser(GithubUser githubUser) {
        // SimplePropertySqlParameterSource could have been used if GithubUser did not have nested fields
        return jdbcTemplate.update(
                "INSERT INTO github_user (" + addDelimiterToColumns(", ") + ") VALUES (:" + addDelimiterToColumns(", :") + ");",
                new MapSqlParameterSource(GithubUserMapper.INSTANCE.toParameterSource(githubUser))
        );
    }

    private String addDelimiterToColumns(String delimiter) {
        return String.join(delimiter, Constants.TABLE_COLUMNS);
    }
}