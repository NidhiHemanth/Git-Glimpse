package org.bootcamp.com.repository;

/*
 Spring only creates beans from concrete classes, not interfaces.
 Hence, GithubJpaRepository cannot be annotated with @Repository.
 */

import org.bootcamp.com.model.GithubUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GithubJpaRepository extends JpaRepository<GithubUser, Long> {
    GithubUser findByUsername(String username);
    int deleteByUsername(String username);
}
