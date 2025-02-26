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
package org.apache.camel.component.validator;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext
@CamelSpringBootTest
@SpringBootTest(
        classes = {
                CamelAutoConfiguration.class,
                ValidatorBeanCallTest.class
        }
)
public class ValidatorBeanCallTest extends ContextTestSupport {

    @Test
    public void testCallBean() throws Exception {
        validEndpoint.expectedMessageCount(1);
        invalidEndpoint.expectedMessageCount(0);

        template
                .sendBody("direct:rootPath",
                        "<report xmlns='http://foo.com/report' xmlns:rb='http://foo.com/report-base'><author><rb:name>Knuth</rb:name></author><content><rb:chapter><rb:subject></rb:subject>"
                                             + "<rb:abstract></rb:abstract><rb:body></rb:body></rb:chapter></content></report>");

        MockEndpoint.assertIsSatisfied(validEndpoint, invalidEndpoint);
    }

    @BeforeEach
    public void setUp() throws Exception {
        deleteTestDirectory();
        validEndpoint = context.getEndpoint("mock:valid", MockEndpoint.class);
        invalidEndpoint = context.getEndpoint("mock:invalid", MockEndpoint.class);

        validEndpoint.reset();
        invalidEndpoint.reset();
    }

    @Bean
    public MyValidatorBean myBean() {
        return new MyValidatorBean();
    }

    @Bean
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:rootPath").to("validator:bean:myBean.loadFile").to("mock:valid");
            }
        };
    }

    public static class MyValidatorBean {

        public InputStream loadFile() throws Exception {
            return Files.newInputStream(Paths.get("src/test/resources/report.xsd"));
        }

    }
}
