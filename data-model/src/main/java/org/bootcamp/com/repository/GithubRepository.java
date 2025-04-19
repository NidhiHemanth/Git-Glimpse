package org.bootcamp.com.repository;

import org.bootcamp.com.model.GithubUser;
import org.bootcamp.com.model.GithubUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 @Repository - used to declare the class as a DAO
               in context of Spring Data JPA, repositories handle communication b/w application & database
 JpaRepository - it is a Spring Data JPA that provides a set of CRUD operations
               it is commonly used with relational databases
 */

@Repository
public interface GithubRepository extends JpaRepository<GithubUser, GithubUserId> {

}
