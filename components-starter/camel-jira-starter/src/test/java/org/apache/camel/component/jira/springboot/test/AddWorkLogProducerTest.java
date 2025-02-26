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
package org.apache.camel.component.jira.springboot.test;


import static org.apache.camel.component.jira.JiraConstants.ISSUE_KEY;
import static org.apache.camel.component.jira.JiraConstants.JIRA_REST_CLIENT_FACTORY;
import static org.apache.camel.component.jira.JiraConstants.MINUTES_SPENT;
import static org.apache.camel.component.jira.springboot.test.Utils.createIssueWithComments;
import static org.apache.camel.component.jira.springboot.test.Utils.createIssueWithWorkLogs;
import static org.apache.camel.component.jira.springboot.test.Utils.newWorkLog;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Worklog;
import com.atlassian.jira.rest.client.api.domain.input.WorklogInput;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.atlassian.util.concurrent.Promises;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.mockito.stubbing.Answer;


@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@CamelSpringBootTest
@SpringBootTest(
    classes = {
        CamelAutoConfiguration.class,
        AddWorkLogProducerTest.class,
        AddWorkLogProducerTest.TestConfiguration.class
    }
)

public class AddWorkLogProducerTest {

    @Autowired
    private CamelContext camelContext;
    

    
    @Autowired
    @Produce("direct:start")
    ProducerTemplate template;

    @EndpointInject("mock:result")
    MockEndpoint mockResult;
    
    static JiraRestClient jiraClient;
    
    static JiraRestClientFactory jiraRestClientFactory;
    
    static IssueRestClient issueRestClient;

    static Issue backendIssue;
    
    
    
    @Bean
    CamelContextConfiguration contextConfiguration() {
        return new CamelContextConfiguration() {
            @Override
            public void beforeApplicationStart(CamelContext context) {
                //get chance to mock camelContext/Registry
                jiraRestClientFactory = mock(JiraRestClientFactory.class);
                jiraClient = mock(JiraRestClient.class);
                issueRestClient = mock(IssueRestClient.class);
                lenient().when(jiraRestClientFactory.createWithBasicHttpAuthentication(any(), any(), any())).thenReturn(jiraClient);
                lenient().when(jiraClient.getIssueClient()).thenReturn(issueRestClient);

                backendIssue = createIssueWithComments(1, 1);
                lenient().when(issueRestClient.getIssue(any())).then(inv -> Promises.promise(backendIssue));
                camelContext.getRegistry().bind(JIRA_REST_CLIENT_FACTORY, jiraRestClientFactory);
            }

            @Override
            public void afterApplicationStart(CamelContext camelContext) {
                //do nothing here                
            }
        };
    }
    
    @Test
    public void testAddWorkLog() throws InterruptedException {
        int minutesSpent = 10;
        Map<String, Object> headers = new HashMap<>();
        headers.put(ISSUE_KEY, backendIssue.getKey());
        headers.put(MINUTES_SPENT, minutesSpent);
        String comment = "A new test comment " + new Date();

        when(issueRestClient.addWorklog(any(URI.class), any(WorklogInput.class)))
                .then((Answer<Void>) inv -> {
                    Collection<Worklog> workLogs = new ArrayList<>();
                    workLogs.add(newWorkLog(backendIssue.getId(), minutesSpent, comment));
                    backendIssue = createIssueWithWorkLogs(backendIssue.getId(), workLogs);
                    return null;
                });

        template.sendBodyAndHeaders(comment, headers);

        mockResult.expectedMessageCount(1);
        mockResult.assertIsSatisfied();

        verify(issueRestClient).getIssue(backendIssue.getKey());
        verify(issueRestClient).addWorklog(eq(backendIssue.getWorklogUri()), any(WorklogInput.class));
    }
    
    @Test
    public void testAddWorkLogMissingIssueKey() throws InterruptedException {
        int minutesSpent = 3;
        Map<String, Object> headers = new HashMap<>();
        headers.put(MINUTES_SPENT, minutesSpent);
        String comment = "A new test comment " + new Date();

        try {
            template.sendBodyAndHeaders(comment, headers);
            fail("Should have thrown an exception");
        } catch (CamelExecutionException e) {
            IllegalArgumentException cause = assertInstanceOf(IllegalArgumentException.class, e.getCause());
            assertTrue(cause.getMessage().contains(ISSUE_KEY));
        }
        mockResult.reset();
        mockResult.expectedMessageCount(0);
        mockResult.assertIsSatisfied();

        verify(issueRestClient, never()).getIssue(any(String.class));
        verify(issueRestClient, never()).addWorklog(any(URI.class), any(WorklogInput.class));
    }

    @Test
    public void testAddWorkLogMissingMinutesSpent() throws InterruptedException {
        
        Map<String, Object> headers = new HashMap<>();
        headers.put(ISSUE_KEY, backendIssue.getKey());
        String comment = "A new test comment " + new Date();

        try {
            template.sendBodyAndHeaders(comment, headers);
            fail("Should have thrown an exception");
        } catch (CamelExecutionException e) {
            IllegalArgumentException cause = assertInstanceOf(IllegalArgumentException.class, e.getCause());
            assertTrue(cause.getMessage().contains(MINUTES_SPENT));
        }
        
        mockResult.reset();
        mockResult.expectedMessageCount(0);
        mockResult.assertIsSatisfied();

        verify(issueRestClient, never()).getIssue(any(String.class));
        verify(issueRestClient, never()).addWorklog(any(URI.class), any(WorklogInput.class));
    }

    @Test
    public void testAddWorkLogMissingComment() throws InterruptedException {
        int minutesSpent = 60;
        Map<String, Object> headers = new HashMap<>();
        headers.put(ISSUE_KEY, backendIssue.getKey());
        headers.put(MINUTES_SPENT, minutesSpent);

        try {
            template.sendBodyAndHeaders(null, headers);
            fail("Should have thrown an exception");
        } catch (CamelExecutionException e) {
            IllegalArgumentException cause = assertInstanceOf(IllegalArgumentException.class, e.getCause());
            assertTrue(cause.getMessage().contains("Missing exchange body"));
        }
        mockResult.reset();
        mockResult.expectedMessageCount(0);
        mockResult.assertIsSatisfied();

        verify(issueRestClient, never()).getIssue(any(String.class));
        verify(issueRestClient, never()).addWorklog(any(URI.class), any(WorklogInput.class));
    }

    
    @Configuration
    public class TestConfiguration {
        
        

        @Bean
        public RouteBuilder routeBuilder() {
            return new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from("direct:start")
                    .to("jira://addWorkLog?jiraUrl=" + JiraTestConstants.getJiraCredentials())
                    .to(mockResult);
                }
            };
        }
        
      
    }
    
    
    
    
}
