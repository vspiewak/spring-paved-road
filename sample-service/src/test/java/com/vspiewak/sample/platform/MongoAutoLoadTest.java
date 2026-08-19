package com.vspiewak.sample.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.vspiewak.sample.RunWithTestcontainers;
import org.bson.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.DockerClientFactory;

/** End-to-end : local profile + a real MongoDB container → the auto-load seeds the fixtures. */
@SpringBootTest
@ActiveProfiles("local")
@Import(RunWithTestcontainers.TestConfig.class)
class MongoAutoLoadTest {

  @BeforeAll
  static void dockerRequired() {
    assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker is required");
  }

  @Autowired private MongoTemplate mongoTemplate;

  @Test
  void shouldSeedCollectionsFromImportDirectory() {
    assertThat(mongoTemplate.getCollectionNames()).contains("orders", "products");
    assertThat(mongoTemplate.findAll(Document.class, "orders"))
        .hasSize(2)
        .extracting(d -> d.getString("orderId"))
        .containsExactlyInAnyOrder("1", "2");
    assertThat(mongoTemplate.findAll(Document.class, "products"))
        .singleElement()
        .satisfies(d -> assertThat(d.getString("name")).isEqualTo("Paved Road Deluxe"));
  }
}
