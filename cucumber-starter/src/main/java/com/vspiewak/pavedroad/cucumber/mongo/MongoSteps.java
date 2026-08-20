package com.vspiewak.pavedroad.cucumber.mongo;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Generic, service-agnostic MongoDB seeding steps. Requires a {@link MongoTemplate} bean on the
 * test context.
 *
 * <p>Consumers add {@code com.vspiewak.pavedroad.cucumber.mongo} to their Cucumber glue. Seeding
 * steps drop the collection first, so a scenario only sees what it seeds.
 */
@RequiredArgsConstructor
public class MongoSteps {

  private final MongoTemplate mongoTemplate;

  /** Drops the collection and inserts one document per data-table row (all values as strings). */
  @Given("The following documents exist in the {string} collection:")
  public void theFollowingDocumentsExistInCollection(String collection, DataTable dataTable) {
    mongoTemplate.dropCollection(collection);
    for (Map<String, String> row : dataTable.asMaps()) {
      mongoTemplate.insert(new Document(row), collection);
    }
  }
}
