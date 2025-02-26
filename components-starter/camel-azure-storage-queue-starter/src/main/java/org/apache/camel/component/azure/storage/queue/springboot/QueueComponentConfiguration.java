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
package org.apache.camel.component.azure.storage.queue.springboot;

import java.time.Duration;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.queue.QueueServiceClient;
import org.apache.camel.component.azure.storage.queue.QueueConfiguration;
import org.apache.camel.component.azure.storage.queue.QueueOperationDefinition;
import org.apache.camel.spring.boot.ComponentConfigurationPropertiesCommon;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Stores and retrieves messages to/from Azure Storage Queue.
 * 
 * Generated by camel-package-maven-plugin - do not edit this file!
 */
@ConfigurationProperties(prefix = "camel.component.azure-storage-queue")
public class QueueComponentConfiguration
        extends
            ComponentConfigurationPropertiesCommon {

    /**
     * Whether to enable auto configuration of the azure-storage-queue
     * component. This is enabled by default.
     */
    private Boolean enabled;
    /**
     * The component configurations. The option is a
     * org.apache.camel.component.azure.storage.queue.QueueConfiguration type.
     */
    private QueueConfiguration configuration;
    /**
     * Service client to a storage account to interact with the queue service.
     * This client does not hold any state about a particular storage account
     * but is instead a convenient way of sending off appropriate requests to
     * the resource on the service. This client contains all the operations for
     * interacting with a queue account in Azure Storage. Operations allowed by
     * the client are creating, listing, and deleting queues, retrieving and
     * updating properties of the account, and retrieving statistics of the
     * account. The option is a com.azure.storage.queue.QueueServiceClient type.
     */
    private QueueServiceClient serviceClient;
    /**
     * Allows for bridging the consumer to the Camel routing Error Handler,
     * which mean any exceptions (if possible) occurred while the Camel consumer
     * is trying to pickup incoming messages, or the likes, will now be
     * processed as a message and handled by the routing Error Handler.
     * Important: This is only possible if the 3rd party component allows Camel
     * to be alerted if an exception was thrown. Some components handle this
     * internally only, and therefore bridgeErrorHandler is not possible. In
     * other situations we may improve the Camel component to hook into the 3rd
     * party component and make this possible for future releases. By default
     * the consumer will use the org.apache.camel.spi.ExceptionHandler to deal
     * with exceptions, that will be logged at WARN or ERROR level and ignored.
     */
    private Boolean bridgeErrorHandler = false;
    /**
     * When is set to true, the queue will be automatically created when sending
     * messages to the queue.
     */
    private Boolean createQueue = false;
    /**
     * Whether the producer should be started lazy (on the first message). By
     * starting lazy you can use this to allow CamelContext and routes to
     * startup in situations where a producer may otherwise fail during starting
     * and cause the route to fail being started. By deferring this startup to
     * be lazy then the startup failure can be handled during routing messages
     * via Camel's routing error handlers. Beware that when the first message is
     * processed then creating and starting the producer may take a little time
     * and prolong the total processing time of the processing.
     */
    private Boolean lazyStartProducer = false;
    /**
     * Queue service operation hint to the producer
     */
    private QueueOperationDefinition operation;
    /**
     * Whether autowiring is enabled. This is used for automatic autowiring
     * options (the option must be marked as autowired) by looking up in the
     * registry to find if there is a single instance of matching type, which
     * then gets configured on the component. This can be used for automatic
     * configuring JDBC data sources, JMS connection factories, AWS Clients,
     * etc.
     */
    private Boolean autowiredEnabled = true;
    /**
     * Used for enabling or disabling all consumer based health checks from this
     * component
     */
    private Boolean healthCheckConsumerEnabled = true;
    /**
     * Used for enabling or disabling all producer based health checks from this
     * component. Notice: Camel has by default disabled all producer based
     * health-checks. You can turn on producer checks globally by setting
     * camel.health.producersEnabled=true.
     */
    private Boolean healthCheckProducerEnabled = true;
    /**
     * Maximum number of messages to get, if there are less messages exist in
     * the queue than requested all the messages will be returned. If left empty
     * only 1 message will be retrieved, the allowed range is 1 to 32 messages.
     */
    private Integer maxMessages = 1;
    /**
     * The ID of the message to be deleted or updated.
     */
    private String messageId;
    /**
     * Unique identifier that must match for the message to be deleted or
     * updated.
     */
    private String popReceipt;
    /**
     * An optional timeout applied to the operation. If a response is not
     * returned before the timeout concludes a RuntimeException will be thrown.
     * The option is a java.time.Duration type.
     */
    private Duration timeout;
    /**
     * How long the message will stay alive in the queue. If unset the value
     * will default to 7 days, if -1 is passed the message will not expire. The
     * time to live must be -1 or any positive number. The format should be in
     * this form: PnDTnHnMn.nS., e.g: PT20.345S -- parses as 20.345 seconds, P2D
     * -- parses as 2 days However, in case you are using
     * EndpointDsl/ComponentDsl, you can do something like Duration.ofSeconds()
     * since these Java APIs are typesafe. The option is a java.time.Duration
     * type.
     */
    private Duration timeToLive;
    /**
     * The timeout period for how long the message is invisible in the queue.
     * The timeout must be between 1 seconds and 7 days. The format should be in
     * this form: PnDTnHnMn.nS., e.g: PT20.345S -- parses as 20.345 seconds, P2D
     * -- parses as 2 days However, in case you are using
     * EndpointDsl/ComponentDsl, you can do something like Duration.ofSeconds()
     * since these Java APIs are typesafe. The option is a java.time.Duration
     * type.
     */
    private Duration visibilityTimeout;
    /**
     * Access key for the associated azure account name to be used for
     * authentication with azure queue services
     */
    private String accessKey;
    /**
     * StorageSharedKeyCredential can be injected to create the azure client,
     * this holds the important authentication information. The option is a
     * com.azure.storage.common.StorageSharedKeyCredential type.
     */
    private StorageSharedKeyCredential credentials;

    public QueueConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(QueueConfiguration configuration) {
        this.configuration = configuration;
    }

    public QueueServiceClient getServiceClient() {
        return serviceClient;
    }

    public void setServiceClient(QueueServiceClient serviceClient) {
        this.serviceClient = serviceClient;
    }

    public Boolean getBridgeErrorHandler() {
        return bridgeErrorHandler;
    }

    public void setBridgeErrorHandler(Boolean bridgeErrorHandler) {
        this.bridgeErrorHandler = bridgeErrorHandler;
    }

    public Boolean getCreateQueue() {
        return createQueue;
    }

    public void setCreateQueue(Boolean createQueue) {
        this.createQueue = createQueue;
    }

    public Boolean getLazyStartProducer() {
        return lazyStartProducer;
    }

    public void setLazyStartProducer(Boolean lazyStartProducer) {
        this.lazyStartProducer = lazyStartProducer;
    }

    public QueueOperationDefinition getOperation() {
        return operation;
    }

    public void setOperation(QueueOperationDefinition operation) {
        this.operation = operation;
    }

    public Boolean getAutowiredEnabled() {
        return autowiredEnabled;
    }

    public void setAutowiredEnabled(Boolean autowiredEnabled) {
        this.autowiredEnabled = autowiredEnabled;
    }

    public Boolean getHealthCheckConsumerEnabled() {
        return healthCheckConsumerEnabled;
    }

    public void setHealthCheckConsumerEnabled(Boolean healthCheckConsumerEnabled) {
        this.healthCheckConsumerEnabled = healthCheckConsumerEnabled;
    }

    public Boolean getHealthCheckProducerEnabled() {
        return healthCheckProducerEnabled;
    }

    public void setHealthCheckProducerEnabled(Boolean healthCheckProducerEnabled) {
        this.healthCheckProducerEnabled = healthCheckProducerEnabled;
    }

    public Integer getMaxMessages() {
        return maxMessages;
    }

    public void setMaxMessages(Integer maxMessages) {
        this.maxMessages = maxMessages;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getPopReceipt() {
        return popReceipt;
    }

    public void setPopReceipt(String popReceipt) {
        this.popReceipt = popReceipt;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public Duration getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(Duration timeToLive) {
        this.timeToLive = timeToLive;
    }

    public Duration getVisibilityTimeout() {
        return visibilityTimeout;
    }

    public void setVisibilityTimeout(Duration visibilityTimeout) {
        this.visibilityTimeout = visibilityTimeout;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public StorageSharedKeyCredential getCredentials() {
        return credentials;
    }

    public void setCredentials(StorageSharedKeyCredential credentials) {
        this.credentials = credentials;
    }
}