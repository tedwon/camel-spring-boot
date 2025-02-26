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
import java.util.Map;
import java.util.Properties;
import java.util.stream.StreamSupport;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.component.kafka.KafkaEndpoint;
import org.apache.camel.component.kafka.SeekPolicy;
import org.apache.camel.component.kafka.serde.DefaultKafkaHeaderDeserializer;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;

import static org.apache.camel.test.junit5.TestSupport.assertIsInstanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
@CamelSpringBootTest
@SpringBootTest(
        classes = {
                CamelAutoConfiguration.class,
                BaseEmbeddedKafkaTestSupport.DefaulKafkaComponent.class,
                KafkaConsumerFullIT.class,
                KafkaConsumerFullIT.TestConfiguration.class,
        }
)
@DisabledIfSystemProperty(named = "ci.env.name", matches = "github.com", disabledReason = "Disabled on GH Action due to Docker limit")
public class KafkaConsumerFullIT extends BaseEmbeddedKafkaTestSupport {
    public static final String TOPIC = "test-full";

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConsumerFullIT.class);

    private final String from = "kafka:" + TOPIC
            + "?groupId=group1&autoOffsetReset=earliest&keyDeserializer=org.apache.kafka.common.serialization.StringDeserializer&"
            + "valueDeserializer=org.apache.kafka.common.serialization.StringDeserializer"
            + "&autoCommitIntervalMs=1000&sessionTimeoutMs=30000&autoCommitEnable=true&interceptorClasses=org.apache.camel.component.kafka.integration.MockConsumerInterceptor";


    @EndpointInject("mock:result")
    private MockEndpoint to;

    private org.apache.kafka.clients.producer.KafkaProducer<String, String> producer;

    @BeforeEach
    public void before() {
        Properties props = getDefaultProperties();
        producer = new org.apache.kafka.clients.producer.KafkaProducer<>(props);
        MockConsumerInterceptor.recordsCaptured.clear();
    }

    @AfterEach
    public void after() {
        if (producer != null) {
            producer.close();
        }
        // clean all test topics
        kafkaAdminClient.deleteTopics(Collections.singletonList(TOPIC)).all();
        to.reset();
    }

    @Order(3)
    @Test
    public void kafkaMessageIsConsumedByCamel() throws InterruptedException {
        String propagatedHeaderKey = "PropagatedCustomHeader";
        byte[] propagatedHeaderValue = "propagated header value".getBytes();
        String skippedHeaderKey = "CamelSkippedHeader";
        to.expectedMessageCount(5);
        to.expectedBodiesReceivedInAnyOrder("message-0", "message-1", "message-2", "message-3", "message-4");
        // The LAST_RECORD_BEFORE_COMMIT header should not be configured on any
        // exchange because autoCommitEnable=true
        to.expectedHeaderValuesReceivedInAnyOrder(KafkaConstants.LAST_RECORD_BEFORE_COMMIT, null, null, null, null, null);
        to.expectedHeaderReceived(propagatedHeaderKey, propagatedHeaderValue);

        for (int k = 0; k < 5; k++) {
            String msg = "message-" + k;
            ProducerRecord<String, String> data = new ProducerRecord<>(TOPIC, "1", msg);
            data.headers().add(new RecordHeader("CamelSkippedHeader", "skipped header value".getBytes()));
            data.headers().add(new RecordHeader(propagatedHeaderKey, propagatedHeaderValue));
            producer.send(data);
        }

        to.assertIsSatisfied(3000);

        assertEquals(5, StreamSupport.stream(MockConsumerInterceptor.recordsCaptured.get(0).records(TOPIC).spliterator(), false)
                .count());

        Map<String, Object> headers = to.getExchanges().get(0).getIn().getHeaders();
        assertFalse(headers.containsKey(skippedHeaderKey), "Should not receive skipped header");
        assertTrue(headers.containsKey(propagatedHeaderKey), "Should receive propagated header");
    }

    @Order(2)
    @Test
    public void kafkaRecordSpecificHeadersAreNotOverwritten() throws InterruptedException {
        String propagatedHeaderKey = KafkaConstants.TOPIC;
        byte[] propagatedHeaderValue = "propagated incorrect topic".getBytes();
        to.expectedHeaderReceived(KafkaConstants.TOPIC, TOPIC);

        ProducerRecord<String, String> data = new ProducerRecord<>(TOPIC, "1", "message");
        data.headers().add(new RecordHeader(propagatedHeaderKey, propagatedHeaderValue));
        producer.send(data);

        to.assertIsSatisfied(3000);

        Map<String, Object> headers = to.getExchanges().get(0).getIn().getHeaders();
        assertTrue(headers.containsKey(KafkaConstants.TOPIC), "Should receive KafkaEndpoint populated kafka.TOPIC header");
        assertEquals(TOPIC, headers.get(KafkaConstants.TOPIC), "Topic name received");
    }

    @Test
    @Order(1)
    public void kafkaMessageIsConsumedByCamelSeekedToBeginning() throws Exception {
        to.expectedMessageCount(5);
        to.expectedBodiesReceivedInAnyOrder("message-0", "message-1", "message-2", "message-3", "message-4");
        for (int k = 0; k < 5; k++) {
            String msg = "message-" + k;
            ProducerRecord<String, String> data = new ProducerRecord<>(TOPIC, "1", msg);
            producer.send(data);
        }
        to.assertIsSatisfied(3000);

        to.reset();

        to.expectedMessageCount(5);

        to.expectedBodiesReceivedInAnyOrder("message-0", "message-1", "message-2", "message-3", "message-4");

        // Restart endpoint,
        context.getRouteController().stopRoute("full-it");

        KafkaEndpoint kafkaEndpoint = (KafkaEndpoint) context.getEndpoint(from);
        kafkaEndpoint.getConfiguration().setSeekTo(SeekPolicy.BEGINNING);

        context.getRouteController().startRoute("full-it");

        // As wee set seek to beginning we should re-consume all messages
        to.assertIsSatisfied(3000);
    }

    @Order(4)
    @Test
    public void kafkaMessageIsConsumedByCamelSeekedToEnd() throws Exception {
        to.expectedMessageCount(5);
        to.expectedBodiesReceivedInAnyOrder("message-0", "message-1", "message-2", "message-3", "message-4");
        for (int k = 0; k < 5; k++) {
            String msg = "message-" + k;
            ProducerRecord<String, String> data = new ProducerRecord<>(TOPIC, "1", msg);
            producer.send(data);
        }
        to.assertIsSatisfied(3000);

        to.reset();

        to.expectedMessageCount(0);

        // Restart endpoint,
        context.getRouteController().stopRoute("full-it");

        KafkaEndpoint kafkaEndpoint = (KafkaEndpoint) context.getEndpoint(from);
        kafkaEndpoint.getConfiguration().setSeekTo(SeekPolicy.END);

        context.getRouteController().startRoute("full-it");

        to.assertIsSatisfied(3000);
    }

    @Order(5)
    @Test
    public void headerDeserializerCouldBeOverridden() {
        KafkaEndpoint kafkaEndpoint
                = context.getEndpoint("kafka:random_topic?headerDeserializer=#myHeaderDeserializer", KafkaEndpoint.class);
        assertIsInstanceOf(MyKafkaHeaderDeserializer.class, kafkaEndpoint.getConfiguration().getHeaderDeserializer());
    }

    @Configuration
    public class TestConfiguration {
        @Bean
        public RouteBuilder routeBuilder() {
            return new RouteBuilder() {
                @Override
                public void configure() {
                    from(from).process(exchange -> LOG.trace("Captured on the processor: {}", exchange.getMessage().getBody()))
                            .routeId("full-it").to(to);
                }
            };
        }

        @Bean("myHeaderDeserializer")
        public MyKafkaHeaderDeserializer createMyKafkaHeaderDeserializer(){
            return new MyKafkaHeaderDeserializer();
        }
    }

    private static class MyKafkaHeaderDeserializer extends DefaultKafkaHeaderDeserializer {
    }
}
