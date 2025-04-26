package org.bootcamp.com.repository;

/*
 Spring only creates beans from concrete classes, not interfaces.
 Hence, GithubJpaRepository cannot be annotated with @Repository.
 */

public interface GithubJpaRepository {
}
