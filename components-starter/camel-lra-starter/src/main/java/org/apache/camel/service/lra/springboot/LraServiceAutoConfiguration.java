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
package org.apache.camel.service.lra.springboot;

import org.apache.camel.CamelContext;
import org.apache.camel.saga.CamelSagaService;
import org.apache.camel.service.lra.LRASagaService;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.spring.boot.util.ConditionalOnCamelContextAndAutoConfigurationBeans;
import org.apache.camel.spring.boot.util.GroupCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@Conditional({ConditionalOnCamelContextAndAutoConfigurationBeans.class,
        LraServiceAutoConfiguration.GroupConditions.class})
@AutoConfigureAfter(CamelAutoConfiguration.class)
@EnableConfigurationProperties({LraServiceConfiguration.class})
public class LraServiceAutoConfiguration {

    @Autowired
    private CamelContext camelContext;

    static class GroupConditions extends GroupCondition {
        public GroupConditions() {
            super("camel", "camel.lra");
        }
    }

    @Bean(name = "lra-service")
    @ConditionalOnMissingBean(CamelSagaService.class)
    @ConditionalOnProperty(value = "camel.lra.enabled", havingValue = "true")
    public LRASagaService configureLraSagaService(LraServiceConfiguration configuration) throws Exception {
        LRASagaService service = new LRASagaService();

        service.setCoordinatorUrl(configuration.getCoordinatorUrl());
        service.setCoordinatorContextPath(configuration.getCoordinatorContextPath());
        service.setLocalParticipantUrl(configuration.getLocalParticipantUrl());
        service.setLocalParticipantContextPath(configuration.getLocalParticipantContextPath());

        camelContext.addService(service);
        return service;
    }
}
