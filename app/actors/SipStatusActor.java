package actors;

import akka.actor.UntypedActor;
import com.exlibris.dps.SipStatusInfo;
import com.exlibris.dps.SipWebServices;
import com.exlibris.dps.SipWebServices_Service;
import com.exlibris.dps.sdk.pds.PdsClient;
import models.Record;
import play.Logger;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import java.net.URL;
import java.util.Date;

/**
 * Created by Ott Konstantin on 25.09.2014.
 */
public class SipStatusActor extends UntypedActor {



    @Override
    public void onReceive(Object message) throws Exception {
        StatusMessage statusMessage = new StatusMessage();
        statusMessage.setType(StatusMessage.SIPSTATUSJOB);
        statusMessage.setCount(0);
        statusMessage.setStatus("Started");
        statusMessage.setStarted(new Date());
        int count = 1;
        if (message instanceof Message) {
            statusMessage.setActive(true);
            Message myMessage = (Message) message;
            String identifier = myMessage.getIdentifier();
            statusMessage.setStatus("Running");
            statusMessage.setCount(count);
            getSender().tell(statusMessage, getSelf());
            sipstatus(identifier);
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

    private void sipstatus(String identifier) {
        Record record = Record.findByIdentifier(identifier);
        if (record != null) {
            getSipStatus(record);
        }
    }

   public static boolean getSipStatus(Record record) {
        boolean ok = false;
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
            ok = true;
        } catch (Exception e) {
            Logger.error("sipstatusError for: " + record.identifier + " - " + e.getMessage());
            e.printStackTrace();

        }
        return ok;
    }
}
