package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.RoundRobinPool;
import play.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * Created by Ott Konstantin on 08.10.2014.
 */
public class RootActor extends UntypedActor {
    static ActorSystem actorSystem = RootActorSystem.getInstance().getActorSystem();
    Map<String,Class> classmap = new HashMap<String, Class>();
    Map<String, ActorRef> children = new HashMap<String, ActorRef>();
    Vector<Jobstatus> jobs = new Vector<>();

    @Override
    public void preStart() {
        classmap.put(StatusMessage.FETCHJOB, FetchActor.class);
        classmap.put(StatusMessage.CREATEJOB, CreateIEActor.class);
        classmap.put(StatusMessage.PUSHJOB, PushActor.class);
        classmap.put(StatusMessage.DEPOSITJOB, DepositActor.class);
        classmap.put(StatusMessage.SIPSTATUSJOB, SipStatusActor.class);
        classmap.put(StatusMessage.CLEANUPJOB, CleanupActor.class);
    }
    @Override
    public void onReceive(Object message) throws Exception {
        ActorRef sender = getSender();
        if (message instanceof CommandMessage) {
            CommandMessage cmd = (CommandMessage) message;
            startCommand(cmd);
        } else if (message instanceof StatusMessage) {
            StatusMessage msg = (StatusMessage) message;
            Jobstatus job = getJob(msg.getType());
            if (job == null) {
                job = new Jobstatus();
                job.setType(msg.getType());
                jobs.add(job);
            }
            if (msg.isActive()) {
                job.setWorker(job.getWorker() +1);
            } else {
                if (job.getWorker()>0) {
                    job.setWorker(job.getWorker() - 1);
                }
                job.setCount(job.getCount() + 1);
            }


        } else if (message instanceof String) {
            String msg = (String) message;
            if (msg.equals("monitor")) {
                Iterator iter = getContext().getChildren().iterator();
                while (iter.hasNext()) {
                    ActorRef actor = (ActorRef) iter.next();
                    Logger.debug("Monitoring Call ("+self().path()+"): "+actor.path() + " - terminated: " +actor.isTerminated());
                }
                Logger.debug("Monitoring Call ("+self().path()+"): jobs: " + jobs.size());
                for (Jobstatus job : jobs) {
                   Logger.debug("Monitoring Call ("+self().path()+"): Job Type: "+ job.getType() + " worker: " + job.getWorker() + " count: " + job.getCount());
                }
                sender().tell(jobs,getSelf());
            }
        } else {
            unhandled(message);
        }
    }

    private void startCommand(CommandMessage cmd) {
        if (classmap.containsKey(cmd.getCommand()))  {
            ActorRef childActor = children.get(cmd.getCommand());
            if (childActor == null || childActor.isTerminated()) {
                childActor = createChild(cmd.getThreadcount(), cmd.getCommand(), classmap.get(cmd.getCommand()));
                children.put(cmd.getCommand(), childActor);
            }
            childActor.tell(cmd.getMessage(), self());
        } else {
            Logger.info(cmd.getCommand() + " not implemented");
        }
    }



    private Jobstatus getJob(String type) {
        for (Jobstatus job : jobs) {
            if (job.getType().equals(type)) {
                return job;
            }
        }
        return null;
    }

    private ActorRef createChild(int pool, String name, Class actorclass) {
        return getContext().actorOf(new RoundRobinPool(pool).props(Props.create(actorclass)),name);
    }



}
