package org.bootcamp.com.constants;

import java.util.List;

public class Constants {
    public static final String TABLE_NAME = "github_user";

    public static final String ID = "id";
    public static final String USERNAME = "username";
    public static final String FULL_NAME = "full_name";
    public static final String BIO = "bio";
    public static final String EMAIL = "email";
    public static final String FOLLOWERS = "followers";
    public static final String FOLLOWING = "following";
    public static final String PROJECT_NAME = "project_name";
    public static final String PROJECT_DESCRIPTION = "project_description";
    public static final String PROJECT_FORKS = "project_forks";
    public static final String PROJECT_URL = "project_url";
    public static final String PROJECT_LANGUAGE = "project_language";
    public static final String PROJECT_STARGAZERS = "project_stargazers";
    public static final String PROFILE_URL = "profile_url";
    public static final String AVATAR_URL = "avatar_url";

    public static final List<String> TABLE_COLUMNS = List.of(USERNAME, FULL_NAME, BIO, EMAIL, FOLLOWERS, FOLLOWING, PROJECT_NAME, PROJECT_DESCRIPTION, PROJECT_LANGUAGE, PROJECT_FORKS, PROFILE_URL, PROJECT_STARGAZERS, PROJECT_URL, AVATAR_URL);

    public static final String GITHUB_USER_URL = "https://api.github.com/users/%s";
    public static final String GITHUB_FOLLOWERS_URL = "https://api.github.com/users/%s/followers";
    public static final String GITHUB_FOLLOWING_URL = "https://api.github.com/users/%s/following";
    public static final String GITHUB_REPOS_URL = "https://api.github.com/users/%s/repos?per_page=100&page=%s";
    public static final String GITHUB_STARRED_REPOS_URL = "https://api.github.com/users/%s/starred";
    public static final String OPEN_AI_API_URL = "https://api.openai.com/v1/chat/completions";

    private Constants() {}
}
