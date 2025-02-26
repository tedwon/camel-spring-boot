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
package org.apache.camel.component.kafka.integration;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spi.StateRepository;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@CamelSpringBootTest
@SpringBootTest(
        classes = {
                CamelAutoConfiguration.class,
                BaseEmbeddedKafkaTestSupport.DefaulKafkaComponent.class,
                KafkaConsumerRebalanceIT.class,
                OffsetStateRepositoryConfiguration.class,
                KafkaConsumerRebalanceIT.TestConfiguration.class,
        }
)
@DisabledIfSystemProperty(named = "ci.env.name", matches = "github.com", disabledReason = "Disabled on GH Action due to Docker limit")
public class KafkaConsumerRebalanceIT extends BaseEmbeddedKafkaTestSupport {
    private static final String TOPIC = "offset-rebalance";

    @EndpointInject("mock:result")
    private MockEndpoint result;

    @Autowired
    private CountDownLatch messagesLatch;

    @Test
    public void offsetGetStateMustHaveBeenCalledTwice() throws Exception {
        boolean offsetGetStateCalled = messagesLatch.await(30000, TimeUnit.MILLISECONDS);
        // The getState should most likely be called during the partition assignment
        assertTrue(offsetGetStateCalled, "StateRepository.getState should have been called for topic " + TOPIC
                                         + ". Remaining count : " + messagesLatch.getCount());
    }

    @AfterEach
    public void after() {
        // clean all test topics
        kafkaAdminClient.deleteTopics(Collections.singletonList(TOPIC));
    }

    @Configuration
    public class TestConfiguration {
        @Bean
        public RouteBuilder routeBuilder() {
            return new RouteBuilder() {
                @Override
                public void configure() {
                    from("kafka:" + TOPIC + "?groupId=" + TOPIC + "_GROUP" + "&autoCommitIntervalMs=1000"
                            + "&autoOffsetReset=latest" + "&consumersCount=1"
                            + "&offsetRepository=#offset").routeId("consumer-rebalance-route").to("mock:result");
                }
            };
        }
    }

    public static class OffsetStateRepository implements StateRepository<String, String> {
        private static final Logger LOG = LoggerFactory.getLogger(OffsetStateRepository.class);
        CountDownLatch messagesLatch;

        public OffsetStateRepository(CountDownLatch messagesLatch) {
            this.messagesLatch = messagesLatch;
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }

        @Override
        public String getState(String key) {
            LOG.debug("Getting the state for {} from topic {}", key, TOPIC);
            if (key.contains(TOPIC)) {
                LOG.trace("Topic matches, counting down");
                messagesLatch.countDown();
            }

            return "-1";
        }

        @Override
        public void setState(String key, String value) {
        }
    }
}
