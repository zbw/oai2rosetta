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
import org.dom4j.Document;
/**
 * Abstract base class that holds common functionality for the responses that
 * return lists. Can be used to get the size of the list and the actual list
 * of objects. For responses that don't return all of their results in one response,
 * the <code>ResumptionToken</code> to get the next set of data can also be
 * retrieved.
 *
 * @author Oskar Grenholm, National Library of Sweden
 */
public abstract class ListBase<T> extends ResponseBase {
    protected List<T> list;
    /**
     * Constructor.
     *
     * @param document the response
     *
     * @throws ErrorResponseException
     */
    public ListBase(Document document) throws ErrorResponseException {
        super(document);
    }
    /**
     * Get the size of the list.
     *
     * @return the size
     */
    public int size() {
        return list.size();
    }
    /**
     * Get the content of the response as a list with objects of type <code>T</code>.
     *
     * @return the list
     */
    public List<T> asList() {
        return list;
    }
    /**
     * Get the <code>ResumptionToken</code>, if any, for this response.
     *
     * @return the <code>ResumptionToken</code>, or <code>null</code>
     * if none available
     */
    public ResumptionToken getResumptionToken() {
        if (super.resumptionToken == null
                || super.resumptionToken.getId() == null
                || super.resumptionToken.getId().length() == 0)
            return null;
        return super.resumptionToken;
    }
}
