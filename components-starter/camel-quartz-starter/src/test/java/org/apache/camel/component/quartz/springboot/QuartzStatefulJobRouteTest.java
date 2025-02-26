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



import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.quartz.StatefulCamelJob;
import org.apache.camel.spring.boot.CamelAutoConfiguration;

import org.junit.jupiter.api.Test;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.hamcrest.CoreMatchers;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Trigger;


@DirtiesContext
@CamelSpringBootTest
@SpringBootTest(
    classes = {
        CamelAutoConfiguration.class,
        QuartzStatefulJobRouteTest.class,
        QuartzStatefulJobRouteTest.TestConfiguration.class
    }
)
public class QuartzStatefulJobRouteTest extends BaseQuartzTest {

    
    @Autowired
    ProducerTemplate template;
    
    @Autowired
    CamelContext context;
    
    @EndpointInject("mock:result")
    MockEndpoint mock;
    
    @Test
    public void testQuartzCronRoute() throws Exception {
        
        mock.expectedMinimumMessageCount(3);

        mock.assertIsSatisfied();

        Trigger trigger = mock.getReceivedExchanges().get(0).getIn().getHeader("trigger", Trigger.class);
        assertThat(trigger instanceof CronTrigger, CoreMatchers.is(true));

        JobDetail detail = mock.getReceivedExchanges().get(0).getIn().getHeader("jobDetail", JobDetail.class);
        assertThat(detail.getJobClass().equals(StatefulCamelJob.class), CoreMatchers.is(true));
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
                    // triggers every 2th second at precise 00,02,04,06..58
                    // notice we must use + as space when configured using URI parameter
                    from("quartz://myGroup/myTimerName?cron=0/2+*+*+*+*+?&stateful=true").to("mock:result");
                }
            };
        }
    }
    
   

}
