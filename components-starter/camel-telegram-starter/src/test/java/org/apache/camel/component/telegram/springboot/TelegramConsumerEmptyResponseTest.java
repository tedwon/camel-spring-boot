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
package org.apache.camel.component.telegram.springboot;


import java.util.concurrent.TimeUnit;

import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.awaitility.Awaitility;


@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@CamelSpringBootTest
@SpringBootTest(
    classes = {
        CamelAutoConfiguration.class,
        TelegramConsumerEmptyResponseTest.class,
        TelegramConsumerEmptyResponseTest.TestConfiguration.class
    }
)
public class TelegramConsumerEmptyResponseTest extends TelegramTestSupport {

    
    static TelegramMockRoutes mockRoutes;
    
    @EndpointInject("mock:telegram")
    private MockEndpoint endpoint;

    @Test
    public void testBehaviourWithEmptyUpdates() {
        /* First make sure the message containing zero updates was sent by the API */
        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .until(() -> mockRoutes.getMock("getUpdates").getRecordedMessages().size() >= 1);

        endpoint.setResultWaitTime(500L);
        endpoint.expectedMinimumMessageCount(1);

        /* Then make sure that the consumer has sent zero exchanges to the route */
        assertThrows(AssertionError.class, () -> {
            endpoint.assertIsSatisfied();
        });
    }


    // *************************************
    // Config
    // *************************************

    @Configuration
    public class TestConfiguration {
        @Bean
        public RouteBuilder routeBuilder() {
            return new RouteBuilder() {
                @Override
                public void configure() {
                    from("telegram:bots?authorizationToken=mock-token").to("mock:telegram");
                }
            };
        }

    }
    
    @Override
    @Bean
    protected TelegramMockRoutes createMockRoutes() {
        mockRoutes =
            new TelegramMockRoutes(port)
            .addEndpoint(
                    "getUpdates",
                    "GET",
                    String.class,
                    TelegramTestUtil.stringResource("messages/updates-empty.json"));
        return mockRoutes;
    }
}
