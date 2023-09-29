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
package org.apache.camel.dataformat.univocity.springboot;

import org.apache.camel.spring.boot.DataFormatConfigurationPropertiesCommon;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Marshal and unmarshal Java objects from and to fixed length records using
 * UniVocity Parsers.
 * 
 * Generated by camel-package-maven-plugin - do not edit this file!
 */
@ConfigurationProperties(prefix = "camel.dataformat.univocity-fixed")
public class UniVocityFixedDataFormatConfiguration
        extends
            DataFormatConfigurationPropertiesCommon {

    /**
     * Whether to enable auto configuration of the univocityFixed data format.
     * This is enabled by default.
     */
    private Boolean enabled;
    /**
     * The padding character. The default value is a space
     */
    private String padding;
    /**
     * Whether or not the trailing characters until new line must be ignored.
     * The default value is false
     */
    private Boolean skipTrailingCharsUntilNewline = false;
    /**
     * Whether or not the record ends on new line. The default value is false
     */
    private Boolean recordEndsOnNewline = false;
    /**
     * The string representation of a null value. The default value is null
     */
    private String nullValue;
    /**
     * Whether or not the empty lines must be ignored. The default value is true
     */
    private Boolean skipEmptyLines = true;
    /**
     * Whether or not the trailing white spaces must be ignored. The default
     * value is true
     */
    private Boolean ignoreTrailingWhitespaces = true;
    /**
     * Whether or not the leading white spaces must be ignored. The default
     * value is true
     */
    private Boolean ignoreLeadingWhitespaces = true;
    /**
     * Whether or not the headers are disabled. When defined, this option
     * explicitly sets the headers as null which indicates that there is no
     * header. The default value is false
     */
    private Boolean headersDisabled = false;
    /**
     * Whether or not the header must be read in the first line of the test
     * document. The default value is false
     */
    private Boolean headerExtractionEnabled = false;
    /**
     * The maximum number of record to read.
     */
    private Integer numberOfRecordsToRead;
    /**
     * The String representation of an empty value.
     */
    private String emptyValue;
    /**
     * The line separator of the files. The default value is to use the JVM
     * platform line separator
     */
    private String lineSeparator;
    /**
     * The normalized line separator of the files. The default value is a new
     * line character.
     */
    private String normalizedLineSeparator;
    /**
     * The comment symbol. The default value is #
     */
    private String comment = "#";
    /**
     * Whether the unmarshalling should produce an iterator that reads the lines
     * on the fly or if all the lines must be read at once. The default value is
     * false
     */
    private Boolean lazyLoad = false;
    /**
     * Whether the unmarshalling should produce maps for the lines values
     * instead of lists. It requires to have header (either defined or
     * collected). The default value is false
     */
    private Boolean asMap = false;

    public String getPadding() {
        return padding;
    }

    public void setPadding(String padding) {
        this.padding = padding;
    }

    public Boolean getSkipTrailingCharsUntilNewline() {
        return skipTrailingCharsUntilNewline;
    }

    public void setSkipTrailingCharsUntilNewline(
            Boolean skipTrailingCharsUntilNewline) {
        this.skipTrailingCharsUntilNewline = skipTrailingCharsUntilNewline;
    }

    public Boolean getRecordEndsOnNewline() {
        return recordEndsOnNewline;
    }

    public void setRecordEndsOnNewline(Boolean recordEndsOnNewline) {
        this.recordEndsOnNewline = recordEndsOnNewline;
    }

    public String getNullValue() {
        return nullValue;
    }

    public void setNullValue(String nullValue) {
        this.nullValue = nullValue;
    }

    public Boolean getSkipEmptyLines() {
        return skipEmptyLines;
    }

    public void setSkipEmptyLines(Boolean skipEmptyLines) {
        this.skipEmptyLines = skipEmptyLines;
    }

    public Boolean getIgnoreTrailingWhitespaces() {
        return ignoreTrailingWhitespaces;
    }

    public void setIgnoreTrailingWhitespaces(Boolean ignoreTrailingWhitespaces) {
        this.ignoreTrailingWhitespaces = ignoreTrailingWhitespaces;
    }

    public Boolean getIgnoreLeadingWhitespaces() {
        return ignoreLeadingWhitespaces;
    }

    public void setIgnoreLeadingWhitespaces(Boolean ignoreLeadingWhitespaces) {
        this.ignoreLeadingWhitespaces = ignoreLeadingWhitespaces;
    }

    public Boolean getHeadersDisabled() {
        return headersDisabled;
    }

    public void setHeadersDisabled(Boolean headersDisabled) {
        this.headersDisabled = headersDisabled;
    }

    public Boolean getHeaderExtractionEnabled() {
        return headerExtractionEnabled;
    }

    public void setHeaderExtractionEnabled(Boolean headerExtractionEnabled) {
        this.headerExtractionEnabled = headerExtractionEnabled;
    }

    public Integer getNumberOfRecordsToRead() {
        return numberOfRecordsToRead;
    }

    public void setNumberOfRecordsToRead(Integer numberOfRecordsToRead) {
        this.numberOfRecordsToRead = numberOfRecordsToRead;
    }

    public String getEmptyValue() {
        return emptyValue;
    }

    public void setEmptyValue(String emptyValue) {
        this.emptyValue = emptyValue;
    }

    public String getLineSeparator() {
        return lineSeparator;
    }

    public void setLineSeparator(String lineSeparator) {
        this.lineSeparator = lineSeparator;
    }

    public String getNormalizedLineSeparator() {
        return normalizedLineSeparator;
    }

    public void setNormalizedLineSeparator(String normalizedLineSeparator) {
        this.normalizedLineSeparator = normalizedLineSeparator;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Boolean getLazyLoad() {
        return lazyLoad;
    }

    public void setLazyLoad(Boolean lazyLoad) {
        this.lazyLoad = lazyLoad;
    }

    public Boolean getAsMap() {
        return asMap;
    }

    public void setAsMap(Boolean asMap) {
        this.asMap = asMap;
    }
}