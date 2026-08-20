package com.vspiewak.sample.platform;

import static org.assertj.core.api.Assertions.assertThat;

import com.mongodb.client.MongoClient;
import com.vspiewak.sample.Containers;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * Proves the mongo-starter appName default end-to-end, server-side : the very connection running
 * the {@code $currentOp} aggregation must identify itself as {@code spring.application.name}.
 */
@SpringBootTest
@Import(Containers.class)
class MongoAppNameIT {

  @Autowired private MongoClient mongoClient;

  @Test
  void shouldIdentifyConnectionsWithTheApplicationName() {
    // when
    var currentOps =
        mongoClient
            .getDatabase("admin")
            .aggregate(
                List.of(
                    new Document(
                        "$currentOp",
                        new Document("allUsers", true).append("idleConnections", true))))
            .into(new ArrayList<>());

    // then
    assertThat(currentOps).extracting(op -> op.getString("appName")).contains("sample-service");
  }
}
