package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.RoundRobinPool;
import play.Logger;

import java.util.Iterator;
import java.util.Vector;

/**
 * Created by Ott Konstantin on 08.10.2014.
 */
public class RootActor extends UntypedActor {
    static ActorSystem actorSystem = RootActorSystem.getInstance().getActorSystem();
    ActorRef fetchActor = null;
    ActorRef createActor = null;
    ActorRef pushActor = null;
    ActorRef depositActor = null;
    ActorRef sipStatusActor = null;
    Vector<Jobstatus> jobs = new Vector<>();

    @Override
    public void preStart() {
        //fetchActor =        actorSystem.actorOf(Props.create(FetchActor.class),"FetchActor");
        //createActor =       actorSystem.actorOf(Props.create(CreateIEActor.class),"CreateIEActor");
        //pushActor =         actorSystem.actorOf(Props.create(PushActor.class),"PushActor");
        //depositActor =      actorSystem.actorOf(Props.create(DepositActor.class),"DepositActor");
        //sipStatusActor =    actorSystem.actorOf(Props.create(SipStatusActor.class),"SipStatusActor");
        fetchActor = getContext().actorOf(new RoundRobinPool(5).props(Props.create(FetchActor.class)),
                "fetchrouter");
        createActor = getContext().actorOf(new RoundRobinPool(3).props(Props.create(CreateIEActor.class)),
                "createrouter");
        pushActor = getContext().actorOf(new RoundRobinPool(5).props(Props.create(PushActor.class)),
                "pushrouter");
        depositActor = getContext().actorOf(new RoundRobinPool(5).props(Props.create(DepositActor.class)),
                "depositrouter");
        sipStatusActor = getContext().actorOf(new RoundRobinPool(5).props(Props.create(SipStatusActor.class)),
                "siprouter");
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
                    //System.out.println(actor.path() + " - terminated: " +actor.isTerminated());

                }
                sender().tell(jobs,getSelf());
            }
        } else {
            unhandled(message);
        }
    }

    private void startCommand(CommandMessage cmd) {
        if (cmd.getCommand().equals(StatusMessage.FETCHJOB)) {
            tellActor(fetchActor,cmd,5,"fetchrouter", FetchActor.class);
        } else if (cmd.getCommand().equals(StatusMessage.CREATEJOB)) {
            tellActor(createActor, cmd, 3, "createrouter", CreateIEActor.class);
        } else if (cmd.getCommand().equals(StatusMessage.PUSHJOB)) {
            tellActor(pushActor, cmd, 5, "pushrouter", PushActor.class);
        } else if (cmd.getCommand().equals(StatusMessage.DEPOSITJOB)) {
            tellActor(depositActor,cmd,5,"depositrouter", DepositActor.class);
        } else if (cmd.getCommand().equals(StatusMessage.SIPSTATUSJOB)) {
            tellActor(sipStatusActor,cmd,5,"siprouter", SipStatusActor.class);
        } else if (cmd.getCommand().equals("stop"+StatusMessage.FETCHJOB)) {
            actorSystem.stop(fetchActor);
        } else if (cmd.getCommand().equals("stop"+StatusMessage.CREATEJOB)) {
            actorSystem.stop(createActor);
        } else if (cmd.getCommand().equals("stop"+StatusMessage.PUSHJOB)) {
            actorSystem.stop(pushActor);
        } else if (cmd.getCommand().equals("stop"+StatusMessage.DEPOSITJOB)) {
            actorSystem.stop(depositActor);
        } else if (cmd.getCommand().equals("stop"+StatusMessage.SIPSTATUSJOB)) {
            actorSystem.stop(sipStatusActor);
        } else {
            Logger.info(cmd.getCommand() + " not implemented");
        }
    }

    private void tellActor(ActorRef actor, CommandMessage msg,int pool, String name, Class actorclass) {
        if (actor.isTerminated()) {
            Jobstatus job = getJob(msg.getCommand());
            actor = getContext().actorOf(new RoundRobinPool(pool).props(Props.create(actorclass)),name);
        }
        actor.tell(msg.getMessage(),self());
    }

    private Jobstatus getJob(String type) {
        for (Jobstatus job : jobs) {
            if (job.getType().equals(type)) {
                return job;
            }
        }
        return null;
    }



}
