package com.vspiewak.pavedroad.mongo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

@ExtendWith(MockitoExtension.class)
class MongoDataImporterTest {

  @Mock private MongoTemplate mongoTemplate;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private MongoClient mongoClient;

  @Mock private MongoDataImportProperties importProperties;

  @InjectMocks private MongoDataImporter mongoDataImporter;

  @TempDir Path tempDir;

  @BeforeEach
  void setUp() {
    lenient().when(importProperties.path()).thenReturn(tempDir.toString());
    givenHosts(new ServerAddress("localhost", 27017));
  }

  private void givenHosts(ServerAddress... addresses) {
    lenient()
        .when(mongoClient.getClusterDescription().getClusterSettings().getHosts())
        .thenReturn(List.of(addresses));
  }

  @Test
  void shouldLoadDocumentsFromCollectionDirectory() throws IOException {
    when(mongoTemplate.count(any(Query.class), eq("orders"))).thenReturn(1L);

    var collectionDir = Files.createDirectory(tempDir.resolve("orders"));
    Files.writeString(
        collectionDir.resolve("order1.json"), "{\"orderId\": \"123\", \"amount\": 42}");

    mongoDataImporter.load();

    var captor = ArgumentCaptor.forClass(Document.class);
    verify(mongoTemplate).insert(captor.capture(), eq("orders"));
    assertThat(captor.getValue().getString("orderId")).isEqualTo("123");
    assertThat(captor.getValue().getInteger("amount")).isEqualTo(42);
  }

  @Test
  void shouldLoadMultipleDocumentsFromMultipleCollections() throws IOException {
    when(mongoTemplate.count(any(Query.class), any(String.class))).thenReturn(1L);

    var ordersDir = Files.createDirectory(tempDir.resolve("orders"));
    Files.writeString(ordersDir.resolve("order1.json"), "{\"orderId\": \"1\"}");

    var productsDir = Files.createDirectory(tempDir.resolve("products"));
    Files.writeString(productsDir.resolve("product1.json"), "{\"productId\": \"A\"}");
    Files.writeString(productsDir.resolve("product2.json"), "{\"productId\": \"B\"}");

    mongoDataImporter.load();

    verify(mongoTemplate).insert(any(Document.class), eq("orders"));
    verify(mongoTemplate, times(2)).insert(any(Document.class), eq("products"));
    verify(mongoTemplate).count(any(Query.class), eq("orders"));
    verify(mongoTemplate).count(any(Query.class), eq("products"));
  }

  @Test
  void shouldSkipWhenHostsAreNotLocal() {
    givenHosts(new ServerAddress("prod-cluster.example.mongodb.net", 27017));

    mongoDataImporter.load();

    verify(mongoTemplate, never()).insert(any(Document.class), any(String.class));
  }

  @Test
  void shouldSkipWhenImportPathDoesNotExist() {
    when(importProperties.path()).thenReturn(tempDir.resolve("nonexistent").toString());

    mongoDataImporter.load();

    verify(mongoTemplate, never()).insert(any(Document.class), any(String.class));
  }

  @Test
  void shouldIgnoreFilesAtRootLevel() throws IOException {
    Files.writeString(tempDir.resolve("stray-file.json"), "{\"key\": \"value\"}");

    mongoDataImporter.load();

    verify(mongoTemplate, never()).insert(any(Document.class), any(String.class));
  }

  @Test
  void shouldHandleEmptyCollectionDirectory() throws IOException {
    when(mongoTemplate.count(any(Query.class), eq("empty"))).thenReturn(0L);

    Files.createDirectory(tempDir.resolve("empty"));

    mongoDataImporter.load();

    verify(mongoTemplate, never()).insert(any(Document.class), any(String.class));
    verify(mongoTemplate).count(any(Query.class), eq("empty"));
  }

  @Test
  void shouldNotRegisterBeanWhenDisabled() {
    new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(MongoDataImporter.class))
        .withPropertyValues(
            "platform.mongo.data-import.enabled=false", "spring.profiles.active=local")
        .run(context -> assertThat(context).doesNotHaveBean(MongoDataImporter.class));
  }
}
