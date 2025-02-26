/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.fhir;

import java.util.HashMap;
import java.util.Map;
import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.api.MethodOutcome;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.fhir.api.ExtraParameters;
import org.apache.camel.component.fhir.internal.FhirApiCollection;
import org.apache.camel.component.fhir.internal.FhirDeleteApiMethod;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link org.apache.camel.component.fhir.api.FhirDelete} APIs.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@CamelSpringBootTest
@SpringBootTest(
        classes = {
                CamelAutoConfiguration.class,
                FhirDeleteIT.class,
                FhirDeleteIT.TestConfiguration.class,
                DefaultCamelContext.class,
                FhirServer.class,
        }
)
@DisabledIfSystemProperty(named = "ci.env.name", matches = "github.com", disabledReason = "Disabled on GH Action due to Docker limit")
public class FhirDeleteIT extends AbstractFhirTestSupport {

    private static final Logger LOG = LoggerFactory.getLogger(FhirDeleteIT.class);
    private static final String PATH_PREFIX = FhirApiCollection.getCollection().getApiName(FhirDeleteApiMethod.class).getName();

    @Test
    public void testDeleteResource() throws Exception {
        assertTrue(patientExists());
        // using org.hl7.fhir.instance.model.api.IBaseResource message body for single parameter "resource"
        MethodOutcome result = requestBody("direct://RESOURCE", this.patient);

        LOG.debug("resource: {}", result);
        assertNotNull(result, "resource result");
        assertFalse(patientExists());
    }

    @Test
    public void testDeleteResourceById() throws Exception {
        assertTrue(patientExists());

        // using org.hl7.fhir.instance.model.api.IIdType message body for single parameter "id"
        MethodOutcome result = requestBody("direct://RESOURCE_BY_ID", this.patient.getIdElement());

        LOG.debug("resourceById: {}", result);
        assertNotNull(result, "resourceById result");
        assertFalse(patientExists());
    }

    @Test
    public void testDeleteResourceByStringId() throws Exception {
        assertTrue(patientExists());

        Map<String, Object> headers = new HashMap<>();
        // parameter type is String
        headers.put("CamelFhir.type", "Patient");
        // parameter type is String
        headers.put("CamelFhir.stringId", this.patient.getIdElement().getIdPart());

        MethodOutcome result = requestBodyAndHeaders("direct://RESOURCE_BY_STRING_ID", null, headers);

        LOG.debug("resourceById: {}", result);
        assertNotNull(result, "resourceById result");
        assertFalse(patientExists());
    }

    @Test
    public void testDeleteResourceConditionalByUrl() throws Exception {
        assertTrue(patientExists());

        MethodOutcome result
                = requestBody("direct://RESOURCE_CONDITIONAL_BY_URL", "Patient?given=Vincent&family=Freeman");

        LOG.debug("resourceConditionalByUrl: {}", result);
        assertNotNull(result, "resourceConditionalByUrl result");
        assertFalse(patientExists());
    }

    @Test
    public void testDeleteResourceConditionalByUrlCacheControlDirective() throws Exception {
        assertTrue(patientExists());
        Map<String, Object> headers = new HashMap<>();
        headers.put(ExtraParameters.CACHE_CONTROL_DIRECTIVE.getHeaderName(), new CacheControlDirective().setNoCache(true));

        MethodOutcome result = requestBodyAndHeaders("direct://RESOURCE_CONDITIONAL_BY_URL",
                "Patient?given=Vincent&family=Freeman", headers);

        LOG.debug("resourceConditionalByUrl: {}", result);
        assertNotNull(result, "resourceConditionalByUrl result");
        assertFalse(patientExists());
    }

    @Configuration
    public class TestConfiguration {
        @Bean
        public RouteBuilder routeBuilder() {
            return new RouteBuilder() {
                @Override
                public void configure() {
                    // test route for resource
                    from("direct://RESOURCE")
                            .to("fhir://" + PATH_PREFIX + "/resource?inBody=resource");

                    // test route for resourceById
                    from("direct://RESOURCE_BY_ID")
                            .to("fhir://" + PATH_PREFIX + "/resourceById?inBody=id");

                    // test route for resourceById
                    from("direct://RESOURCE_BY_STRING_ID")
                            .to("fhir://" + PATH_PREFIX + "/resourceById");

                    // test route for resourceConditionalByUrl
                    from("direct://RESOURCE_CONDITIONAL_BY_URL")
                            .to("fhir://" + PATH_PREFIX + "/resourceConditionalByUrl?inBody=url");
                }
            };
        }
    }
}
