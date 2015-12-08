package actors;

import akka.actor.UntypedActor;
import com.exlibris.core.sdk.formatting.DublinCore;
import com.exlibris.dps.sdk.deposit.IEParser;
import com.exlibris.dps.sdk.deposit.IEParserFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import models.Record;
import models.Resource;
import oai.OAIClient;
import oai.OAIException;
import play.Logger;
import utils.CorruptFileException;
import utils.ResourceUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Hashtable;

/**
 * Created by Ott Konstantin on 25.09.2014.
 */
public class FetchActor extends UntypedActor {


    @Override
    public void onReceive(Object message) throws Exception {

        StatusMessage statusMessage = new StatusMessage();
        statusMessage.setType(StatusMessage.FETCHJOB);
        statusMessage.setCount(0);
        statusMessage.setStatus("Started");
        statusMessage.setStarted(new Date());
        int count = 1;
        if (message instanceof Message) {
            statusMessage.setActive(true);
            Message myMessage = (Message) message;
            int identifier = myMessage.getId();
            statusMessage.setStatus("Fetching");
            statusMessage.setCount(count);
            getSender().tell(statusMessage, getSelf());
            fetch(identifier);
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

    private void fetch(int identifier) {
        Record record = Record.findById(identifier);
        fetchRecord(record);
    }

    public static boolean fetchRecord(Record record) {
        boolean ok = false;
        record.status = record.STATUSIMPORTING;
        record.save();
        OAIClient oaiClient = new OAIClient(record.repository.oaiUrl);
        Config conf = ConfigFactory.load();
        String importdirectory = conf.getString("importdirectory");
        String metadataPrefix = record.repository.metadataPrefix;
        if (metadataPrefix == null) {
            metadataPrefix = "didl";
        }
        try {
            oai.Record oairecord =oaiClient.getRecord(record.identifier, metadataPrefix);
            record.title = oairecord.getMetadataField(record.repository.oaiTitle);
            record.id = oairecord.getId();

            // maybe the oai d:Item did not have an id, so construct it from the identifier
            if (record.id == null) {
                String handle = record.identifier.substring(record.identifier.lastIndexOf(":")+1);
                handle = handle.replaceAll("/", "_");
                record.id = "hdl_" + handle;
            }
            record.save();
            String mapping = record.repository.oaiMapping;
            String[] mappings = mapping.split("\r\n");

            IEParser ie = IEParserFactory.create();
            DublinCore dc = ie.getDublinCoreParser();
            for (int i=0;i<mappings.length;i++) {
                // mapping: <iefield> <xpath in OAI> <countfield> <option [type|prefix]> <optionvalue>
                String[] keyval = mappings[i].split(" ");
                if (keyval.length > 1)  {
                    String ieField = keyval[0];
                    String xpathfield = keyval[1];
                    int countfield = Integer.parseInt(keyval[2]) - 1;
                    String option = "";
                    String optionvalue = "";

                    if (keyval.length == 5)  {
                        option = keyval[3];
                        optionvalue = keyval[4];
                    }
                    String value = oairecord.getMetadataField(xpathfield,countfield);
                    if (option.equals("type") && optionvalue.equals("dcterms:URI")) {
                        if (!value.startsWith("http")) {
                            value=null;
                        }
                    }
                    if (value != null) {
                        if (option.equals("prefix")) {
                            value = value.substring(value.indexOf(optionvalue) + optionvalue.length());
                        }
                        if (option.equals("type")) {
                            ieField = ieField.substring(3);
                            dc.addElement(dc.DC_NAMESPACE, ieField, optionvalue, value);
                            //dc.addElement(ieField, value);
                        } else {
                            dc.addElement(ieField, value);
                        }
                    }
                }
            }
            record.metadata = dc.toXml();
            //maybe we need another record with another metadataprefix for getting the resources
            if (!metadataPrefix.equals(record.repository.resourcesPrefix)
                    && record.repository.resourcesPrefix != null
                    && !record.repository.resourcesPrefix.equals("")) {
                oairecord =oaiClient.getRecord(record.identifier, record.repository.resourcesPrefix);
            }
            Hashtable<String, String> resources = oairecord.getResources("//d:Resource","ref","mimeType",record.repository.nomimetypes);
            if (resources.size()== 0) {
                record.status = record.STATUSIMPORTEDERROR;
                record.errormsg = "no files in repo";
                ok=false;
            } else {
                for (String uri : resources.keySet()) {
                    //check if already existing
                    //String origfile = uri.replaceAll("\\+", "%20");
                    String origfile = uri;
                    String filename = uri.substring(uri.lastIndexOf("/") + 1).replaceAll("\\+", " ");
                    if (!record.existResource(importdirectory + record.repository.id + "/" + record.id + "/content/streams/" + filename)) {
                        Resource resource = new Resource();
                        resource.origFile = origfile;

                        resource.localFile = importdirectory + record.repository.id + "/" + record.id + "/content/streams/" + filename;
                        resource.mime = resources.get(uri);
                        resource.record = record;
                        resource.save();
                        record.resources.add(resource);
                        ResourceUtils.getResource(resource.origFile, importdirectory + record.repository.id + "/" + record.id + "/content/streams/", filename);

                    }
                }

                record.logcreated = new Date();
                record.logmodified = new Date();
                record.status = record.STATUSIMPORTED;
                ok = true;
            }
        } catch (OAIException e) {
            record.errormsg = e.getMessage();
            record.status = record.STATUSIMPORTEDERROR;
            Logger.error("fetchError for: " + record.identifier + " - " + e.getMessage());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Logger.error("fetchError for: " + record.identifier + " - "+ e.getMessage());
            record.errormsg = "file not found: " + e.getMessage();
            record.status = record.STATUSIMPORTEDERROR;
        } catch (URISyntaxException e) {
            record.errormsg = e.getLocalizedMessage();
            record.status = record.STATUSIMPORTEDERROR;
            Logger.error("fetchError for: " + record.identifier + " - "+ e.getMessage());
        } catch (CorruptFileException e) {
            record.errormsg = e.getLocalizedMessage();
            record.status = record.STATUSIMPORTEDERROR;
            Logger.error("fetchError for: " + record.identifier + " - "+ e.getMessage());
        } catch (IOException e) {
            record.errormsg = e.getLocalizedMessage();
            record.status = record.STATUSIMPORTEDERROR;
            Logger.error("fetchError for: " + record.identifier + " - "+ e.getMessage());
        } catch (Exception e) {
            record.errormsg = e.getLocalizedMessage();
            record.status = record.STATUSIMPORTEDERROR;
            e.printStackTrace();
        }
        record.save();
        return ok;
    }
}
