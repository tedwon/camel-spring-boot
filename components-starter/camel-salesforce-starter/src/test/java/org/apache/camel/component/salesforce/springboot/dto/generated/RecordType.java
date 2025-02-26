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
package org.apache.camel.component.salesforce.springboot.dto.generated;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.camel.component.salesforce.api.dto.AbstractDescribedSObjectBase;
import org.apache.camel.component.salesforce.api.dto.SObjectDescription;
import org.apache.camel.component.salesforce.api.dto.SObjectDescriptionUrls;
import org.apache.camel.component.salesforce.api.dto.SObjectField;

/**
 * Salesforce DTO for SObject RecordType
 */
public class RecordType extends AbstractDescribedSObjectBase {

    public RecordType() {
        getAttributes().setType("RecordType");
    }

    private static final SObjectDescription DESCRIPTION = createSObjectDescription();

    private String DeveloperName;

    @JsonProperty("DeveloperName")
    public String getDeveloperName() {
        return this.DeveloperName;
    }

    @JsonProperty("DeveloperName")
    public void setDeveloperName(String DeveloperName) {
        this.DeveloperName = DeveloperName;
    }

    private String NamespacePrefix;

    @JsonProperty("NamespacePrefix")
    public String getNamespacePrefix() {
        return this.NamespacePrefix;
    }

    @JsonProperty("NamespacePrefix")
    public void setNamespacePrefix(String NamespacePrefix) {
        this.NamespacePrefix = NamespacePrefix;
    }

    private String Description;

    @JsonProperty("Description")
    public String getDescription() {
        return this.Description;
    }

    @JsonProperty("Description")
    public void setDescription(String Description) {
        this.Description = Description;
    }

    private String BusinessProcessId;

    @JsonProperty("BusinessProcessId")
    public String getBusinessProcessId() {
        return this.BusinessProcessId;
    }

    @JsonProperty("BusinessProcessId")
    public void setBusinessProcessId(String BusinessProcessId) {
        this.BusinessProcessId = BusinessProcessId;
    }

    private String SobjectType;

    @JsonProperty("SobjectType")
    public String getSobjectType() {
        return this.SobjectType;
    }

    @JsonProperty("SobjectType")
    public void setSobjectType(String SobjectType) {
        this.SobjectType = SobjectType;
    }

    private Boolean IsActive;

    @JsonProperty("IsActive")
    public Boolean getIsActive() {
        return this.IsActive;
    }

    @JsonProperty("IsActive")
    public void setIsActive(Boolean IsActive) {
        this.IsActive = IsActive;
    }

    private User CreatedBy;

    @JsonProperty("CreatedBy")
    public User getCreatedBy() {
        return this.CreatedBy;
    }

    @JsonProperty("CreatedBy")
    public void setCreatedBy(User CreatedBy) {
        this.CreatedBy = CreatedBy;
    }

    private User LastModifiedBy;

    @JsonProperty("LastModifiedBy")
    public User getLastModifiedBy() {
        return this.LastModifiedBy;
    }

    @JsonProperty("LastModifiedBy")
    public void setLastModifiedBy(User LastModifiedBy) {
        this.LastModifiedBy = LastModifiedBy;
    }

    @Override
    public final SObjectDescription description() {
        return DESCRIPTION;
    }

    private static SObjectDescription createSObjectDescription() {
        final SObjectDescription description = new SObjectDescription();

        final List<SObjectField> fields1 = new ArrayList<>();
        description.setFields(fields1);

        final SObjectField sObjectField1
                = createField("Id", "Record Type ID", "id", "tns:ID", 18, false, false, false, false, false, false, true);
        fields1.add(sObjectField1);
        final SObjectField sObjectField2
                = createField("Name", "Name", "string", "xsd:string", 80, false, false, true, false, false, false, true);
        fields1.add(sObjectField2);
        final SObjectField sObjectField3 = createField("DeveloperName", "Record Type Name", "string", "xsd:string", 80, false,
                false, false, false, false, false, false);
        fields1.add(sObjectField3);
        final SObjectField sObjectField4 = createField("NamespacePrefix", "Namespace Prefix", "string", "xsd:string", 15, false,
                true, false, false, false, false, false);
        fields1.add(sObjectField4);
        final SObjectField sObjectField5 = createField("Description", "Description", "string", "xsd:string", 255, false, true,
                false, false, false, false, false);
        fields1.add(sObjectField5);
        final SObjectField sObjectField6 = createField("BusinessProcessId", "Business Process ID", "reference", "tns:ID", 18,
                false, true, false, false, false, false, false);
        fields1.add(sObjectField6);
        final SObjectField sObjectField7 = createField("SobjectType", "SObject Type Name", "picklist", "xsd:string", 40, false,
                false, false, false, false, false, false);
        fields1.add(sObjectField7);
        final SObjectField sObjectField8 = createField("IsActive", "Active", "boolean", "xsd:boolean", 0, false, false, false,
                false, false, false, false);
        fields1.add(sObjectField8);
        final SObjectField sObjectField9 = createField("CreatedById", "Created By ID", "reference", "tns:ID", 18, false, false,
                false, false, false, false, false);
        fields1.add(sObjectField9);
        final SObjectField sObjectField10 = createField("CreatedDate", "Created Date", "datetime", "xsd:dateTime", 0, false,
                false, false, false, false, false, false);
        fields1.add(sObjectField10);
        final SObjectField sObjectField11 = createField("LastModifiedById", "Last Modified By ID", "reference", "tns:ID", 18,
                false, false, false, false, false, false, false);
        fields1.add(sObjectField11);
        final SObjectField sObjectField12 = createField("LastModifiedDate", "Last Modified Date", "datetime", "xsd:dateTime", 0,
                false, false, false, false, false, false, false);
        fields1.add(sObjectField12);
        final SObjectField sObjectField13 = createField("SystemModstamp", "System Modstamp", "datetime", "xsd:dateTime", 0,
                false, false, false, false, false, false, false);
        fields1.add(sObjectField13);

        description.setLabel("Record Type");
        description.setLabelPlural("Record Types");
        description.setName("RecordType");

        final SObjectDescriptionUrls sObjectDescriptionUrls1 = new SObjectDescriptionUrls();
        sObjectDescriptionUrls1.setDescribe("/services/data/v50.0/sobjects/RecordType/describe");
        sObjectDescriptionUrls1.setRowTemplate("/services/data/v50.0/sobjects/RecordType/{ID}");
        sObjectDescriptionUrls1.setSobject("/services/data/v50.0/sobjects/RecordType");
        description.setUrls(sObjectDescriptionUrls1);

        return description;
    }
}
