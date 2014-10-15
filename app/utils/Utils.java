package utils;

import actors.RootActorSystem;
import actors.StatusMessage;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

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
            e.printStackTrace();
            StatusMessage result = new StatusMessage(true);
            result.setError(e.getLocalizedMessage());
            return result;

        }
    }
}
