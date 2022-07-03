package com.example.springbootelk.controller;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class ElasticSearchStatusController {
    private static final Logger LOG = LoggerFactory.getLogger(ElasticSearchStatusController.class);

    private final ElasticsearchClient client;

    @Autowired
    public ElasticSearchStatusController(ElasticsearchClient client) {
        this.client = client;
    }


    @GetMapping("/elasticsearch/ping")
    public boolean isAlive() throws IOException {
        LOG.info("Entering /elasticsearch/ping");
        LOG.info("Pinging Elasticsearch server");
        BooleanResponse isAlive = client.ping();
        LOG.info("Elasticsearch ping result: {}", isAlive.value());
        return isAlive.value();
    }
}
