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
package org.apache.camel.openapi;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.camel.BindToRegistry;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.engine.DefaultClassResolver;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.rest.RestParamType;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;

import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;


import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@DirtiesContext
@CamelSpringBootTest
@SpringBootTest(
		classes = {
				CamelAutoConfiguration.class,
				RestOpenApiReaderFileResponseModelTest.class,
				RestOpenApiReaderFileResponseModelTest.TestConfiguration.class,
				DummyRestConsumerFactory.class
		}
)
public class RestOpenApiReaderFileResponseModelTest {

	private static final Logger LOG = LoggerFactory.getLogger(RestOpenApiReaderFileResponseModelTest.class);

	@BindToRegistry("dummy-rest")
	private final DummyRestConsumerFactory factory = new DummyRestConsumerFactory();

	@Autowired
	CamelContext context;

	@Configuration
	public class TestConfiguration {

		@Bean
		public RouteBuilder routeBuilder() {
			return new RouteBuilder() {

				@Override
				public void configure() throws Exception {
					rest("/hello").consumes("application/json").produces("application/octet-stream").get("/pdf/{name}").description("Saying hi").param().name("name")
							.type(RestParamType.path).dataType("string").description("Who is it").example("Donald Duck").endParam().responseMessage().code(200)
							.message("A document as reply").responseModel(java.io.File.class).endResponseMessage().to("log:hi");
				}
			};
		}
	}

	@Test
	public void testReaderRead() throws Exception {
		BeanConfig config = new BeanConfig();
		config.setHost("localhost:8080");
		config.setSchemes(new String[] {"http"});
		config.setBasePath("/api");
		Info info = new Info();
		config.setInfo(info);
		config.setVersion("2.0");
		RestOpenApiReader reader = new RestOpenApiReader();

		OpenAPI openApi = reader.read(context, ((ModelCamelContext) context).getRestDefinitions(), config, context.getName(),
				new DefaultClassResolver());
		assertNotNull(openApi);

		String json = RestOpenApiSupport.getJsonFromOpenAPIAsString(openApi, config);

		LOG.info(json);
		assertTrue(json.contains("\"type\" : \"file\""));

		context.stop();
	}

	@Test
	public void testReaderReadV3() throws Exception {
		BeanConfig config = new BeanConfig();
		config.setHost("localhost:8080");
		config.setSchemes(new String[] {"http"});
		config.setBasePath("/api");
		Info info = new Info();
		config.setInfo(info);
		RestOpenApiReader reader = new RestOpenApiReader();

		OpenAPI openApi = reader.read(context, ((ModelCamelContext) context).getRestDefinitions(), config, context.getName(),
				new DefaultClassResolver());
		assertNotNull(openApi);

		String json = Json.pretty(openApi);

		LOG.info(json);
		assertTrue(json.contains("\"format\" : \"binary\""));
		assertTrue(json.contains("\"type\" : \"string\""));

		context.stop();
	}
}
