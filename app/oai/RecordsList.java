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
import java.util.LinkedList;
import org.dom4j.Document;
import org.dom4j.Node;
/**
 * Class that represents the response from a <code>ListRecords</code> request.
 * Can be used to get the list of <code>Records</code> that made up the
 * response, i.e. <code>asList()</code> will return a <code>List&lt;Record&gt;</code>.
 *
 * @author Oskar Grenholm, National Library of Sweden
 */
public class RecordsList extends ListBase<Record> {
    private static final String RECORD_XPATH = "oai:ListRecords/oai:record";
    /**
     * Creates a <code>RecordsList</code> from the returned response.
     *
     * @param document the response
     * @throws ErrorResponseException
     */
    public RecordsList(Document document) throws ErrorResponseException {
        super(document);
        super.list = new LinkedList<Record>();
        for (Node record : xpath.selectNodes(RECORD_XPATH)) {
            list.add(new Record(document, record));
        }
    }
}