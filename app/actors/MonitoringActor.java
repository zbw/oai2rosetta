package actors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Ott Konstantin on 07.10.2014.
 */
public class MonitoringActor extends UntypedActor {

    private final Map<ActorRef,StatusMessage> jobs = new ConcurrentHashMap<ActorRef,StatusMessage>();
    public MonitoringActor() {

    }
    @Override
    public void preStart() {
    }
    private void addSender(ActorRef actorRef, StatusMessage message){
        jobs.put(actorRef,message);
    }

    private void removeSender(ActorRef actorRef){
        jobs.remove(actorRef);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof StatusMessage) {
            StatusMessage statusMessage = (StatusMessage) message;
            if (statusMessage.isActive()) {
                addSender(getSender(),statusMessage);
                getContext().watch(getSender());
            } else {
                removeSender(getSender());
            }
        } else if (message instanceof String) {
            getSender().tell(getMessage((String)message),getSelf());
        }
    }

    private StatusMessage getMessage(String type) {
        Set<ActorRef> actors = jobs.keySet();
        StatusMessage msg = new StatusMessage(false);
        msg.setExists(false);
        for (ActorRef actor: actors) {
            if (actor.path().name().equals(type)) {
                return jobs.get(actor);
            }
        }
        return msg;
    }
}
