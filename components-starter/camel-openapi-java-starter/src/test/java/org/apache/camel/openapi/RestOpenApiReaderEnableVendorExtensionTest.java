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

import static org.junit.jupiter.api.Assertions.assertFalse;
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

@DirtiesContext
@CamelSpringBootTest
@SpringBootTest(
		classes = {
				CamelAutoConfiguration.class,
				RestOpenApiReaderEnableVendorExtensionTest.class,
				RestOpenApiReaderEnableVendorExtensionTest.TestConfiguration.class,
				DummyRestConsumerFactory.class,
				DummyUserService.class
		}
)
public class RestOpenApiReaderEnableVendorExtensionTest {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@BindToRegistry("dummy-rest")
	private final DummyRestConsumerFactory factory = new DummyRestConsumerFactory();

	@BindToRegistry("userService")
	private final DummyUserService userService = new DummyUserService();

	@Autowired
	CamelContext context;

	@Configuration
	public class TestConfiguration {

		@Bean
		public RouteBuilder routeBuilder() {
			return new RouteBuilder() {

				@Override
				public void configure() throws Exception {
					// enable vendor extensions
					restConfiguration().apiVendorExtension(true);

					// this user REST service is json only
					rest("/user").tag("dude").description("User rest service").consumes("application/json")
							.produces("application/json")

							.get("/{id}").description("Find user by id").outType(User.class).responseMessage()
							.message("The user returned").endResponseMessage().param().name("id")
							.type(RestParamType.path).description("The id of the user to get").dataType("integer").endParam()
							.to("bean:userService?method=getUser(${header.id})")

							.put().description("Updates or create a user").type(User.class).param().name("body")
							.type(RestParamType.body).description("The user to update or create")
							.endParam().to("bean:userService?method=updateUser")

							.get("/findAll").description("Find all users").outType(User[].class).responseMessage()
							.message("All the found users").endResponseMessage()
							.to("bean:userService?method=listUsers");
				}
			};
		}
	}

	@Test
	public void testEnableVendorExtension() throws Exception {
		BeanConfig config = new BeanConfig();
		config.setHost("localhost:8080");
		config.setSchemes(new String[] {"http"});
		config.setBasePath("/api");
		config.setTitle("Camel User store");
		config.setLicense("Apache 2.0");
		config.setVersion("2.0");
		config.setLicenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html");
		RestOpenApiReader reader = new RestOpenApiReader();

		OpenAPI openApi = reader.read(context, ((ModelCamelContext) context).getRestDefinitions(), config, context.getName(),
				new DefaultClassResolver());
		assertNotNull(openApi);

		String json = RestOpenApiSupport.getJsonFromOpenAPIAsString(openApi, config);

		log.info(json);

		String camelId = context.getName();

		assertTrue(json.contains("\"host\" : \"localhost:8080\""));
		assertTrue(json.contains("\"description\" : \"The user returned\""));
		assertTrue(json.contains("\"$ref\" : \"#/definitions/User\""));
		assertFalse(json.contains("\"enum\""));
		assertTrue(json.contains("\"x-camelContextId\" : \"" + camelId + "\""));
		context.stop();
	}

	@Test
	public void testEnableVendorExtensionV3() throws Exception {
		BeanConfig config = new BeanConfig();
		config.setHost("localhost:8080");
		config.setSchemes(new String[] {"http"});
		config.setBasePath("/api");
		config.setTitle("Camel User store");
		config.setLicense("Apache 2.0");

		config.setLicenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html");
		RestOpenApiReader reader = new RestOpenApiReader();

		OpenAPI openApi = reader.read(context, ((ModelCamelContext) context).getRestDefinitions(), config, context.getName(),
				new DefaultClassResolver());
		assertNotNull(openApi);

		String json = Json.pretty(openApi);

		log.info(json);

		String camelId = context.getName();

		assertTrue(json.contains("\"url\" : \"http://localhost:8080/api\""));
		assertTrue(json.contains("\"description\" : \"The user returned\""));
		assertTrue(json.contains("\"$ref\" : \"#/components/schemas/User\""));
		assertFalse(json.contains("\"enum\""));
		assertTrue(json.contains("\"x-camelContextId\" : \"" + camelId + "\""));
		context.stop();
	}
}
