package utils;

import actors.CommandMessage;
import actors.Jobstatus;
import actors.RootActorSystem;
import actors.StatusMessage;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.util.Timeout;
import play.Logger;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.ArrayList;
import java.util.Vector;

import static akka.pattern.Patterns.ask;

/**
 * Created by Ott Konstantin on 09.10.2014.
 */
public class Utils {

    static ActorSystem actorSystem = RootActorSystem.getInstance().getActorSystem();

    public static StatusMessage getStatusMessage(String jobtype) {
        Timeout timeout = new Timeout(Duration.create(10, "seconds"));
        ActorSelection monitor = actorSystem.actorSelection("/user/MonitorActor");
        Future future = ask(monitor,jobtype,timeout);

        //Abfragen
        try {
            StatusMessage result = (StatusMessage) Await.result(future, timeout.duration());
            return result;
        } catch (Exception e) {
            Logger.error(e.getMessage());
            StatusMessage result = new StatusMessage(true);
            result.setError(e.getLocalizedMessage());
            return result;

        }
    }

    public static ArrayList<StatusMessage> getStatusMessages() {
        Timeout timeout = new Timeout(Duration.create(10, "seconds"));
        ActorSelection monitor = actorSystem.actorSelection("/user/MonitorActor");
        Future future = ask(monitor,"monitor",timeout);

        //Abfragen
        try {
            ArrayList<StatusMessage> result = (ArrayList<StatusMessage>) Await.result(future, timeout.duration());
            return result;
        } catch (Exception e) {
            Logger.error(e.getMessage());

            return new ArrayList<>();

        }
    }

    public static Vector<Jobstatus> getJobMessages() {
        Timeout timeout = new Timeout(Duration.create(10, "seconds"));
        ActorSelection monitor = actorSystem.actorSelection("user/RootStatusActor");
        Future future = ask(monitor,"monitor",timeout);

        //Abfragen
        try {
            Vector<Jobstatus> result = (Vector<Jobstatus>) Await.result(future, timeout.duration());
            return result;
        } catch (Exception e) {
            Logger.error(e.getMessage());

            return new Vector();

        }
    }

    public static void stopJob(String type) {
        ActorSelection monitor = actorSystem.actorSelection("user/RootActor");
        CommandMessage msg = new CommandMessage(type,false,null, 0);
        monitor.tell(msg,null);
    }
}
