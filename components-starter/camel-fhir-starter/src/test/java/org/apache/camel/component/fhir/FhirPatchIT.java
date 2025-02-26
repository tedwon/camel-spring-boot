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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ca.uhn.fhir.rest.api.MethodOutcome;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.fhir.api.ExtraParameters;
import org.apache.camel.component.fhir.internal.FhirApiCollection;
import org.apache.camel.component.fhir.internal.FhirPatchApiMethod;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link org.apache.camel.component.fhir.api.FhirPatch} APIs. The class source won't be generated again
 * if the generator MOJO finds it under src/test/java.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@CamelSpringBootTest
@SpringBootTest(
        classes = {
                CamelAutoConfiguration.class,
                FhirPatchIT.class,
                FhirPatchIT.TestConfiguration.class,
                DefaultCamelContext.class,
                FhirServer.class,
        }
)
@DisabledIfSystemProperty(named = "ci.env.name", matches = "github.com", disabledReason = "Disabled on GH Action due to Docker limit")
public class FhirPatchIT extends AbstractFhirTestSupport {

    private static final Logger LOG = LoggerFactory.getLogger(FhirPatchIT.class);
    private static final String PATH_PREFIX = FhirApiCollection.getCollection().getApiName(FhirPatchApiMethod.class).getName();
    private static final String PATCH = "[ { \"op\":\"replace\", \"path\":\"/active\", \"value\":true } ]";

    @Test
    public void testPatchById() throws Exception {
        final Map<String, Object> headers = new HashMap<>();
        // parameter type is String
        headers.put("CamelFhir.patchBody", PATCH);
        // parameter type is org.hl7.fhir.instance.model.api.IIdType
        headers.put("CamelFhir.id", this.patient.getIdElement());
        // parameter type is ca.uhn.fhir.rest.api.PreferReturnEnum
        headers.put("CamelFhir.preferReturn", null);

        MethodOutcome result = requestBodyAndHeaders("direct://PATCH_BY_ID", null, headers);
        assertNotNull(result, "patchById result");
        assertActive(result);
    }

    @Test
    public void testPatchByStringId() throws Exception {
        final Map<String, Object> headers = new HashMap<>();
        // parameter type is String
        headers.put("CamelFhir.patchBody", PATCH);
        // parameter type is String
        headers.put("CamelFhir.stringId", this.patient.getId());
        // parameter type is ca.uhn.fhir.rest.api.PreferReturnEnum
        headers.put("CamelFhir.preferReturn", null);

        MethodOutcome result = requestBodyAndHeaders("direct://PATCH_BY_SID", null, headers);
        assertActive(result);
    }

    @Test
    public void testPatchByStringIdPreferResponseTypes() throws Exception {
        final Map<String, Object> headers = new HashMap<>();
        // parameter type is String
        headers.put("CamelFhir.patchBody", PATCH);
        // parameter type is String
        headers.put("CamelFhir.stringId", this.patient.getId());
        // parameter type is ca.uhn.fhir.rest.api.PreferReturnEnum
        headers.put("CamelFhir.preferReturn", null);

        List<Class<? extends IBaseResource>> preferredResponseTypes = new ArrayList<>();
        preferredResponseTypes.add(Patient.class);
        headers.put(ExtraParameters.PREFER_RESPONSE_TYPES.getHeaderName(), preferredResponseTypes);

        MethodOutcome result = requestBodyAndHeaders("direct://PATCH_BY_SID", null, headers);
        assertActive(result);
    }

    @Test
    @Disabled(value = "https://github.com/jamesagnew/hapi-fhir/issues/955")
    public void testPatchByUrl() throws Exception {
        final Map<String, Object> headers = new HashMap<>();
        // parameter type is String
        headers.put("CamelFhir.patchBody", PATCH);
        // parameter type is String
        headers.put("CamelFhir.url", "Patient?given=Vincent&family=Freeman");
        // parameter type is ca.uhn.fhir.rest.api.PreferReturnEnum
        headers.put("CamelFhir.preferReturn", null);

        MethodOutcome result = requestBodyAndHeaders("direct://PATCH_BY_URL", null, headers);

        assertNotNull(result, "patchByUrl result");
        LOG.debug("patchByUrl: {}", result);
        assertActive(result);
    }

    @Configuration
    public class TestConfiguration {
        @Bean
        public RouteBuilder routeBuilder() {
            return new RouteBuilder() {
                @Override
                public void configure() {
                    // test route for patchById
                    from("direct://PATCH_BY_ID")
                            .to("fhir://" + PATH_PREFIX + "/patchById");

                    // test route for patchBySId
                    from("direct://PATCH_BY_SID")
                            .to("fhir://" + PATH_PREFIX + "/patchById");

                    // test route for patchByUrl
                    from("direct://PATCH_BY_URL")
                            .to("fhir://" + PATH_PREFIX + "/patchByUrl");
                }
            };
        }
    }

    private void assertActive(MethodOutcome result) {
        LOG.debug("result: {}", result);
        IIdType id = result.getId();

        Patient patient = fhirClient.read().resource(Patient.class).withId(id).preferResponseType(Patient.class).execute();
        assertTrue(patient.getActive());
    }
}
