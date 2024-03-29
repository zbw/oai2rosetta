package actors;

import akka.actor.UntypedActor;
import com.exlibris.digitool.deposit.service.xmlbeans.DepData;
import com.exlibris.digitool.deposit.service.xmlbeans.DepositDataDocument;
import com.exlibris.digitool.deposit.service.xmlbeans.DepositResultDocument;
import com.exlibris.dps.DepositWebServices_Service;
import com.exlibris.dps.ProducerWebServices;
import com.exlibris.dps.ProducerWebServices_Service;
import com.exlibris.dps.sdk.pds.HeaderHandlerResolver;
import models.Record;
import org.apache.xmlbeans.XmlException;
import play.Logger;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

/**
 * Created by Ott Konstantin on 25.09.2014.
 */
public class DepositActor extends UntypedActor {



    @Override
    public void onReceive(Object message) throws Exception {
        StatusMessage statusMessage = new StatusMessage();
        statusMessage.setType(StatusMessage.DEPOSITJOB);
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
            deposit(identifier);
            statusMessage.setActive(false);
            statusMessage.setStatus("Running");
            statusMessage.setFinished(new Date());
            getSender().tell(statusMessage, getSelf());
        } else if (message instanceof StatusMessage){
            getSender().tell(statusMessage,getSelf());

        } else {
            unhandled(message);
        }
    }

    private void deposit(int identifier) {
        Record record = Record.findById(identifier);
        if (record != null && record.status == record.STATUSEXPORTED) {
            record.status = record.STATUSINGESTING;
            record.save();
            if (deposit(record)) {
                record.status = record.STATUSINGESTED;
            } else {
                record.status = record.STATUSINGESTEDERROR;
            }
            record.save();
        }
    }

   private boolean deposit(Record record) {
        boolean ok = false;

        String producerId = record.repository.producerId;

        try {
            ProducerWebServices pws = new ProducerWebServices_Service(
                    new URL(record.repository.producerWsdlUrl),
                    new QName("http://dps.exlibris.com/", "ProducerWebServices")).
                    getProducerWebServicesPort();
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
            DepositWebServices_Service dpws = new DepositWebServices_Service(new URL(record.repository.depositWsdlUrl),new QName("http://dps.exlibris.com/", "DepositWebServices"));
            dpws.setHandlerResolver(new HeaderHandlerResolver(record.repository.userName, record.repository.password, record.repository.institution));


            String retval = dpws.getDepositWebServicesPort().submitDepositActivity(null,
                    record.repository.materialFlowId,
                    record.id, producerId,
                    record.repository.depositSetId);
            DepositResultDocument depositResultDocument = DepositResultDocument.Factory.parse(retval);
            DepositResultDocument.DepositResult depositResult = depositResultDocument.getDepositResult();
            for(int i=0;i<1;i++) {
                Thread.sleep(3000);//wait until deposit is in
                if (depositResult.getIsError()) {
                    record.sipId = depositResult.getSipId();
                    record.errormsg = depositResult.getMessageCode() + " " + depositResult.getMessageDesc();
                    ok=false;
                } else {
                    record.sipId = depositResult.getSipId();
                    ok = true;
                }
            }


        } catch (XmlException e) {
            e.printStackTrace();
            Logger.error("depositError for: " + record.identifier + " - " + e.getMessage());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Logger.error("depositError for: " + record.identifier + " - "+ e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("depositError for: " + record.identifier + " - "+ e.getMessage());
            record.errormsg = ("depositError for: " + record.identifier + " - "+ e.getMessage());
            record.save();
        }
        return ok;
    }
}
