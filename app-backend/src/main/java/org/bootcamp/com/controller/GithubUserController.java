package org.bootcamp.com.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 @RestController - Automatically serializes return values (Java objects) to JSON / XML.
                   Combines @Controller and @ResponseBody
 @RequestMapping - Maps HTTP requests to handler methods.
 @CrossOrigin - Enables Cross-Origin Resource Sharing (CORS).
                Allows your API to be accessed from a different domain.
                EG: frontend on localhost:3000 calling backend on localhost:8080)
 */

/*
 * 1. fetch users in chunks for pagination
 * 2. add new user after validating using github API
 * 3. filter & sort by column
 * 4. summarize user projects using openAI API
 * 5. delete a user record
 */

@RestController
@RequestMapping("/api/github")
@CrossOrigin
public class GithubUserController {
}
