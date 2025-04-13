package org.bootcamp.com.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/*
 @Embeddable - marks a class whose fields can be embedded into another JPA entity
 */

@Embeddable
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GithubProject implements Serializable {
    @NotBlank
    private String projectName;

    private String description;

    private String language;

    private Integer stargazersCount;

    private Integer forksCount;

    private String htmlUrl;
}
