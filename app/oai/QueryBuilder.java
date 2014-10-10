/*
* Copyright 2008 National Library of Sweden
* Copyright 2014 ZBW for modifications
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package oai;

/**
 * Created by Ott Konstantin on 25.08.2014.
 */
public class QueryBuilder {

    private enum Verb { Identify, GetRecord, ListIdentifiers, ListMetadataFormats, ListRecords, ListSets }
    private static final String VERB = "verb";
    private static final String QUESTION_MARK = "?";
    private static final String AMPERSAND = "&";
    private static final String EQUAL_SIGN = "=";
    private static final String IDENTIFIER = "identifier";
    private static final String METADATA_PREFIX = "metadataPrefix";
    private static final String FROM = "from";
    private static final String UNTIL = "until";
    private static final String SET = "set";
    private static final String RESUMPTION_TOKEN = "resumptionToken";
    private String basesurl;
    private String extendedBasesurl;

    /**
     * Creates a <code>QueryBuilder</code> for a specific OAI-PMH server.
     *
     * @param baseurl the base URL to the OAI-PMH server
     */
    public QueryBuilder(String baseurl) {
        this.basesurl = baseurl;
        this.extendedBasesurl = baseurl + QUESTION_MARK + VERB + EQUAL_SIGN;
    }
    /**
     * Get the base URL.
     *
     * @return the base URL
     */
    public String getBaseUrl() {
        return basesurl;
    }
    /**
     * Build the query for a <code>GetRecord</code> request.
     *
     * @param identifier the identifier
     * @param metadataPrefix the metadata prefix
     *
     * @return the full query URL as a <code>String</code>
     */
    public String buildGetRecordQuery(String identifier, String metadataPrefix) {
        StringBuffer buffer = new StringBuffer(extendedBasesurl);
        buffer.append(Verb.GetRecord);
        buffer.append(AMPERSAND).append(IDENTIFIER).append(EQUAL_SIGN).append(identifier);
        buffer.append(AMPERSAND).append(METADATA_PREFIX).append(EQUAL_SIGN).append(metadataPrefix);
        return buffer.toString();
    }
    /**
     * Build the query for a <code>Identify</code> request.
     *
     * @return the full query URL as a <code>String</code>
     */
    public String buildIdentifyQuery() {
        StringBuffer buffer = new StringBuffer(extendedBasesurl);
        buffer.append(Verb.Identify);
        return buffer.toString();
    }
    /**
     * Build the query for a <code>ListIdentifiers</code> request.
     *
     * @param metadataPrefix which metadata format
     * @param from a start date, optional (may be <code>null</code>)
     * @param until a stop date, optional (may be <code>null</code>)
     * @param set a specific set, optional (may be <code>null</code>)
     *
     * @return the full query URL as a <code>String</code>
     */
    public String buildListIdentifiersQuery(String metadataPrefix, String from, String until, String set) {
        StringBuffer buffer = new StringBuffer(extendedBasesurl);
        buffer.append(Verb.ListIdentifiers);
        buffer.append(AMPERSAND).append(METADATA_PREFIX).append(EQUAL_SIGN).append(metadataPrefix);
        if (from != null)
            buffer.append(AMPERSAND).append(FROM).append(EQUAL_SIGN).append(from);
        if (until != null)
            buffer.append(AMPERSAND).append(UNTIL).append(EQUAL_SIGN).append(until);
        if (set != null)
            buffer.append(AMPERSAND).append(SET).append(EQUAL_SIGN).append(set);
        return buffer.toString();
    }
    /**
     * Build the query for an additional <code>ListIdentifiers</code> request
     * with a <code>ResumptionToken</code>.
     *
     * @param token a resumption token returned from a previous request
     * @return the full query URL as a <code>String</code>
     */
    public String buildListIdentifiersQuery(ResumptionToken token) {
        StringBuffer buffer = new StringBuffer(extendedBasesurl);
        buffer.append(Verb.ListIdentifiers);
        buffer.append(AMPERSAND).append(RESUMPTION_TOKEN).append(EQUAL_SIGN).append(token.getId());
        return buffer.toString();
    }
    /**
     * Build the query for a <code>ListMetadtaFormats</code> request.
     *
     * @param identifier the id, optional (may be <code>null</code>)
     *
     * @return the full query URL as a <code>String</code>
     */
    public String buildListMetadataFormatsQuery(String identifier) {
        StringBuffer buffer = new StringBuffer(extendedBasesurl);
        buffer.append(Verb.ListMetadataFormats);
        if (identifier != null)
            buffer.append(AMPERSAND).append(IDENTIFIER).append(EQUAL_SIGN).append(identifier);
        return buffer.toString();
    }
    /**
     * Build the query for a <code>ListRecords</code> request.
     *
     * @param metadataPrefix which metadata format
     * @param from a start date, optional (may be <code>null</code>)
     * @param until a stop date, optional (may be <code>null</code>)
     * @param set a specific set, optional (may be <code>null</code>)
     *
     * @return the full query URL as a <code>String</code>
     */
    public String buildListRecordsQuery(String metadataPrefix, String from, String until, String set) {
        StringBuffer buffer = new StringBuffer(extendedBasesurl);
        buffer.append(Verb.ListRecords);
        buffer.append(AMPERSAND).append(METADATA_PREFIX).append(EQUAL_SIGN).append(metadataPrefix);
        if (from != null)
            buffer.append(AMPERSAND).append(FROM).append(EQUAL_SIGN).append(from);
        if (until != null)
            buffer.append(AMPERSAND).append(UNTIL).append(EQUAL_SIGN).append(until);
        if (set != null)
            buffer.append(AMPERSAND).append(SET).append(EQUAL_SIGN).append(set);
        return buffer.toString();
    }
    /**
     * Build the query for an additional <code>ListRecords</code> request
     * with a <code>ResumptionToken</code>.
     *
     * @param token a resumption token returned from a previous request
     * @return the full query URL as a <code>String</code>
     */
    public String buildListRecordsQuery(ResumptionToken token) {
        StringBuffer buffer = new StringBuffer(extendedBasesurl);
        buffer.append(Verb.ListRecords);
        buffer.append(AMPERSAND).append(RESUMPTION_TOKEN).append(EQUAL_SIGN).append(token.getId());
        return buffer.toString();
    }
    /**
     * Build the query for a <code>ListSets</code> request.
     *
     * @return the full query URL as a <code>String</code>
     */
    public String buildListSetsQuery() {
        StringBuffer buffer = new StringBuffer(extendedBasesurl);
        buffer.append(Verb.ListSets);
        return buffer.toString();
    }
    /**
     * Build the query for an additional <code>ListSets</code> request
     * with a <code>ResumptionToken</code>.
     *
     * @param token a resumption token returned from a previous request
     * @return the full query URL as a <code>String</code>
     */
    public String buildListSetsQuery(ResumptionToken token) {
        StringBuffer buffer = new StringBuffer(extendedBasesurl);
        buffer.append(Verb.ListSets);
        buffer.append(AMPERSAND).append(RESUMPTION_TOKEN).append(EQUAL_SIGN).append(token.getId());
        return buffer.toString();
    }
}
