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
package utils;

/**
 * Created by Ott Konstantin on 25.08.2014.
 */
/**
 * An exception that can be thrown, when file tests recognize a corrupt file
 */
public class CorruptFileException extends Exception {
    private static final long serialVersionUID = 4926653436917245659L;
    public CorruptFileException() {
        super();
    }
    public CorruptFileException(String message) {
        super(message);
    }
    public CorruptFileException(Exception e) {
        super(e);
    }
}
