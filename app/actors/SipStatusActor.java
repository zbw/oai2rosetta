package actors;

import akka.actor.UntypedActor;
import com.exlibris.dps.SipStatusInfo;
import com.exlibris.dps.SipWebServices;
import com.exlibris.dps.SipWebServices_Service;
import models.Record;
import play.Logger;

import javax.xml.namespace.QName;
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
            int identifier = myMessage.getId();
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

    private void sipstatus(int identifier) {
        Record record = Record.findById(identifier);
        if (record != null) {
            getSipStatus(record);
        }
    }

   public static boolean getSipStatus(Record record) {
        boolean ok = false;
        String producerId = record.repository.producerId;
        try {
            SipWebServices sipws = new SipWebServices_Service(
                    new URL(record.repository.sipstatusWsdlUrl),
                    new QName("http://dps.exlibris.com/", "SipWebServices")).
                    getSipWebServicesPort();
            
            SipStatusInfo sipStatusInfo = sipws.getSIPStatusInfo(""+record.sipId);
            String sipStatus = sipStatusInfo.getStage();
            if (sipStatus == null) {
                record.sipStatus = "no rosetta status received";
            } else {
                record.sipStatus = sipStatus;
            }

            if (!record.sipStatus.isEmpty() && record.sipStatus.equals("Finished")) {
                record.status = Record.STATUSFINISHED;
            }
            record.sipActive = sipStatusInfo.getStatus();
            record.sipModul = sipStatusInfo.getModule();
            record.save();
            ok = true;
            //Logger.info("sipstatus for: " + record.sipId + " is " + record.sipStatus);
        } catch (Exception e) {
            Logger.error("sipstatusError for: " + record.identifier + " - " + e.getMessage());
            e.printStackTrace();

        }
        return ok;
    }
}
