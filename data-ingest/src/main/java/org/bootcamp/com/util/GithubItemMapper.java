package org.bootcamp.com.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bootcamp.com.exception.DataValidationException;
import org.bootcamp.com.model.GithubUser;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class GithubItemMapper implements ItemProcessor<GithubUser, GithubUser> {
    @Autowired
    private Validator beanValidator;

    @Override
    public GithubUser process(@NonNull GithubUser githubUserItem) {
        // If ItemProcessor was converting from 2 different Object types, then Mapper logic impl is required.
        // For example, JsonNode -> GithubUser

        // Here we validate the GithubUser java bean
        final Set<ConstraintViolation<GithubUser>> constraintViolationSet = beanValidator.validate(githubUserItem);
        if(!constraintViolationSet.isEmpty()) {
            log.error("** SKIPPING {}", constraintViolationSet.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(",")));
            throw new DataValidationException("GithubUser JSON File bean has invalid parameters");
        }

        return githubUserItem;
    }
}
