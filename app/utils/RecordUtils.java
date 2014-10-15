package utils;

import com.exlibris.core.sdk.consts.Enum;
import com.exlibris.core.sdk.formatting.DublinCore;
import com.exlibris.core.sdk.utils.FileUtil;
import com.exlibris.digitool.common.dnx.DnxDocument;
import com.exlibris.digitool.common.dnx.DnxDocumentFactory;
import com.exlibris.digitool.common.dnx.DnxDocumentHelper;
import com.exlibris.digitool.deposit.service.xmlbeans.DepData;
import com.exlibris.digitool.deposit.service.xmlbeans.DepositDataDocument;
import com.exlibris.digitool.deposit.service.xmlbeans.DepositResultDocument;
import com.exlibris.dps.*;
import com.exlibris.dps.sdk.deposit.IEParser;
import com.exlibris.dps.sdk.deposit.IEParserFactory;
import com.exlibris.dps.sdk.pds.PdsClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.jcraft.jsch.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import gov.loc.mets.FileType;
import gov.loc.mets.MetsDocument;
import gov.loc.mets.MetsType;
import models.Record;
import models.Resource;
import oai.OAIClient;
import oai.OAIException;
import org.apache.commons.io.FileUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import play.libs.Json;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import java.io.File;
import java.io.IOException;
import java.lang.Exception;
import java.net.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by Ott Konstantin on 28.08.2014.
 */
public class RecordUtils {

    public static boolean fetchRecord(Record record) {
        boolean ok = false;
        record.status = record.STATUSIMPORTING;
        record.save();
        OAIClient oaiClient = new OAIClient(record.repository.oaiUrl);
        Config conf = ConfigFactory.load();
        String importdirectory = conf.getString("importdirectory");
        try {
            oai.Record oairecord =oaiClient.getRecord(record.identifier, "didl");
            record.title = oairecord.getMetadataField(record.repository.oaiTitle);
            record.id = oairecord.getId();
            record.save();
            String mapping = record.repository.oaiMapping;
            String[] mappings = mapping.split("\r\n");
            Hashtable<String, String> metadata = new Hashtable<String,String>() ;
            for (int i=0;i<mappings.length;i++) {
                String[] keyval = mappings[i].split(" ");
                if (keyval.length==2)  {
                    metadata.put(keyval[0], oairecord.getMetadataField(keyval[1]));
                }
            }
            record.metadata=Json.toJson(metadata).toString();
            Hashtable<String, String> resources = oairecord.getResources("//d:Resource","ref","mimeType");

            for (String uri : resources.keySet()) {
                //check if already existing
                String origfile =  uri.replaceAll("\\+", "%20");
                String filename = uri.substring(uri.lastIndexOf("/") + 1).replaceAll("\\+", " ");
                if (!record.existResource(importdirectory + record.repository.id + "/" + record.id + "/content/streams/" + filename)) {
                    Resource resource = new Resource();
                    resource.origFile = origfile;

                    resource.localFile = importdirectory + record.repository.id + "/" + record.id + "/content/streams/" + filename;
                    ResourceUtils.getResource(resource.origFile, importdirectory + record.repository.id + "/" + record.id + "/content/streams/", filename);
                    resource.mime = resources.get(uri);
                    resource.record = record;
                    resource.save();
                    record.resources.add(resource);
                    }
                }

            record.logcreated        = new Date();
            record.logmodified      = new Date();
            record.status = record.STATUSIMPORTED;

            ok = true;
        } catch (OAIException e) {
            record.status = record.STATUSIMPORTEDERROR;
            e.printStackTrace();
        } catch (IOException e) {
            record.status = record.STATUSIMPORTEDERROR;
            e.printStackTrace();
        } catch (URISyntaxException e) {
            record.status = record.STATUSIMPORTEDERROR;
            e.printStackTrace();
        }
        record.save();
        return ok;
    }


    public static boolean createIE(Record record) {
        boolean ok = false;
        Config conf = ConfigFactory.load();
        String importdirectory   =conf.getString("importdirectory");
        String rootdirectory = importdirectory+record.repository.id+"/"+record.id+"/content/streams/";
        String IEfullFileName =  importdirectory+record.repository.id+"/"+record.id+"/content/ie1.xml";
        try {
            IEParser ie = IEParserFactory.create();
            DublinCore dc = ie.getDublinCoreParser();
            JsonNode json = Json. parse(record.metadata);
            Hashtable<String, String> metadata = Json.fromJson(json,Hashtable.class);
            for (String meta : metadata.keySet()) {
                dc.addElement(meta,metadata.get(meta));
            }
            ie.setIEDublinCore(dc);
            List<MetsType.FileSec.FileGrp> fGrpList = new ArrayList<MetsType.FileSec.FileGrp>();
            // add fileGrp
            MetsType.FileSec.FileGrp fGrp = ie.addNewFileGrp(Enum.UsageType.VIEW, Enum.PreservationType.PRESERVATION_MASTER);

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
            MetsDocument metsDoc = MetsDocument.Factory.parse(ie.toXML());
            //insert IE created in content directory
            File ieXML = new File(IEfullFileName);
            XmlOptions opt = new XmlOptions();
            opt.setSavePrettyPrint();
            FileUtil.writeFile(ieXML, metsDoc.xmlText(opt));
            ok= true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ok;
    }
    public static void getSipStatus(Record record) {
        PdsClient pds = PdsClient.getInstance();
        pds.init(record.repository.pdsUrl, false);
        String producerId = record.repository.producerId;
        String pdsHandle;
        try {
            pdsHandle = pds.login(record.repository.institution,record.repository.userName,record.repository.password);
            SipWebServices sipws = new SipWebServices_Service(
                    new URL(record.repository.sipstatusWsdlUrl),
                    new QName("http://dps.exlibris.com/", "SipWebServices")).
                    getSipWebServicesPort();
            BindingProvider bindingProvider = (BindingProvider) sipws;
            bindingProvider.getRequestContext().put(
                    BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                    record.repository.sipstatusWsdlEndpoint);
            SipStatusInfo sipStatusInfo = sipws.getSIPStatusInfo(""+record.sipId);
            String sipStatus = sipStatusInfo.getStage();
            record.sipStatus = sipStatus;
            if (!sipStatus.isEmpty() && sipStatus.equals("Finished")) {
                record.status = Record.STATUSFINISHED;
            }
            record.sipActive = sipStatusInfo.getStatus();
            record.sipModul = sipStatusInfo.getModule();
            record.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean deposit(Record record) {
        boolean ok = false;
        PdsClient pds = PdsClient.getInstance();
        pds.init(record.repository.pdsUrl, false);
        String producerId = record.repository.producerId;
        String pdsHandle;
        try {
            pdsHandle = pds.login(record.repository.institution,record.repository.userName,record.repository.password);
            ProducerWebServices pws = new ProducerWebServices_Service(
                    new URL(record.repository.producerWsdlUrl),
                    new QName("http://dps.exlibris.com/", "ProducerWebServices")).
                    getProducerWebServicesPort();
            // change the endpoint
            BindingProvider bindingProvider = (BindingProvider) pws;
            bindingProvider.getRequestContext().put(
                    BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                    record.repository.producerWsdlEndpoint);
            String producerAgentId = pws.getInternalUserIdByExternalId(record.repository.userName);
            String xmlReply = pws.getProducersOfProducerAgent(producerAgentId);
            DepositDataDocument depositDataDocument = DepositDataDocument.Factory.parse(xmlReply);
            DepositDataDocument.DepositData depositData = depositDataDocument.getDepositData();
            boolean isValidProducer = false;
            DepData[] depdata = depositData.getDepDataArray();
            for(int j=0;j<depdata.length;j++){
               if(producerId.equals(depdata[j].getId())){
                    isValidProducer = true;
                    break;
                }
            }
            if (!isValidProducer) {
                return false;
            }
            // Getting Deposit webservice handle
            DepositWebServices dpws = new DepositWebServices_Service(new URL(record.repository.depositWsdlUrl),new QName("http://dps.exlibris.com/", "DepositWebServices")).getDepositWebServicesPort();
            BindingProvider dep_bindingProvider = (BindingProvider) dpws;
            dep_bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, record.repository.depositWsdlEndpoint);

            // Getting Sip webservice handle
            SipWebServices sipws = new SipWebServices_Service(new URL(record.repository.sipstatusWsdlUrl),new QName("http://dps.exlibris.com/", "SipWebServices")).getSipWebServicesPort();
            BindingProvider sip_bindingProvider = (BindingProvider) sipws;
            sip_bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, record.repository.sipstatusWsdlEndpoint);

            String retval = dpws.submitDepositActivity(pdsHandle,
                    record.repository.materialFlowId,
                    record.id, producerId,
                    record.repository.depositSetId);
            DepositResultDocument depositResultDocument = DepositResultDocument.Factory.parse(retval);
            DepositResultDocument.DepositResult depositResult = depositResultDocument.getDepositResult();
            for(int i=0;i<1;i++) {
                Thread.sleep(3000);//wait until deposit is in
                if (depositResult.getIsError()) {
                    record.sipId = depositResult.getSipId();
                    record.status = Record.STATUSEXPORTEDERROR;
                } else {
                    record.sipId = depositResult.getSipId();
                    record.status = Record.STATUSEXPORTED;
                }
            }
            ok = true;

        } catch (XmlException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ok;
    }

    public static boolean move(Record record) {
        boolean ok = false;
        Config conf = ConfigFactory.load();
        String importdirectory = conf.getString("importdirectory");
        JSch jsch = new JSch();
        Session sftpSession;
        File keyFile = new File(record.repository.ftpKey);
        try {
            sftpSession = jsch.getSession(
                    record.repository.ftpUser,
                    record.repository.ftpHost,
                    Integer.parseInt(record.repository.ftpPort)
            );
            Hashtable config = new Hashtable();
            config.put("StrictHostKeyChecking", "no");
            sftpSession.setConfig(config);
            if (keyFile.exists()) {
                jsch.addIdentity(keyFile.getAbsolutePath(), "");
            } else {
                System.out.println("keyfile not found");
                return false;
            }
            sftpSession.connect();

            ChannelSftp sftpChannel = (ChannelSftp) sftpSession.openChannel("sftp");
            sftpChannel.connect();
            if (sftpChannel.isConnected()) {
                sftpChannel.cd(record.repository.ftpDir);
            }
            File baseDir = new File(importdirectory+record.repository.id+"/"+record.id);
            if (copyFiles(sftpChannel,baseDir)) {
                //delete local files

                try {
                    FileUtils.deleteDirectory(baseDir);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            ok = true;
            sftpSession.disconnect();
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            e.printStackTrace();
        }

        return ok;
    }
    private static boolean copyFiles(ChannelSftp sftpChannel, File src) {
        boolean overwrite = true;
        File[] list = src.listFiles();
        if(list==null){
            return false;
        }
        try {
            sftpChannel.mkdir(src.getName());
            // take parent rights
            try {
                SftpATTRS attr = sftpChannel.lstat(".");
                sftpChannel.setStat(src.getName(),attr);
            } catch (Exception e) {
                //Logger.info(e.getMessage() +" permissions for: " + src.getName() + " : " + sftpChannel.lstat(src.getName()));
            }
            //Logger.info("permissions for: " + src.getName() + " : " + sftpChannel.lstat(src.getName()));
        } catch (SftpException e) {
            if (!overwrite) {
                return false;
            }
        }
        try {
            sftpChannel.cd(src.getName());
        } catch (SftpException e1) {

        }
        // Start copying files in the directory
        for (File curFile : list) {
            if (curFile.isDirectory()) {
                copyFiles(sftpChannel,curFile);
            } else {
                try {
                    sftpChannel.put(curFile.getAbsolutePath(), curFile.getName(),ChannelSftp.OVERWRITE);
                } catch (SftpException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        try {
            sftpChannel.cd("..");
        } catch (SftpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }
}
