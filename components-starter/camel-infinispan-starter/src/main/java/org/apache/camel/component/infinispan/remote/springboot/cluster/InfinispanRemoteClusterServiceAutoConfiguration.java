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
package org.apache.camel.component.infinispan.remote.springboot.cluster;

import org.apache.camel.cluster.CamelClusterService;
import org.apache.camel.component.infinispan.remote.cluster.InfinispanRemoteClusterService;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.spring.boot.cluster.ClusteredRouteControllerAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.Optional;

@Configuration
@AutoConfigureBefore({ ClusteredRouteControllerAutoConfiguration.class, CamelAutoConfiguration.class })
@ConditionalOnProperty(prefix = "camel.cluster.infinispan.remote", name = "enabled")
@EnableConfigurationProperties(InfinispanRemoteClusterServiceConfiguration.class)
public class InfinispanRemoteClusterServiceAutoConfiguration {

    @Autowired
    private InfinispanRemoteClusterServiceConfiguration configuration;

    @Bean(name = "infinispan-remote-cluster-service")
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public CamelClusterService infinispanRemoteClusterService() {
        InfinispanRemoteClusterService service = new InfinispanRemoteClusterService();

        Optional.ofNullable(configuration.getId())
                .ifPresent(service::setId);
        Optional.ofNullable(configuration.getOrder())
                .ifPresent(service::setOrder);
        Optional.ofNullable(configuration.getAttributes())
                .ifPresent(service::setAttributes);
        service.setConfiguration(configuration);

        return service;
    }
}
