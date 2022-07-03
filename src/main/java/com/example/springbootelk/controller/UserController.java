package com.example.springbootelk.controller;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import com.example.springbootelk.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequestMapping("/user")
@RestController
public class UserController {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);
    private final ElasticsearchClient elasticsearchClient;

    @Autowired
    public UserController(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    /**
     * Save new user. ID is generated automatically
     *
     * @param user user request dto
     * @return HTTP response 200 OK
     * @throws IOException if error while indexing
     */
    @PostMapping
    public ResponseEntity<User> saveUser(@RequestBody User user) throws IOException {
        user.setUserId(UUID.randomUUID());
        LOG.info("Attempting to index user: {}", user);

        IndexResponse response = elasticsearchClient
                .index(i -> i
                        .index("users")
                        .id(user.getUserId().toString())
                        .document(user)
                );

        LOG.info("Indexed with version " + response.version());
        return ResponseEntity.ok(user);
    }

    /**
     * Find user by ID
     *
     * @param id user id - UUID
     * @return User
     * @throws IOException if error while searching
     */
    @GetMapping("/search/id/{id}")
    public ResponseEntity<User> findUserById(@PathVariable("id") UUID id) throws IOException {
        LOG.info("Attempting to find user: {}", id);
        GetResponse<User> response = elasticsearchClient
                .get(g -> g
                        .index("users")
                        .id(id.toString()), User.class
                );

        if (response.found()) {
            User user = response.source();
            LOG.info("User found: {}", user);
            return ResponseEntity.ok(user);
        } else {
            LOG.info("User not found");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Find user by firstName
     *
     * @param firstName fist name
     * @param exactMatch indicate whether exact match or contains
     * @return user
     * @throws IOException if error while indexing
     */
    @GetMapping("/search/name/{firstName}")
    public ResponseEntity<List<User>> findUserByName(@PathVariable String firstName,
                                                     @RequestParam("exactMatch") boolean exactMatch) throws IOException {

        SearchResponse<User> response;
        if (exactMatch) {
            response = elasticsearchClient.search(s -> s
                            .index("users")
                            .query(q -> q
                                    .match(t -> t
                                            .field("firstName")
                                            .query(firstName)
                                    )
                            ),
                    User.class
            );
        } else {
            String searchText = String.format("*%s*", firstName);
            response = elasticsearchClient.search(s -> s
                            .index("users")
                            .query(q ->
                                    q.wildcard(builder -> builder
                                            .field("firstName")
                                            .wildcard(searchText))
                            ),
                    User.class
            );
        }


        TotalHits total = response.hits().total();
        if ((total != null ? total.value() : 0) == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        boolean isExactResult = total.relation().equals(TotalHitsRelation.Eq);

        if (isExactResult) {
            LOG.info("There are " + total.value() + " users");
        } else {
            LOG.info("There are more than " + total.value() + " users");
        }

        List<Hit<User>> hits = response.hits().hits();
        List<User> users = new ArrayList<>();
        for (Hit<User> hit : hits) {
            User user = hit.source();
            LOG.info("Found User " + user.getUserId() + ", score " + hit.score());
            users.add(user);
        }
        return ResponseEntity.ok(users);
    }
}
