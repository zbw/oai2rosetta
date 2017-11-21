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
import java.util.List;

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
        boolean ok = true;
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
                    boolean optional = false;
                    if (ieField.endsWith("?")) {
                        ieField = ieField.substring(0, ieField.length()-1);
                        optional=true;
                    }
                    String xpathfield = keyval[1];
                    int countfield = Integer.parseInt(keyval[2]) - 1;
                    String option = "";
                    String optionvalue = "";

                    if (keyval.length == 5)  {
                        option = keyval[3];
                        optionvalue = keyval[4];
                    }
                    String value = null;
                    if (xpathfield.equals("//setSpec")) {
                        if (countfield<0 || oairecord.getHeader().getSetSpecs().size()<=countfield) {
                            if (countfield<0) {
                                //if count < 0 then take the last field
                                value= oairecord.getHeader().getSetSpecs().get(oairecord.getHeader().getSetSpecs().size()-1);
                            } else {
                                value = oairecord.getHeader().getSetSpecs().get(countfield);
                            }
                        }                      

                    } else {
                        value = oairecord.getMetadataField(xpathfield, countfield);
                        if (value != null && option.equals("type") && optionvalue.equals("dcterms:URI")) {
                            if (!value.startsWith("http")) {
                                value = null;
                            }
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
                    } else if (optional){
                        ok=true;
                    } else {
                        record.errormsg = "No mapping for: " + mappings[i];
                        record.status = record.STATUSIMPORTEDERROR;
                        ok=false;
                        break;
                    }

                }
            }
            // collections
            if (!record.repository.mastercollection.equals("")) {
                String[] keyval = record.repository.collectionxpath.split(" ");
                String ieField = keyval[0];
                String xpathfield = keyval[1];
                int countfield = 0;
                String value="";
                if (keyval.length>2) {
                    countfield =  Integer.parseInt(keyval[2]) - 1;
                }
                if (record.repository.completecollectionpath) {
                    for (String spec : oairecord.getHeader().getSetSpecs()) {
                        value = value + "/" + spec ;
                    }
                } else {
                    if (countfield<0) {
                        //if count < 0 then take the last field
                        value= "/" +oairecord.getHeader().getSetSpecs().get(oairecord.getHeader().getSetSpecs().size()-1);
                    } else {
                        value = "/" +oairecord.getHeader().getSetSpecs().get(countfield);
                    }
                }
                value = record.repository.mastercollection + value;
                dc.addElement(ieField, value);
            }
            // ingest the global dc
            String[] dcingests = record.repository.dcingest.split("\r\n");
            for (int i=0;i<dcingests.length;i++) {
                String[] keyval = dcingests[i].split(" ");
                if (keyval.length ==2) {
                    dc.addElement(keyval[0], keyval[1]);
                }
            }
            if (ok) {
                record.metadata = dc.toXml();
                //maybe we need another record with another metadataprefix for getting the resources
                if (!metadataPrefix.equals(record.repository.resourcesPrefix)
                        && record.repository.resourcesPrefix != null
                        && !record.repository.resourcesPrefix.equals("")) {
                    oairecord = oaiClient.getRecord(record.identifier, record.repository.resourcesPrefix);
                }
                Hashtable<String, String> resources = oairecord.getResources("//d:Resource", "ref", "mimeType", record.repository.nomimetypes);
                if (resources.size() == 0) {
                    record.status = record.STATUSIMPORTEDERROR;
                    record.errormsg = "no files in repo";
                    ok = false;
                } else {
                    for (String uri : resources.keySet()) {
                        //check if already existing
                        String origfile = uri.replaceAll("\\+", "%20");
                        //String origfile = uri;
                        // somehow urls are handled different if sent throught bitstream/handle
                        // noticed on urlencoded filenames. So its better to take bitstream/handle...
                        // this is actually a workaround, it has to be fixed in OAI-PMH didl resource
                        if ( record.repository.xmlRedirect && origfile.indexOf("bitstream/handle")<0) {
                            origfile = origfile.replaceFirst("bitstream","bitstream/handle");
                        }
                        origfile = ResourceUtils.cleanUrl(origfile);
                        if (origfile == null) {
                            record.status = record.STATUSIMPORTEDERROR;
                            record.errormsg = "file " + uri +" not found";
                            record.save();
                            ok=false;
                            return ok;

                        }

                        String filename = uri.substring(uri.lastIndexOf("/") + 1).replaceAll("\\+", " ");
                        if (!record.existResource(importdirectory + record.repository.id + "/" + record.id + "/content/streams/" + filename)) {
                            ResourceUtils.getResource(origfile, importdirectory + record.repository.id + "/" + record.id + "/content/streams/", filename);
                            if (record.repository.extractZip && filename.endsWith("zip")) {
                                List<String> zips = ResourceUtils.unzip(importdirectory + record.repository.id + "/" + record.id + "/content/streams/" , filename);
                                for (String zipfilename: zips) {
                                    Resource resource = new Resource();
                                    resource.origFile = origfile;
                                    resource.localFile = importdirectory + record.repository.id + "/" + record.id + "/content/streams/" + zipfilename;
                                    resource.mime = resources.get(uri);
                                    resource.record = record;
                                    resource.save();
                                    record.resources.add(resource);
                                }
                            } else {
                                Resource resource = new Resource();
                                resource.origFile = origfile;
                                resource.localFile = importdirectory + record.repository.id + "/" + record.id + "/content/streams/" + filename;
                                resource.mime = resources.get(uri);
                                resource.record = record;
                                resource.save();
                                record.resources.add(resource);
                            }

                        }
                    }

                    record.logcreated = new Date();
                    record.logmodified = new Date();
                    record.status = record.STATUSIMPORTED;
                    ok = true;
                }
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
