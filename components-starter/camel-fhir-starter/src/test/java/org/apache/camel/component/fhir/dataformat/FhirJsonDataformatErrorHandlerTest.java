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
package org.apache.camel.component.fhir.dataformat;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParserErrorHandler;
import ca.uhn.fhir.parser.LenientErrorHandler;
import ca.uhn.fhir.parser.StrictErrorHandler;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.fhir.FhirJsonDataFormat;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@CamelSpringBootTest
@SpringBootTest(
        classes = {
                CamelAutoConfiguration.class,
                FhirJsonDataformatErrorHandlerTest.class,
                FhirJsonDataformatErrorHandlerTest.TestConfiguration.class,
        }
)
public class FhirJsonDataformatErrorHandlerTest {

    private static final String INPUT
            = "{\"resourceType\":\"Patient\",\"extension\":[ {\"valueDateTime\":\"2011-01-02T11:13:15\"} ]}";

    private MockEndpoint mockEndpoint;
    private final FhirContext fhirContext = FhirContext.forR4();

    @Autowired
    CamelContext camelContext;

    @Autowired
    protected ProducerTemplate template;

    @BeforeEach
    public void setUp() throws Exception {
        mockEndpoint = camelContext.getEndpoint("mock:result", MockEndpoint.class);
        mockEndpoint.reset();
    }

    @Test
    public void unmarshalParserErrorHandler() throws Throwable {
        try {
            template.sendBody("direct:unmarshalErrorHandlerStrict", INPUT);
            fail("Expected a DataFormatException");
        } catch (CamelExecutionException e) {
            assertTrue(e.getCause() instanceof DataFormatException);
        }
    }

    @Test
    public void unmarshalLenientErrorHandler() throws Exception {
        mockEndpoint.expectedMessageCount(1);

        template.sendBody("direct:unmarshalErrorHandlerLenient", INPUT);

        mockEndpoint.assertIsSatisfied();

        Exchange exchange = mockEndpoint.getExchanges().get(0);
        Patient patient = (Patient) exchange.getIn().getBody();
        assertEquals(1, patient.getExtension().size());
        assertEquals(null, patient.getExtension().get(0).getUrl());
        assertEquals("2011-01-02T11:13:15", patient.getExtension().get(0).getValueAsPrimitive().getValueAsString());
    }


    @Configuration
    public class TestConfiguration {
        @Bean
        public RouteBuilder routeBuilder() {
            return new RouteBuilder() {
                @Override
                public void configure() {
                    FhirJsonDataFormat strickErrorHandlerDataformat = getStrictErrorHandlerDataFormat();
                    FhirJsonDataFormat lenientErrorHandlerDataFormat = getLenientErrorHandlerDataFormat();

                    from("direct:unmarshalErrorHandlerStrict")
                            .unmarshal(strickErrorHandlerDataformat)
                            .to("mock:errorIsThrown");

                    from("direct:unmarshalErrorHandlerLenient")
                            .unmarshal(lenientErrorHandlerDataFormat)
                            .to("mock:result");
                }

                private FhirJsonDataFormat getStrictErrorHandlerDataFormat() {
                    FhirJsonDataFormat fhirJsonDataFormat = new FhirJsonDataFormat();
                    fhirJsonDataFormat.setFhirContext(fhirContext);
                    IParserErrorHandler parserErrorHandler = new StrictErrorHandler();
                    fhirJsonDataFormat.setParserErrorHandler(parserErrorHandler);
                    return fhirJsonDataFormat;
                }

                private FhirJsonDataFormat getLenientErrorHandlerDataFormat() {
                    FhirJsonDataFormat fhirJsonDataFormat = new FhirJsonDataFormat();
                    fhirJsonDataFormat.setFhirContext(fhirContext);
                    IParserErrorHandler parserErrorHandler = new LenientErrorHandler();
                    fhirJsonDataFormat.setParserErrorHandler(parserErrorHandler);
                    return fhirJsonDataFormat;
                }
            };
        }
    }
}
