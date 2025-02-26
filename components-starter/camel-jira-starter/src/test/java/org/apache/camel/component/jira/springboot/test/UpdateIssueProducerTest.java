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


import static org.apache.camel.component.jira.JiraConstants.ISSUE_ASSIGNEE;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_KEY;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_PRIORITY_NAME;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_SUMMARY;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_TYPE_NAME;
import static org.apache.camel.component.jira.JiraConstants.JIRA_REST_CLIENT_FACTORY;
import static org.apache.camel.component.jira.springboot.test.Utils.createIssue;
import static org.apache.camel.component.jira.springboot.test.Utils.userAssignee;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.MetadataRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.BasicPriority;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Priority;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.atlassian.util.concurrent.Promise;
import io.atlassian.util.concurrent.Promises;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;



@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@CamelSpringBootTest
@SpringBootTest(
    classes = {
        CamelAutoConfiguration.class,
        UpdateIssueProducerTest.class,
        UpdateIssueProducerTest.TestConfiguration.class
    }
)

public class UpdateIssueProducerTest {

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
    
    static MetadataRestClient metadataRestClient;

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
                metadataRestClient = mock(MetadataRestClient.class);
                when(jiraRestClientFactory.createWithBasicHttpAuthentication(any(), any(), any())).thenReturn(jiraClient);
                when(jiraClient.getIssueClient()).thenReturn(issueRestClient);
                when(jiraClient.getMetadataClient()).thenReturn(metadataRestClient);
                
                Map<Integer, IssueType> issueTypes = new HashMap<>();
                issueTypes.put(1, new IssueType(null, 1L, "Bug", false, null, null));
                issueTypes.put(2, new IssueType(null, 2L, "Task", false, null, null));
                Promise<Iterable<IssueType>> promiseIssueTypes = Promises.promise(issueTypes.values());
                when(metadataRestClient.getIssueTypes()).thenReturn(promiseIssueTypes);

                Map<Integer, Priority> issuePriorities = new HashMap<>();
                issuePriorities.put(1, new Priority(null, 1L, "High", null, null, null));
                issuePriorities.put(2, new Priority(null, 2L, "Low", null, null, null));
                Promise<Iterable<Priority>> promisePriorities = Promises.promise(issuePriorities.values());
                when(metadataRestClient.getPriorities()).thenReturn(promisePriorities);

                backendIssue = createIssue(11L);
                when(issueRestClient.updateIssue(anyString(), any(IssueInput.class))).then(inv -> {
                    String issueKey = inv.getArgument(0);
                    IssueInput issueInput = inv.getArgument(1);
                    String summary = (String) issueInput.getField("summary").getValue();
                    Integer issueTypeId = Integer.parseInt(getValue(issueInput, "issuetype", "id"));
                    IssueType issueType = issueTypes.get(issueTypeId);
                    String description = (String) issueInput.getField("description").getValue();
                    Integer priorityId = Integer.parseInt(getValue(issueInput, "priority", "id"));
                    BasicPriority priority = issuePriorities.get(priorityId);
                    backendIssue = createIssue(11L, summary, issueKey, issueType, description, priority, userAssignee, null, null);
                    BasicIssue basicIssue = new BasicIssue(backendIssue.getSelf(), backendIssue.getKey(), backendIssue.getId());
                    return Promises.promise(basicIssue);
                });
                when(issueRestClient.getIssue(any())).then(inv -> Promises.promise(backendIssue));
                
                camelContext.getRegistry().bind(JIRA_REST_CLIENT_FACTORY, jiraRestClientFactory);
            }

            @Override
            public void afterApplicationStart(CamelContext camelContext) {
                //do nothing here                
            }
        };
    }
    
    @Test
    public void verifyIssueUpdated() throws InterruptedException {

        Map<String, Object> headers = new HashMap<>();
        headers.put(ISSUE_KEY, backendIssue.getKey());
        headers.put(ISSUE_TYPE_NAME, "Task");
        headers.put(ISSUE_SUMMARY, "Demo Bug jira " + (new Date()));
        headers.put(ISSUE_PRIORITY_NAME, "Low");
        headers.put(ISSUE_ASSIGNEE, "tom");

        template.sendBodyAndHeaders("New description " + (new Date()), headers);

        Issue issue = issueRestClient.getIssue(backendIssue.getKey()).claim();
        assertEquals(backendIssue, issue);
        assertEquals(backendIssue.getIssueType(), issue.getIssueType());
        assertEquals(backendIssue.getPriority(), issue.getPriority());
        assertEquals(backendIssue.getSummary(), issue.getSummary());
        assertEquals(backendIssue.getProject(), issue.getProject());
        assertEquals(backendIssue.getDescription(), issue.getDescription());
        mockResult.expectedMessageCount(1);
        mockResult.assertIsSatisfied();
    }
    
    private String getValue(IssueInput issueInput, String field, String key) {
        ComplexIssueInputFieldValue complexField = (ComplexIssueInputFieldValue) issueInput.getField(field).getValue();
        return (String) complexField.getValuesMap().get(key);
    }

    
    @Configuration
    public class TestConfiguration {
        
        

        @Bean
        public RouteBuilder routeBuilder() {
            return new RouteBuilder() {
                @Override
                public void configure() throws IOException  {
                    from("direct:start")
                            .to("jira://updateIssue?jiraUrl=" + JiraTestConstants.getJiraCredentials())
                            .to(mockResult);
                }
            };
        }
        
      
    }
    
    
    
    
}
