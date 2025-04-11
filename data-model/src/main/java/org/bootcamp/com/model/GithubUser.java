package org.bootcamp.com.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/*
 @Data - generates all the boilerplate code for a POJO
 @Builder - implements the Builder pattern
 @Entity - marks the class as a database table entity
 @Embedded - includes an @Embeddable class inside an @Entity
 */

@Data
@Builder
@Entity
@Table(name = "GITHUB_USER")
@NoArgsConstructor
@AllArgsConstructor
public class GithubUser {
    @Id
    @NotBlank(groups = OnCreate.class)
    @Column(name = "username")
    private String username;

    @Version
    @Transient
    @Column
    private long version = -1L;

    @NotBlank
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
    private List<GithubProjects> githubProjects;

    @Column(name = "starred_projects")
    private List<GithubProjects> starredProjects;

    @Column(name = "profile_url")
    private String profileUrl;

    @Column(name = "avatar_url")
    private String avatarUrl;
}
