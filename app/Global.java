/**
 * Created by Ott Konstantin on 25.09.2014.
 */

import actors.TestActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import play.Application;
import play.GlobalSettings;
import play.libs.Akka;
import scala.concurrent.duration.FiniteDuration;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Global extends GlobalSettings {



    @Override
    public void onStart(Application app) {

        FiniteDuration delay = FiniteDuration.create(0, TimeUnit.SECONDS);
        FiniteDuration frequency = FiniteDuration.create(5, TimeUnit.SECONDS);
        ActorRef myActor = Akka.system().actorOf(Props.create(TestActor.class));
        Runnable showTime = new Runnable() {
            @Override
            public void run() {
                System.out.println("Time is now: " + new Date());
            }
        };



        Akka.system().scheduler().schedule(
                delay,
                frequency,
                myActor,
                "test",
                Akka.system().dispatcher(),
                null);
    }

}