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

import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Created by Ott Konstantin on 25.08.2014.
 */
public abstract class ResponseBase {
    public static final String OAI_NS_PREFIX = "oai";
    public static final String OAI_NS_URI = "http://www.openarchives.org/OAI/2.0/";
    public static final String DIDL_NS_PREFIX = "d";
    public static final String DIDL_NS_URI = "urn:mpeg:mpeg21:2002:02-DIDL-NS";
    public static final String MODS_NS_PREFIX = "mods";
    public static final String MODS_NS_URI = "http://www.loc.gov/mods/v3";
    public static final String OAI_DC_PREFIX = "oai_dc";
    public static final String OAI_DC_URI = "http://www.openarchives.org/OAI/2.0/oai_dc/";
    public static final String DC_PREFIX = "dc";
    public static final String DC_URI = "http://purl.org/dc/elements/1.1/";
    private static final String RESPONSE_DATE_XPATH = "oai:responseDate";
    private static final String RESUMPTION_TOKEN_XPATH = "*/oai:resumptionToken";
    private static final String ERROR_XPATH = "oai:error";
    protected Document response;
    protected XPathWrapper xpath;
    protected String responseDate;
    protected ResumptionToken resumptionToken;
    /**
     * Create a <code>ResponseBase</code> from a response.
     *
     * @param document the response
     * @throws ErrorResponseException
     */
    public ResponseBase(Document document) throws ErrorResponseException {
        Element root = document.getRootElement();
        this.xpath = new XPathWrapper(root);
        xpath.addNamespace(OAI_NS_PREFIX, OAI_NS_URI);
        this.response = document;
        this.responseDate = xpath.valueOf(RESPONSE_DATE_XPATH);
        Element token = xpath.selectSingleElement(RESUMPTION_TOKEN_XPATH);
        this.resumptionToken = token != null ? new ResumptionToken(token) : null;
        Element error = xpath.selectSingleElement(ERROR_XPATH);
        if (error != null) {
            throw new ErrorResponseException(error);
        }
    }
    /**
     * Get the xml-document of the full response from the server.
     *
     * @return the response
     */
    public Document getResponse() {
        return response;
    }
    /**
     * Get the date the response was returned.
     *
     * @return the response date
     */
    public String getResponseDate() {
        return responseDate;
    }
}
