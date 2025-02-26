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
package org.apache.camel.component.infinispan.remote.spring;

import org.apache.camel.component.infinispan.remote.InfinispanRemoteIdempotentRepository;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext
@CamelSpringBootTest
@SpringBootTest(
        classes = {
                CamelAutoConfiguration.class,
                SpringInfinispanRemoteIdempotentRepositoryCamelIT.class
        },
        properties = { "camel.springboot.routes-include-pattern=file:src/test/resources/org/apache/camel/component/infinispan/spring/SpringInfinispanRemoteIdempotentRepositoryCamelTest.xml" }
)
@DisabledIfSystemProperty(named = "ci.env.name", matches = "github.com", disabledReason = "Disabled on GH Action due to Docker limit")
public class SpringInfinispanRemoteIdempotentRepositoryCamelIT
        extends SpringInfinispanRemoteIdempotentRepositoryTestSupport {

        @Bean
        public org.apache.camel.spi.IdempotentRepository repo() {
                InfinispanRemoteIdempotentRepository infinispanRemoteIdempotentRepository = new InfinispanRemoteIdempotentRepository("idempotent");
                infinispanRemoteIdempotentRepository.setCacheContainer(manager);

                return infinispanRemoteIdempotentRepository;
        }

        @Bean
        public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
                return new PropertySourcesPlaceholderConfigurer();
        }
}
