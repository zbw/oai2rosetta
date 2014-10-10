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
/**
 * A general exception that OAI4J throws. Often used to wrap lower-level
 * exceptions.
 *
 * @author Oskar Grenholm, National Library of Sweden
 */
public class OAIException extends Exception {
    private static final long serialVersionUID = 5926653436917245659L;
    public OAIException() {
        super();
    }
    public OAIException(Exception e) {
        super(e);
    }
}
