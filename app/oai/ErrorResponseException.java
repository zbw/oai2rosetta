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
/**
 * Created by Ott Konstantin on 25.08.2014.
 */
public class ErrorResponseException extends OAIException {
    private static final long serialVersionUID = -2010182612617642664L;
    private static final String ERROR_CODE_ATTR = "code";
    public static final String BAD_ARGUMENT = "badArgument";
    public static final String BAD_RESUMPTION_TOKEN = "badResumptionToken";
    public static final String BAD_VERB = "badVerb";
    public static final String CANNOT_DISSEMINATE_FORMAT = "cannotDisseminateFormat";
    public static final String ID_DOES_NOT_EXIST = "idDoesNotExist";
    public static final String NO_RECORDS_MATCH = "noRecordsMatch";
    public static final String NO_METADATA_FORMATS = "noMetadataFormats";
    public static final String NO_SET_HIERARCHY = "noSetHierarchy";
    private String code;
    private String message;
    /**
     * Creates an <code>ErrorResponseException</code> with
     * the returned error code and error message.
     *
     * @param error the <code>&lt;error&gt;</code> element
     */
    public ErrorResponseException(Element error) {
        super();
        this.code = error.attributeValue(ERROR_CODE_ATTR);
        this.message = error.getTextTrim();
    }
    /**
     * Get the error code.
     *
     * @return the error code
     */
    public String getCode() {
        return code;
    }
    /**
     * Get the error message.
     *
     * @return the error message
     */
    public String getMessage() {
        return message;
    }
}
