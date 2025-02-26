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
import java.util.List;
import java.util.Properties;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@CamelSpringBootTest
@SpringBootTest(
        classes = {
                CamelAutoConfiguration.class,
                BaseEmbeddedKafkaTestSupport.DefaulKafkaComponent.class,
                KafkaConsumerLastRecordHeaderIT.class,
                KafkaConsumerLastRecordHeaderIT.TestConfiguration.class,
        }
)
@DisabledIfSystemProperty(named = "ci.env.name", matches = "github.com", disabledReason = "Disabled on GH Action due to Docker limit")
public class KafkaConsumerLastRecordHeaderIT extends BaseEmbeddedKafkaTestSupport {
    private static final String TOPIC = "last-record";

    @EndpointInject("mock:result")
    private MockEndpoint result;

    private org.apache.kafka.clients.producer.KafkaProducer<String, String> producer;

    @BeforeEach
    public void before() {
        Properties props = getDefaultProperties();
        producer = new org.apache.kafka.clients.producer.KafkaProducer<>(props);
    }

    @AfterEach
    public void after() {
        if (producer != null) {
            producer.close();
        }
        // clean all test topics
        kafkaAdminClient.deleteTopics(Collections.singletonList(TOPIC));
    }

    /**
     * When consuming data with autoCommitEnable=false Then the LAST_RECORD_BEFORE_COMMIT header must be always defined
     * And it should be true only for the last one
     */
    @Test
    public void shouldStartFromBeginningWithEmptyOffsetRepository() throws InterruptedException {
        result.expectedMessageCount(5);
        result.expectedBodiesReceived("message-0", "message-1", "message-2", "message-3", "message-4");

        for (int i = 0; i < 5; i++) {
            producer.send(new ProducerRecord<>(TOPIC, "1", "message-" + i));
        }

        result.assertIsSatisfied(3000);

        List<Exchange> exchanges = result.getExchanges();
        for (int i = 0; i < exchanges.size(); i++) {
            Boolean header = exchanges.get(i).getIn().getHeader(KafkaConstants.LAST_RECORD_BEFORE_COMMIT, Boolean.class);
            assertNotNull(header, "Header not set for #" + i);
            assertEquals(header, i == exchanges.size() - 1, "Header invalid for #" + i);
            // as long as the partitions count is 1 on topic:
            header = exchanges.get(i).getIn().getHeader(KafkaConstants.LAST_POLL_RECORD, Boolean.class);
            assertNotNull(header, "Last record header not set for #" + i);
            assertEquals(header, i == exchanges.size() - 1, "Last record header invalid for #" + i);
        }
    }

    @Configuration
    public class TestConfiguration {
        @Bean
        public RouteBuilder routeBuilder() {
            return new RouteBuilder() {
                @Override
                public void configure() {
                    from("kafka:" + TOPIC + "?groupId=A&autoOffsetReset=earliest&autoCommitEnable=false").to("mock:result");
                }
            };
        }
    }
}
