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
package org.apache.camel.component.platform.http.springboot;

import org.junit.jupiter.api.Test;

import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;

abstract class PlatformHttpBase {

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	public void testGet() {
		Assertions.assertThat(
						restTemplate.getForEntity("/myget", String.class).getStatusCodeValue())
				.isEqualTo(200);
	}

	@Test
	public void testPost() {
		Assertions.assertThat(
						restTemplate.postForEntity("/mypost", "test", String.class).getBody())
				.isEqualTo("TEST");
	}
}
