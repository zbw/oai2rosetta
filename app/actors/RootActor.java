package actors;

import akka.actor.*;

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

    @Override
    public void preStart() {
        fetchActor =        actorSystem.actorOf(Props.create(FetchActor.class),"FetchActor");
        createActor =       actorSystem.actorOf(Props.create(CreateIEActor.class),"CreateIEActor");
        pushActor =         actorSystem.actorOf(Props.create(PushActor.class),"PushActor");
        depositActor =      actorSystem.actorOf(Props.create(DepositActor.class),"DepositActor");
        sipStatusActor =    actorSystem.actorOf(Props.create(SipStatusActor.class),"SipStatusActor");
    }
    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof CommandMessage) {
            CommandMessage cmd = (CommandMessage) message;
            if (cmd.getCommand().equals(StatusMessage.FETCHJOB)) {
                fetchActor.tell(cmd.getMessage(),self());
            } else if (cmd.getCommand().equals(StatusMessage.CREATEJOB)) {
                createActor.tell(cmd.getMessage(),self());
            } else if (cmd.getCommand().equals(StatusMessage.PUSHJOB)) {
                pushActor.tell(cmd.getMessage(),self());
            } else if (cmd.getCommand().equals(StatusMessage.DEPOSITJOB)) {
                depositActor.tell(cmd.getMessage(),self());
            } else if (cmd.getCommand().equals(StatusMessage.SIPSTATUSJOB)) {
                sipStatusActor.tell(cmd.getMessage(),self());
            } else {
                 System.out.println(cmd.getCommand() + " not implemented");
            }
        } else {
            unhandled(message);
        }
    }
}
