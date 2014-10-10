package actors;

import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import models.Record;
import utils.RecordUtils;

import java.util.Date;
import java.util.List;

/**
 * Created by Ott Konstantin on 25.09.2014.
 */
public class CreateIEActor extends UntypedActor {

    private final ActorSelection monitorActor;
    private final String type = "CREATEIE";

    public CreateIEActor() {
        this.monitorActor = this.context().system().actorSelection("user/MonitorActor");
    }

    @Override
    public void preStart() {
        monitorActor.tell(new StatusMessage(false, "idle"), getSelf());
    }

    @Override
    public void onReceive(Object message) throws Exception {
        StatusMessage statusMessage = new StatusMessage();
        statusMessage.setType(type);
        statusMessage.setCount(0);
        statusMessage.setStatus("Started");
        statusMessage.setStarted(new Date());
        int count = 1;
        if (message instanceof Message) {
            statusMessage.setActive(true);
            monitorActor.tell(statusMessage, getSelf());
            Message myMessage = (Message) message;
            String identifier = myMessage.getIdentifier();
            int limit = myMessage.getLimit();
            if (myMessage.isBatch()) {
                List<Record> records = Record.limit(identifier, Record.STATUSIMPORTED, limit);
                for (Record record : records) {
                    create(record.identifier);
                    statusMessage.setStatus("Fetching");
                    statusMessage.setCount(count);
                    monitorActor.tell(statusMessage, getSelf());
                    count++;
                    System.out.println(count);
                }
            } else {
                statusMessage.setStatus("Fetching");
                statusMessage.setCount(count);
                monitorActor.tell(statusMessage, getSelf());
                create(identifier);
            }
            statusMessage.setActive(false);
            statusMessage.setStatus("Finished");
            statusMessage.setFinished(new Date());
            monitorActor.tell(statusMessage, getSelf());
        } else if (message instanceof StatusMessage){
            getSender().tell(statusMessage,getSelf());

        } else {
            unhandled(message);
        }
    }

    private void create (String identifier) {
        Record record = Record.findByIdentifier(identifier);
        if (record != null) {
            record.status = record.STATUSIECREATING;
            record.save();
            if (RecordUtils.createIE(record)) {
                record.status = record.STATUSIECREATED;
            } else {
                record.status = record.STATUSIECREATEDERROR;
            }
            record.save();
        }
    }
}
