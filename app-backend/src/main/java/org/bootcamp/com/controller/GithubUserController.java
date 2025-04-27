package org.bootcamp.com.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.bootcamp.com.dto.GithubUserDto;
import org.bootcamp.com.request.GithubUserFetchRequest;
import org.bootcamp.com.response.GithubUserFetchResponse;
import org.bootcamp.com.service.GithubUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/*
 @RestController - Automatically serializes return values (Java objects) to JSON / XML.
                   Combines @Controller and @ResponseBody
 @RequestMapping - Maps HTTP requests to handler methods.
 @CrossOrigin - Enables Cross-Origin Resource Sharing (CORS).
                Allows your API to be accessed from a different domain.
                EG: frontend on localhost:3000 calling backend on localhost:8080)


 In modern applications, Entity classes don't have to be declared Serializable because
 Spring uses Jackson to convert your Java objects into JSON and back when dealing with Requests & Responses.
 */

/*
 * 1. fetch users in chunks for pagination
 * 2. filter & sort by column
 * 3. delete a user record
 * 4. add new user after validating using github API
 * 5. summarize user projects using openAI API
 */

@RestController
@RequestMapping("/api/github")
@CrossOrigin
public class GithubUserController {
    @Autowired
    GithubUserService githubUserService;

    @Operation(summary = "Get User by their Username")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User Found and fetched",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GithubUserDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "User Not Found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @GetMapping("/user/{username}")
    public ResponseEntity<GithubUserDto> findUserByUsername(@PathVariable String username) {
        return githubUserService.findByUsername(username)
                .map(ResponseEntity::ok)                                            // user -> ResponseEntity.ok(user)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete a User by their Username")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User Deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping("/delete/{username}")
    public ResponseEntity<Void> deleteUserByUsername(@PathVariable String username) {
        return new ResponseEntity<>(githubUserService.deleteByUsername(username) > 0 ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Fetch Filtered & Sorted Users for cursor pagination")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User fetched successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GithubUserFetchResponse.class))
            ),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @PostMapping("/fetch")
    public ResponseEntity<GithubUserFetchResponse> fetchUsers(@RequestBody GithubUserFetchRequest fetchRequest) {
        return new ResponseEntity<>(githubUserService.fetchUsers(fetchRequest), HttpStatus.OK);
    }

    @Operation(summary = "Add User into Database using their Username")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User Added"),
            @ApiResponse(responseCode = "404", description = "Invalid Username"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping("/user/add/{username}")
    public ResponseEntity<Void> addUser(@PathVariable String username) {
        return new ResponseEntity<>(githubUserService.addUser(username) > 0 ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }
}
