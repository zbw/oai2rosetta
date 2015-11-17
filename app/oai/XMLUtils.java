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
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
/**
 * Utility class that has some static helper methods for handling XML.
 *
 * @author Oskar Grenholm, National Library of Sweden
 */
public class XMLUtils {
    public static final String ENCODING = "UTF-8";
    /* No need for a constructor. */
    private XMLUtils() {}
    /**
     * Writes the XML contained in the <code>Element</code> to the specified
     * <code>OutputStream</code>.
     *
     * @param element the XML element to write
     * @param stream the stream to write to
     *
     * @throws IOException
     */
    public static void writeXmlTo(Element element, OutputStream stream) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(stream, ENCODING);
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding(ENCODING);
        XMLWriter xmlwriter = new XMLWriter(writer, format);
        xmlwriter.write(element);
        xmlwriter.flush();
        writer.flush();
    }
    /**
     * Converts an <code>Element</code> to a <code>String</code>.
     *
     * @param xml the XML element to write
     *
     * @return a <code>String</code> with the XML
     * @throws IOException
     */
    public static String xmlToString(Element xml) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        writeXmlTo(xml, stream);
        return stream.toString(ENCODING);
    }
}
