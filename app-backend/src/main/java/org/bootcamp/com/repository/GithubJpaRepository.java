package org.bootcamp.com.repository;

/*
 Spring only creates beans from concrete classes, not interfaces.
 Hence, GithubJpaRepository cannot be annotated with @Repository.
 */

import org.bootcamp.com.model.GithubUser;
import org.bootcamp.com.model.GithubUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GithubJpaRepository extends JpaRepository<GithubUser, GithubUserId> {
    GithubUser findByUsername(String username);
    GithubUser deleteByUsername(String username);
}
