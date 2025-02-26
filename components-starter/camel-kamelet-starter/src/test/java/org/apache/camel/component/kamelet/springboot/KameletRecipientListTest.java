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
package org.apache.camel.component.kamelet.springboot;

import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.junit.jupiter.api.Test;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;

@DirtiesContext
@CamelSpringBootTest
@SpringBootTest(
    classes = {
        CamelAutoConfiguration.class,
        KameletRecipientListTest.class,
    }
)

public class KameletRecipientListTest {

    @Autowired
    ProducerTemplate template;

    @EndpointInject("mock:foo")
    MockEndpoint mock1;

    @EndpointInject("mock:bar")
    MockEndpoint mock2;

    @EndpointInject("mock:result")
    MockEndpoint mock;

    @Test
    public void testRecipientList() throws Exception {
        mock1.expectedBodiesReceived("A");
        mock2.expectedBodiesReceived("B");

        template.sendBody("direct:foo", "A");
        template.sendBody("direct:bar", "B");

        mock.assertIsSatisfied();
    }

    // **********************************************
    //
    // test set-up
    //
    // **********************************************

    @Bean
    protected RoutesBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                routeTemplate("broker")
                        .templateParameter("queue")
                        .from("kamelet:source")
                        .recipientList(constant("mock:{{queue}}"));

                from("direct:foo")
                        .kamelet("broker?queue=foo");

                from("direct:bar")
                        .kamelet("broker?queue=bar");
            }
        };
    }
}