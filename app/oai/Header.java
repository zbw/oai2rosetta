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
import java.util.List;
import java.util.LinkedList;
import org.dom4j.Node;
import static oai.ResponseBase.*;

/**
 * Class that holds the information returned in the <code>&lt;header&gt;</code>
 * part of a response. The verbs that has responses that includes a header
 * are <code>GetRecord</code>, <code>ListRecords</code> and <code>ListIdentifiers</code>.
 *
 * @author Oskar Grenholm, National Library of Sweden
 */
public class Header {
    private static final String IDENTIFIER_XPATH = "oai:identifier";
    private static final String DATESTAMP_XPATH = "oai:datestamp";
    private static final String SETSPEC_XPATH = "oai:setSpec";
    private String identifier;
    private String datestamp;
    private List<String> setSpecs;
    /**
     * Creates a <code>Header</code> object.
     *
     * @param node the <code>&lt;header&gt;</code> node
     */
    public Header(Node node) {
        XPathWrapper xpath = new XPathWrapper(node);
        xpath.addNamespace(OAI_NS_PREFIX, OAI_NS_URI);
        this.identifier = xpath.valueOf(IDENTIFIER_XPATH);
        this.datestamp = xpath.valueOf(DATESTAMP_XPATH);
        this.setSpecs = new LinkedList<String>();
        for(Node spec : xpath.selectNodes(SETSPEC_XPATH)) {
            setSpecs.add(spec.getText());
        }
    }
    /**
     * Get the identifier.
     *
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }
    /**
     * Get the datestamp.
     *
     * @return the datestamp
     */
    public String getDatestamp() {
        return datestamp;
    }
    /**
     * Get a list of sets.
     *
     * @return a list of sets
     */
    public List<String> getSetSpecs() {
        return setSpecs;
    }
}
