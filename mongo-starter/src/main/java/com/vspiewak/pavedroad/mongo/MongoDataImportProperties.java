package com.vspiewak.pavedroad.mongo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "platform.mongo.data-import")
public record MongoDataImportProperties(
    @DefaultValue("true") boolean enabled,
    @DefaultValue("src/test/resources/mongo/import") String path) {}
