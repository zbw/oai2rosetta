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


import org.dom4j.Element;
import org.dom4j.Node;

import java.util.LinkedList;
import java.util.List;
import static oai.RecordsList.*;

/**
 * A set is a tool for selective harvesting from a repository. A record can
 * be a member of one or more sets. This class represents a specific set.
 *
 * @author Oskar Grenholm, National Library of Sweden
 */
public class Set {
    private static final String SPEC_XPATH = "oai:setSpec";
    private static final String NAME_XPATH = "oai:setName";
    private static final String DESCRIPTION_XPATH = "oai:setDescription/*";
    private String spec;
    private String name;
    private List<Element> descriptions;
    /**
     * Create a <code>Set</code> from the <code>&lt;set&gt;</code> element
     * of a <code>ListSets</code> response.
     *
     * @param node
     */
    public Set(Node node) {
        XPathWrapper xpath = new XPathWrapper(node);
        xpath.addNamespace(OAI_NS_PREFIX, OAI_NS_URI);
        this.spec = xpath.valueOf(SPEC_XPATH);
        this.name = xpath.valueOf(NAME_XPATH);
        this.descriptions = new LinkedList<Element>();
        for (Node description : xpath.selectNodes(DESCRIPTION_XPATH)) {
            descriptions.add((Element) description);
        }
    }
    /**
     * Get the set spec (a short name or id for the set).
     * This is used in the queries that take a set parameter.
     *
     * @return the set spec
     */
    public String getSpec() {
        return spec;
    }
    /**
     * Get the name of this set.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * Get the descriptions of this set.
     *
     * @return a list of descriptions
     */
    public List<Element> getDescriptions() {
        return descriptions;
    }
}
