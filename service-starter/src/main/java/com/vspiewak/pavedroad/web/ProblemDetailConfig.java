package com.vspiewak.pavedroad.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ProblemDetail;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * The error contract, shipped as a dependency. {@code spring.mvc.problemdetails.enabled} is a
 * platform <em>mandate</em> (it lives in the override layer) because fleet consumers cannot afford
 * a per-service error shape ; this adds the platform's own extension members on top.
 *
 * <p>A service defining its own {@link PlatformProblemDetailAdvice} bean replaces it.
 */
@AutoConfiguration
@ConditionalOnClass({ProblemDetail.class, ResponseBodyAdvice.class})
@ConditionalOnWebApplication(type = Type.SERVLET)
public class ProblemDetailConfig {

  @Bean
  @ConditionalOnMissingBean
  public PlatformProblemDetailAdvice platformProblemDetailAdvice(
      @Value("${spring.application.name:unknown}") String applicationName) {
    return new PlatformProblemDetailAdvice(applicationName);
  }
}
