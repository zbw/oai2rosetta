package actors;

import akka.actor.UntypedActor;
import com.exlibris.core.sdk.consts.Enum;
import com.exlibris.core.sdk.formatting.DublinCore;
import com.exlibris.core.sdk.formatting.DublinCoreFactory;
import com.exlibris.core.sdk.utils.FileUtil;
import com.exlibris.digitool.common.dnx.DnxDocument;
import com.exlibris.digitool.common.dnx.DnxDocumentFactory;
import com.exlibris.digitool.common.dnx.DnxDocumentHelper;
import com.exlibris.dps.sdk.deposit.IEParser;
import com.exlibris.dps.sdk.deposit.IEParserFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import gov.loc.mets.FileType;
import gov.loc.mets.MdSecType;
import gov.loc.mets.MetsDocument;
import gov.loc.mets.MetsType;
import models.Record;
import models.Resource;
import oai.OAIClient;
import oai.OAIException;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import play.Logger;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Ott Konstantin on 25.09.2014.
 */
public class CreateIEActor extends UntypedActor {



    @Override
    public void onReceive(Object message) throws Exception {
        StatusMessage statusMessage = new StatusMessage();
        statusMessage.setType(StatusMessage.CREATEJOB);
        statusMessage.setCount(0);
        statusMessage.setStatus("Started");
        statusMessage.setStarted(new Date());
        int count = 1;
        if (message instanceof Message) {
            statusMessage.setActive(true);
            Message myMessage = (Message) message;
            int identifier = myMessage.getId();
            statusMessage.setStatus("Running");
            statusMessage.setCount(count);
            getSender().tell(statusMessage, getSelf());
            create(identifier);
            statusMessage.setActive(false);
            statusMessage.setStatus("Finished");
            statusMessage.setFinished(new Date());
            getSender().tell(statusMessage, getSelf());
        } else if (message instanceof StatusMessage){
            getSender().tell(statusMessage,getSelf());

        } else {
            unhandled(message);
        }
    }

    private void create (int id) {
        Record record = Record.findById(id);
        if (record != null) {
            record.status = record.STATUSIECREATING;
            record.save();
            if (createIE(record)) {
                record.status = record.STATUSIECREATED;
            } else {
                record.status = record.STATUSIECREATEDERROR;
            }
            record.save();
        }
    }

    public static boolean createIE(Record record) {
        boolean ok = false;
        Config conf = ConfigFactory.load();
        String importdirectory   =conf.getString("importdirectory");
        String rootdirectory = importdirectory+record.repository.id+"/"+record.id+"/content/streams/";
        String IEfullFileName =  importdirectory+record.repository.id+"/"+record.id+"/content/ie1.xml";
        try {
            IEParser ie = IEParserFactory.create();
            DublinCore dc = DublinCoreFactory.getInstance().createDocument(record.metadata);
            // add the ingested dc fields

            ie.setIEDublinCore(dc);
            List<MetsType.FileSec.FileGrp> fGrpList = new ArrayList<MetsType.FileSec.FileGrp>();
            // add fileGrp
            MetsType.FileSec.FileGrp fGrp = ie.addNewFileGrp(com.exlibris.core.sdk.consts.Enum.UsageType.VIEW, Enum.PreservationType.PRESERVATION_MASTER);

            // add dnx - A new DNX is constructed and added on the file group level
            DnxDocument dnxDocument = ie.getFileGrpDnx(fGrp.getID());
            DnxDocumentHelper documentHelper = new DnxDocumentHelper(dnxDocument);
            documentHelper.getGeneralRepCharacteristics().setRevisionNumber("1");
            documentHelper.getGeneralRepCharacteristics().setLabel(record.title);
            ie.setFileGrpDnx(documentHelper.getDocument(), fGrp.getID());
            fGrpList.add(fGrp);
            FileNameMap fileNameMap = URLConnection.getFileNameMap();
            int count =0;
            List<Resource> resources = record.getResources();
            for (Resource resource : resources) {
                File file = new File(resource.localFile);
                String ext = "";
                try {
                    ext = resource.mime.substring(resource.mime.lastIndexOf("/")+1);
                } catch ( ArrayIndexOutOfBoundsException ae) {}
                FileType fileType = ie.addNewFile(fGrp, resource.mime, file.getName(), ext.toUpperCase() + " file " + count);
                // add dnx - A new DNX is constructed and added on the file level
                DnxDocument dnx = ie.getFileDnx(fileType.getID());
                DnxDocumentHelper fileDocumentHelper = new DnxDocumentHelper(dnx);
                fileDocumentHelper.getGeneralFileCharacteristics().setLabel(file.getName());
                fileDocumentHelper.getGeneralFileCharacteristics().setFileOriginalPath(resource.origFile);
                ie.setFileDnx(fileDocumentHelper.getDocument(), fileType.getID());
                count++;
                resource.delete();
            }

            ie.generateChecksum(rootdirectory, Enum.FixityType.MD5.toString());
            ie.updateSize(rootdirectory);
            ie.generateStructMap(fGrpList.get(0),null, "Table of Contents");

            //CMS mit dem anderen dh geht es steht dann aber an der falschen stelle...
            DnxDocument ieDnx = DnxDocumentFactory.getInstance().createDnxDocument();
            DnxDocumentHelper ieDnxHelper = new DnxDocumentHelper(ieDnx);
            boolean isCMS = false;
            if (record.repository.cms != null && record.repository.cmsfield != null) {
                if (dc.getDcValue(record.repository.cmsfield) != null) {
                    isCMS = true;
                }
            }
            if (isCMS) {
                DnxDocumentHelper.CMS cms = ieDnxHelper. new CMS();
                cms.setSystem(record.repository.cms);
                cms.setRecordId(dc.getDcValue(record.repository.cmsfield));
                cms.setMId("CMS"+ dc.getDcValue(record.repository.cmsfield));
                ieDnxHelper.setCMS(cms);
                ie.setIeDnx(ieDnxHelper.getDocument());
            }
            // attach Source MD
            if (record.repository.source_mdformat!=null && !record.repository.source_mdformat.equals("")) {
                XmlObject xml = getSourceMD(record);

                if (record.repository.source_mdformat.equals("mods")) {
                    ie.setIeSourceMd(gov.loc.mets.MdSecType.MdWrap.MDTYPE.MODS, xml);

                } else if (record.repository.source_mdformat.equals("oai_dc")) {
                    ie.setIeSourceMd(MdSecType.MdWrap.MDTYPE.DC, xml);
                } else if (record.repository.source_mdformat.equals("marc")) {
                    ie.setIeSourceMd(MdSecType.MdWrap.MDTYPE.MARC, xml);
                } else {
                    ie.setIeSourceMd(MdSecType.MdWrap.MDTYPE.OTHER, xml);
                }
                DnxDocument modsDnx = ie.getIeDnx();
                DnxDocumentHelper documentModsHelper = new DnxDocumentHelper(modsDnx);
                ie.setIeDnx(modsDnx);
            }
            MetsDocument metsDoc = MetsDocument.Factory.parse(ie.toXML());
            //insert IE created in content directory
            File ieXML = new File(IEfullFileName);
            XmlOptions opt = new XmlOptions();
            opt.setSavePrettyPrint();
            FileUtil.writeFile(ieXML, metsDoc.xmlText(opt));
            ok= true;
        } catch (Exception e) {
            if (e.getLocalizedMessage().length() > 100) {
                record.errormsg = e.getLocalizedMessage().substring(0, 100);
            } else {
                record.errormsg = e.getLocalizedMessage();
            }
            e.printStackTrace();
            Logger.error("createIEError for: " + record.identifier + " - " + e.getMessage());
        }
        return ok;
    }

    private static XmlObject getSourceMD(Record record) throws XmlException, ParserConfigurationException, SAXException, OAIException, IOException {
        OAIClient oaiClient = new OAIClient(record.repository.oaiUrl);
        oai.Record oairecord =oaiClient.getRecord(record.identifier, record.repository.source_mdformat);
        XmlOptions options = new XmlOptions();
        options.setCharacterEncoding("UTF-8");
        String md = oairecord.getMetadataAsString();
        //md = basic(md);
        try {
            return XmlObject.Factory.parse(md, options);
        } catch (Exception ice) {
            throw new XmlException("invalid chars: "+ basic(md));
        }
    }

    /**
     * There were some Supplementary Characters in a dataset and the parser does not like them,
     * so get them out of here...
     * this one filters them out in a lambda way
     * http://www.oracle.com/us/technologies/java/supplementary-142654.html
     * @param s
     * @return
     */
    private static String basic1 (String s) {
        return s.codePoints().filter(cp -> cp < 0x10000)
                .collect(StringBuilder::new,
                        StringBuilder::appendCodePoint,
                        StringBuilder::append)
                .toString();
    }

    /**
     * and this one looks for surrogates
     * @param s
     * @return
     */
    private static String basic(String s) {
        StringBuilder sb = new StringBuilder();
        String out="";
        for (char ch : s.toCharArray()) {
            if (!Character.isLowSurrogate(ch) && !Character.isHighSurrogate(ch)) {
                sb.append(ch);
            } else {
                System.out.println(ch + " " + Character.getNumericValue(ch));
                System.out.println( "\\u" + Integer.toHexString(ch | 0x10000).substring(1) );
                out +=   "\\u" + Integer.toHexString(ch | 0x10000).substring(1) + " ";
            }

        }
        //return sb.length() == s.length() ? s : sb.toString();
        return out;
    }
}
