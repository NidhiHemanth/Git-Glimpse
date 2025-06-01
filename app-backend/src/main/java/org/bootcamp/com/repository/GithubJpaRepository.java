package org.bootcamp.com.repository;

import org.bootcamp.com.model.GithubUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GithubJpaRepository extends JpaRepository<GithubUser, Long>, GithubCustomJpaRepository {
    GithubUser findByUsername(String username);
    int deleteByUsername(String username);
}
