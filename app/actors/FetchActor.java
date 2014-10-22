package actors;

import akka.actor.UntypedActor;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import models.Record;
import models.Resource;
import oai.OAIClient;
import oai.OAIException;
import play.Logger;
import play.libs.Json;
import utils.ResourceUtils;

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
            String identifier = myMessage.getIdentifier();
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

    private void fetch(String identifier) {
        Record record = Record.findByIdentifier(identifier);
        fetchRecord(record);
    }

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
            record.metadata= Json.toJson(metadata).toString();
            Hashtable<String, String> resources = oairecord.getResources("//d:Resource","ref","mimeType");

            for (String uri : resources.keySet()) {
                //check if already existing
                String origfile =  uri.replaceAll("\\+", "%20");
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

            record.logcreated        = new Date();
            record.logmodified      = new Date();
            record.status = record.STATUSIMPORTED;

            ok = true;
        } catch (OAIException e) {
            record.errormsg = e.getLocalizedMessage();
            record.status = record.STATUSIMPORTEDERROR;
            e.printStackTrace();
            Logger.error("fetchError for: " + record.identifier + " - " + e.getMessage());
        } catch (IOException e) {
            Logger.error("fetchError for: " + record.identifier + " - "+ e.getMessage());
            record.errormsg = e.getLocalizedMessage();
            record.status = record.STATUSIMPORTEDERROR;
            e.printStackTrace();
        } catch (URISyntaxException e) {
            record.errormsg = e.getLocalizedMessage();
            record.status = record.STATUSIMPORTEDERROR;
            e.printStackTrace();
            Logger.error("fetchError for: " + record.identifier + " - "+ e.getMessage());
        }
        record.save();
        return ok;
    }
}
