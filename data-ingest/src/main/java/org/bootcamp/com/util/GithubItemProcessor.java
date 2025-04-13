package org.bootcamp.com.util;

import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import org.bootcamp.com.model.GithubUser;
import org.springframework.batch.item.ItemProcessor;

public class GithubItemProcessor implements ItemProcessor<GithubUser, GithubUser> {
    @Override
    public GithubUser process(@NonNull GithubUser githubUserItem) {
        return githubUserItem;
    }
}
