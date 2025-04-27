package org.bootcamp.com.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/*
 @Data - generates all the boilerplate code for a POJO
 @Builder - implements the Builder pattern
 @Entity - marks the class as a database table entity
 @Embedded - includes an @Embeddable class inside an @Entity

 In modern applications, Entity classes don't have to be declared Serializable because
    it is managed entirely by Spring Data JPA & Hibernate (JPA provider).
 It is required to explicitly declare the classes Serializable in cases where data is being
    sent between sessions / services / kafka brokers / for caching.

 NOTE: Spring JPA does not support GenerationType.IDENTITY with composite keys.
       Although, JDBC & JDBC Template does support it.
       To avoid any complications, username is declared as UNIQUE instead of making it a composite key.
 */

@Data
@Builder
@Entity
@Table(name = "GITHUB_USER")
@NoArgsConstructor
@AllArgsConstructor
public class GithubUser implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(groups = OnCreate.class)
    @Column(name = "username", unique = true)
    private String username;

    @Version
    @Transient
    @Column
    @Builder.Default
    private long version = -1L;

    @Size(max = 100)
    @Column(name = "full_name")
    private String fullName;

    @Email
    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "bio", length = 500)
    private String bio;

    @Column(name = "followers")
    private Integer followers;

    @Column(name = "following")
    private Integer following;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "projectName", column = @Column(name = "project_name")),
            @AttributeOverride(name = "description", column = @Column(name = "project_description")),
            @AttributeOverride(name = "language", column = @Column(name = "project_language")),
            @AttributeOverride(name = "stargazersCount", column = @Column(name = "project_stargazers")),
            @AttributeOverride(name = "forksCount", column = @Column(name = "project_forks")),
            @AttributeOverride(name = "htmlUrl", column = @Column(name = "project_url"))
    })
    @Column(name = "projects")
    private GithubProject mostStarredProject;

    @Column(name = "profile_url")
    private String profileUrl;

    @Column(name = "avatar_url")
    private String avatarUrl;
}
