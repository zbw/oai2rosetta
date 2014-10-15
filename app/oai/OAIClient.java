/*
* Copyright 2008 National Library of Sweden
* Copyright 2014 ZBW for modifications
* original:
* https://github.com/marma/oai4j-client/blob/master/src/main/java/se/kb/oai/pmh/OaiPmhServer.java
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

import java.net.URL;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;

/**
 * Created by Ott Konstantin on 25.08.2014.
 */
public class OAIClient {
    private QueryBuilder builder;
    private SAXReader reader;
    /**
     * Creates an <code>OaiPmhServer</code> with the given base URL.
     *
     * @param url base URL that points to an OAI-PMH server
     */
    public OAIClient(String url) {
        this.builder = new QueryBuilder(url);
        this.reader = new SAXReader();
    }
    /**
     * Creates an <code>OaiPmhServer</code> with the given base URL.
     *
     * @param url base URL that points to an OAI-PMH server
     */
    public OAIClient(URL url) {
        this(url.toString());
    }
    /**
     * Get the base URL to the OAI-PMH server.
     *
     * @return the base URL
     */
    public String getBaseUrl() {
        return builder.getBaseUrl();
    }
    /**
     * Send a GetRecord request to the OAI-PMH server with
     * the specified parameters.
     *
     * @param identifier id to get a Record for
     * @param metadataPrefix which metadata format
     *
     * @return the response from the server
     * @throws OAIException
     */
    public Record getRecord(String identifier, String metadataPrefix) throws OAIException {
        try {
            String query = builder.buildGetRecordQuery(identifier, metadataPrefix);
            Document document = reader.read(query);
            return new Record(document);
        } catch (ErrorResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new OAIException(e);
        }
    }

    /**
     * Send a request to the OAI-PMH server that it should list all
     * identifiers that has metadata in the specified format.
     *
     * @param metadataPrefix which metadata format
     *
     * @return a list of identifiers
     * @throws OAIException
     */
    public IdentifiersList listIdentifiers(String metadataPrefix) throws OAIException {
        return listIdentifiers(metadataPrefix, null, null, null);
    }
    /**
     * Send a request to the OAI-PMH server that it should list all
     * identifiers that matches the given parameters.
     *
     * @param metadataPrefix which metadata format
     * @param from a start date, optional (may be <code>null</code>)
     * @param until a stop date, optional (may be <code>null</code>)
     * @param set a specific set, optional (may be <code>null</code>)
     *
     * @return a list of identifiers
     * @throws OAIException
     */
    public IdentifiersList listIdentifiers(String metadataPrefix, String from, String until, String set) throws OAIException {
        try {
            String query = builder.buildListIdentifiersQuery(metadataPrefix, from, until, set);
            Document document = reader.read(query);
            return new IdentifiersList(document);
        } catch (ErrorResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new OAIException(e);
        }
    }
    /**
     * List next set of identifiers not returned in the previous response from
     * a call to listIdentifiers().
     *
     * @param resumptionToken a resumption token returned from a previous call
     *
     * @return a list of identifiers
     * @throws OAIException
     */
    public IdentifiersList listIdentifiers(ResumptionToken resumptionToken) throws OAIException {
        try {
            String query = builder.buildListIdentifiersQuery(resumptionToken);
            Document document = reader.read(query);
            return new IdentifiersList(document);
        } catch (ErrorResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new OAIException(e);
        }
    }
    /**
     * Send a request for the OAI-PMH server to return a list of Records.
     *
     * @param metadataPrefix which metadata format
     *
     * @return a list of records
     * @throws OAIException
     */
    public RecordsList listRecords(String metadataPrefix) throws OAIException {
        return listRecords(metadataPrefix, null, null, null);
    }
    /**
     * Send a request for the OAI-PMH server to return a list of Records.
     *
     * @param metadataPrefix which metadata format
     * @param from a start date, optional (may be <code>null</code>)
     * @param until a stop date, optional (may be <code>null</code>)
     * @param set a specific set, optional (may be <code>null</code>)
     *
     * @return a lsit of records
     * @throws OAIException
     */
    public RecordsList listRecords(String metadataPrefix, String from, String until, String set) throws OAIException {
        try {
            String query = builder.buildListRecordsQuery(metadataPrefix, from, until, set);
            Document document = reader.read(query);
            return new RecordsList(document);
        } catch (ErrorResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new OAIException(e);
        }
    }
    /**
     * List next set of records not returned in the previous response from
     * a call to listRecords().
     *
     * @param resumptionToken a resumption token returned from a previous call
     *
     * @return a list of records
     * @throws OAIException
     */
    public RecordsList listRecords(ResumptionToken resumptionToken) throws OAIException {
        try {
            String query = builder.buildListRecordsQuery(resumptionToken);
            Document document = reader.read(query);
            return new RecordsList(document);
        } catch (ErrorResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new OAIException(e);
        }
    }

    /**
     * List all sets the OAI-PMH server has.
     *
     * @return a list of sets
     * @throws OAIException
     */
    public SetsList listSets() throws OAIException {
        try {
            String query = builder.buildListSetsQuery();
            Document document = reader.read(query);
            return new SetsList(document);
        } catch (ErrorResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new OAIException(e);
        }
    }
    /**
     * List next set of sets not returned in the previous response from
     * a call to listSets().
     *
     * @param resumptionToken
     *
     * @return a list of sets
     * @throws OAIException
     */
    public SetsList listSets(ResumptionToken resumptionToken) throws OAIException {
        try {
            String query = builder.buildListSetsQuery(resumptionToken);
            Document document = reader.read(query);
            return new SetsList(document);
        } catch (ErrorResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new OAIException(e);
        }
    }
}
