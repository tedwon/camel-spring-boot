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
package org.apache.camel.itest.springboot;

import org.apache.camel.itest.springboot.util.ArquillianPackager;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(ArquillianExtension.class)
public class CamelInfinispanTest extends AbstractSpringBootTestSupport {

    @Deployment
    public static Archive<?> createSpringBootPackage() throws Exception {
        return ArquillianPackager.springBootPackage(createTestConfig());
    }

    public static ITestConfig createTestConfig() {
        return new ITestConfigBuilder()
                .module(inferModuleName(CamelInfinispanTest.class))
                .dependency("org.infinispan:infinispan-client-hotrod:14.0.21.Final")
                .dependency("org.infinispan:infinispan-commons:14.0.21.Final")
                .dependency("org.infinispan:infinispan-component-annotations:14.0.21.Final")
                .dependency("org.infinispan:infinispan-core:14.0.21.Final")
                .dependency("org.infinispan:infinispan-query-dsl:14.0.21.Final")
                .dependency("org.infinispan:infinispan-jboss-marshalling:14.0.21.Final")
                .dependency("org.infinispan:infinispan-marshaller-protostuff:14.0.21.Final")
                .dependency("org.infinispan:infinispan-remote-query-client:14.0.21.Final")
                .dependency("org.infinispan.protostream:protostream-types:4.6.5.Final")
                .dependency("org.infinispan.protostream:protostream:4.6.5.Final")
                .disableJmx("org.infinispan:*")
                .build();
    }

    @Test
    public void componentTests() throws Exception {
        this.runComponentTest(config);
        this.runModuleUnitTestsIfEnabled(config);
    }


}
