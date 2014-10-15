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
public class ResumptionToken {

        private String id;
        private String expirationDate;
        private String listSize;
        /**
         * Create a <code>ResumptionToken</code> from the <code>&lt;resumptionToken&gt;</code>
         * element of a response.
         *
         * @param element
         */
        public ResumptionToken(Element element) {
            this.id = element.getTextTrim();
            this.expirationDate = element.attributeValue("expirationDate");
            this.listSize = element.attributeValue("completeListSize");
        }
        /**
         * Create a <code>ResumptionToken</code> from the an id and an expiration date.
         *
         * @param id
         * @param expirationDate
         */
        public ResumptionToken(String id, String expirationDate) {
            this.id = id;
            this.expirationDate = expirationDate;
        }
        /**
         * Get the id of this resumption token.
         *
         * @return the id
         */
        public String getId() {
            return id;
        }
        /**
         * Get the date when this resumption token expires.
         *
         * @return the date
         */
        public String getExpirationDate() {
            return expirationDate;
        }

        public String getListSize() {
            return listSize;
        }
}
