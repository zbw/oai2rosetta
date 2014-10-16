/**
 * Created by Ott Konstantin on 25.09.2014.
 */

import actors.TestActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.avaje.ebean.Ebean;
import models.User;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.libs.Akka;
import play.libs.Yaml;
import scala.concurrent.duration.FiniteDuration;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Global extends GlobalSettings {



    @Override
    public void onStart(Application app) {
        Logger.info("Application has started");
        FiniteDuration delay = FiniteDuration.create(0, TimeUnit.SECONDS);
        FiniteDuration frequency = FiniteDuration.create(5, TimeUnit.SECONDS);
        ActorRef myActor = Akka.system().actorOf(Props.create(TestActor.class));
        Runnable showTime = new Runnable() {
            @Override
            public void run() {
                System.out.println("Time is now: " + new Date());
            }
        };

        if (User.find.findRowCount() == 0) {
            Map users =   (Map) Yaml.load("initial-user.yml");
            System.out.println(users);
            Ebean.save((Collection) (users.get("users")));
            Ebean.save(users.get("users"));
        }


        Akka.system().scheduler().schedule(
                delay,
                frequency,
                myActor,
                "test",
                Akka.system().dispatcher(),
                null);
    }

}