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
package org.apache.camel.component.quartz.springboot;



import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.boot.CamelAutoConfiguration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;


@DirtiesContext
@CamelSpringBootTest
@SpringBootTest(
    classes = {
        CamelAutoConfiguration.class,
        QuartzAddRoutesAfterCamelContextStartedTest.class
    }
)
public class QuartzAddRoutesAfterCamelContextStartedTest extends BaseQuartzTest {

    
    @Autowired
    ProducerTemplate template;
    
    @Autowired
    CamelContext context;
    
    @EndpointInject("mock:result")
    MockEndpoint mock;
    
    @Test
    public void testAddRoutes() throws Exception {
        // camel context should already be started
        assertTrue(context.getStatus().isStarted());

        
        mock.expectedMessageCount(2);

        // add the quartz router after CamelContext has been started
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from("quartz://myGroup/myTimerName?trigger.repeatInterval=100&trigger.repeatCount=1").to("mock:result");
            }
        });

        // it should also work
        mock.assertIsSatisfied();
    }
    
        
   

}
