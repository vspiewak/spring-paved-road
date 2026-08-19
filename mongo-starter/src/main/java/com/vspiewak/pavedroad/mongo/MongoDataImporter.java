package com.vspiewak.pavedroad.mongo;

import com.mongodb.client.MongoClient;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

/**
 * Seeds the local MongoDB from {@code <path>/<collection>/*.json} when the {@code local} profile is
 * active. Guarded by a host check — it will never load into a remote cluster.
 */
@Slf4j
@Profile("local")
@AutoConfiguration
@ConditionalOnClass(MongoTemplate.class)
@ConditionalOnProperty(
    prefix = "platform.mongo.data-import",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
@EnableConfigurationProperties(MongoDataImportProperties.class)
public class MongoDataImporter {

  @Autowired private MongoTemplate mongoTemplate;

  @Autowired private MongoClient mongoClient;

  @Autowired private MongoDataImportProperties importProperties;

  @SneakyThrows
  @EventListener(ApplicationReadyEvent.class)
  public void load() {

    var hosts = mongoClient.getClusterDescription().getClusterSettings().getHosts();
    var allLocal =
        !hosts.isEmpty()
            && hosts.stream()
                .allMatch(a -> a.getHost().equals("localhost") || a.getHost().equals("127.0.0.1"));
    if (!allLocal) {
      log.warn("skipping mongodb autoload, hosts aren't local: {}", hosts);
      return;
    }

    var importPath = Path.of(importProperties.path());
    if (!Files.exists(importPath)) {
      log.info("skipping mongodb autoload, nothing at {}", importPath);
      return;
    }

    for (var collectionDir : Files.list(importPath).filter(Files::isDirectory).toList()) {

      var collection = collectionDir.getFileName().toString();

      for (var file : Files.list(collectionDir).filter(Files::isRegularFile).toList()) {
        var doc = Document.parse(Files.readString(file));
        mongoTemplate.insert(doc, collection);
      }

      long count = mongoTemplate.count(new Query(), collection);
      log.info("MongoDB - {} {} loaded", count, collection);
    }
  }
}
