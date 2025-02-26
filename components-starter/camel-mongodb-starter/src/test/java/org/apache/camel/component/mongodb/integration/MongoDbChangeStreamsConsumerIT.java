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
package org.apache.camel.component.mongodb.integration;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.CreateCollectionOptions;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.mongodb.MongoDbConstants;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DirtiesContext
@CamelSpringBootTest
@SpringBootTest(
        classes = {
                CamelAutoConfiguration.class,
                MongoDbChangeStreamsConsumerIT.class,
                AbstractMongoDbITSupport.MongoConfiguration.class
        }
)
@DisabledIfSystemProperty(named = "ci.env.name", matches = "github.com", disabledReason = "Disabled on GH Action due to Docker limit")
public class MongoDbChangeStreamsConsumerIT extends AbstractMongoDbITSupport {

    private MongoCollection<Document> mongoCollection;
    private String collectionName;

    @BeforeEach
    public void before() {
        collectionName = "camelTest";
        mongoCollection = db.getCollection(collectionName, Document.class);
        mongoCollection.drop();

        CreateCollectionOptions collectionOptions = new CreateCollectionOptions();
        db.createCollection(collectionName, collectionOptions);
        mongoCollection = db.getCollection(collectionName, Document.class);
    }

    @AfterEach
    public void after() {
        getMockEndpoint("mock:test").reset();
    }

    @Test
    public void basicTest() throws Exception {
        assertEquals(0, mongoCollection.countDocuments());

        String consumerRouteId = "simpleConsumer";
        addTestRoutes();
        context.getRouteController().startRoute(consumerRouteId);

        MockEndpoint mock = getMockEndpoint("mock:test");
        mock.expectedMessageCount(10);

        Thread t = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                mongoCollection.insertOne(new Document("increasing", i).append("string", "value" + i));
            }
        });

        t.start();
        t.join();

        mock.assertIsSatisfied();
        context.getRouteController().stopRoute(consumerRouteId);
    }

    @Test
    public void filterTest() throws Exception {
        assertEquals(0, mongoCollection.countDocuments());

        String consumerRouteId = "filterConsumer";
        addTestRoutes();
        context.getRouteController().startRoute(consumerRouteId);

        MockEndpoint mock = getMockEndpoint("mock:test");
        mock.expectedMessageCount(1);

        Thread t = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                mongoCollection.insertOne(new Document("increasing", i).append("string", "value" + i));
            }
        });

        t.start();
        t.join();

        mock.assertIsSatisfied();

        Document actualDocument = mock.getExchanges().get(0).getIn().getBody(Document.class);
        assertEquals("value2", actualDocument.get("string"));
        context.getRouteController().stopRoute(consumerRouteId);
    }

    @Test
    public void operationTypeAndIdHeaderTest() throws Exception {
        assertEquals(0, mongoCollection.countDocuments());

        String consumerRouteId = "simpleConsumer";
        addTestRoutes();
        context.getRouteController().startRoute(consumerRouteId);

        MockEndpoint mock = getMockEndpoint("mock:test");
        mock.expectedMessageCount(2);

        ObjectId objectId = new ObjectId();
        Thread t = new Thread(() -> {
            mongoCollection.insertOne(new Document("_id", objectId).append("string", "value"));
            mongoCollection.deleteOne(new Document("_id", objectId));
        });

        t.start();
        t.join();

        mock.assertIsSatisfied();

        Exchange insertExchange = mock.getExchanges().get(0);
        assertEquals("insert", insertExchange.getIn().getHeader("CamelMongoDbStreamOperationType"));
        assertEquals(objectId, insertExchange.getIn().getHeader("_id"));

        Exchange deleteExchange = mock.getExchanges().get(1);
        Document deleteBodyDocument = deleteExchange.getIn().getBody(Document.class);
        String deleteBody = "{\"_id\": \"" + objectId.toHexString() + "\"}";
        assertEquals("delete", deleteExchange.getIn().getHeader("CamelMongoDbStreamOperationType"));
        assertEquals(objectId, deleteExchange.getIn().getHeader("_id"));
        assertEquals(1, deleteBodyDocument.size());
        assertTrue(deleteBodyDocument.containsKey("_id"));
        assertEquals(objectId.toHexString(), deleteBodyDocument.getObjectId("_id").toHexString());
        context.getRouteController().stopRoute(consumerRouteId);
    }

    protected void addTestRoutes() throws Exception {
        context.addRoutes(new RouteBuilder() {

            @Override
            public void configure() {
                from("mongodb:myDb?consumerType=changeStreams&database={{mongodb.testDb}}&collection={{mongodb.testCollection}}")
                        .id("simpleConsumer")
                        .autoStartup(false)
                        .to("mock:test");

                from("mongodb:myDb?consumerType=changeStreams&database={{mongodb.testDb}}&collection={{mongodb.testCollection}}&streamFilter={{myStreamFilter}}")
                        .id("filterConsumer")
                        .autoStartup(false)
                        .log("${body}")
                        .to("mock:test");
            }
        });
    }
}
