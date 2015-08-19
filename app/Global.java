/**
 * Created by Ott Konstantin on 25.09.2014.
 */

import actors.*;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.avaje.ebean.Ebean;
import models.Record;
import models.Repository;
import models.User;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.db.ebean.Model;
import play.libs.Akka;
import play.libs.Yaml;
import scala.concurrent.duration.Duration;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Global extends GlobalSettings {

    static ActorSystem actorSystem = RootActorSystem.getInstance().getActorSystem();
    static {
        actorSystem.actorOf(Props.create(RootActor.class),"RootActor");
        actorSystem.actorOf(Props.create(RootActor.class),"RootStatusActor");
        actorSystem.actorOf(Props.create(RootActor.class),"RootFetchActor");
        actorSystem.actorOf(Props.create(RootActor.class),"RootCreateIEActor");
        actorSystem.actorOf(Props.create(RootActor.class),"RootPushActor");
        actorSystem.actorOf(Props.create(RootActor.class),"RootDepositActor");
        actorSystem.actorOf(Props.create(RootActor.class),"RootCleanupActor");
        actorSystem.actorOf(Props.create(RootActor.class),"RootGetRecordActor");
    }

    @Override
    public void onStart(Application app) {
        Logger.info("Application has started");
        ActorRef myActor = Akka.system().actorOf(Props.create(TestActor.class));
        Runnable showTime = new Runnable() {
            @Override
            public void run() {
                System.out.println("Time is now: " + new Date());
            }
        };

        if (User.find.findRowCount() == 0) {
            Map users = (Map) Yaml.load("initial-user.yml");
            System.out.println(users);
            Ebean.save((Collection) (users.get("users")));
            Ebean.save(users.get("users"));
        }


        // schedule tasks
        scheduleStatus();
        scheduleFetch();
        scheduleCreate();
        schedulePush();
        scheduleDeposit();

    }

    // Fetch Schedules
    private void scheduleGetRecords() {
        Akka.system().scheduler().schedule(
                Duration.create(nextExecutionInSeconds(23, 00), TimeUnit.SECONDS),
                Duration.create(24, TimeUnit.HOURS),
                new Runnable() {
                    @Override
                    public void run() {
                        Logger.info("GetRecords Schedule---    " + new Date());
                        runGetRecords();
                    }
                },
                Akka.system().dispatcher()
        );
    }

    private void runGetRecords() {
        List<Repository> repositories = new Model.Finder(String.class, Repository.class).all();
        ActorSelection rootActor = actorSystem.actorSelection("user/RootActor");
        for (Repository repository:repositories) {
            if (repository.active) {
                CommandMessage msg =  new CommandMessage(StatusMessage.GETRECORDJOB,false, repository.repository_id,0);
                rootActor.tell(msg, null);
                Logger.info("repository: " + repository.title + " getting records");
            } else {
                Logger.info("repository: " + repository.title + " not active");
            }
        }
    }

    // Fetch Schedules
    private void scheduleFetch() {
        Akka.system().scheduler().schedule(
                Duration.create(nextExecutionInSeconds(23, 59), TimeUnit.SECONDS),
                Duration.create(24, TimeUnit.HOURS),
                new Runnable() {
                    @Override
                    public void run() {
                        Logger.info("Fetch Schedule---    " + new Date());
                        runFetch();
                    }
                },
                Akka.system().dispatcher()
        );
    }
    private void runFetch() {
        List<Repository> repositories = new Model.Finder(String.class, Repository.class).all();
        ActorSelection rootActor = actorSystem.actorSelection("user/RootActor");
        for (Repository repository:repositories) {
            if (repository.active) {
                List<Record> records = Record.limit(repository.repository_id, Record.STATUSNEW, repository.joblimit);
                for (Record record : records) {
                    CommandMessage msg = new CommandMessage(StatusMessage.FETCHJOB, false, record.recordId, 0);
                    Logger.info("fetching record: " + record.identifier);
                    rootActor.tell(msg, null);
                }
                Logger.info("fetched: " + records.size() + " records");
            } else {
                Logger.info("repository: " + repository.title + " not active");
            }
        }
    }



    // Create Schedules
    private void scheduleCreate() {
        Akka.system().scheduler().schedule(
                Duration.create(nextExecutionInSeconds(1, 00), TimeUnit.SECONDS),
                Duration.create(24, TimeUnit.HOURS),
                new Runnable() {
                    @Override
                    public void run() {
                        Logger.info("Create Schedule ---    " + new Date());
                        runCreate();
                    }
                },
                Akka.system().dispatcher()
        );
    }
    private void runCreate() {
        List<Repository> repositories = new Model.Finder(String.class, Repository.class).all();
        ActorSelection rootActor = actorSystem.actorSelection("user/RootActor");
        for (Repository repository:repositories) {
            if (repository.active) {
                List<Record> records = Record.limit(repository.repository_id, Record.STATUSIMPORTED, repository.joblimit);
                for (Record record : records) {
                    CommandMessage msg = new CommandMessage(StatusMessage.CREATEJOB, false, record.recordId, 0);
                    Logger.info("creating ie record: " + record.identifier);
                    rootActor.tell(msg, null);
                }
                Logger.info("created: " + records.size() + " records");
            } else {
                Logger.info("repository: " + repository.title + " not active");
            }
        }
    }

    // Push Schedules
    private void schedulePush() {
        Akka.system().scheduler().schedule(
                Duration.create(nextExecutionInSeconds(3, 00), TimeUnit.SECONDS),
                Duration.create(24, TimeUnit.HOURS),
                new Runnable() {
                    @Override
                    public void run() {
                        Logger.info("Push Schedule ---    " + new Date());
                        runPush();
                    }
                },
                Akka.system().dispatcher()
        );
    }
    private void runPush() {
        List<Repository> repositories = new Model.Finder(String.class, Repository.class).all();
        ActorSelection rootActor = actorSystem.actorSelection("user/RootActor");
        for (Repository repository:repositories) {
            if (repository.active) {
                List<Record> records = Record.limit(repository.repository_id, Record.STATUSIECREATED, repository.joblimit);
                for (Record record : records) {
                    CommandMessage msg = new CommandMessage(StatusMessage.PUSHJOB, false, record.recordId, 0);
                    rootActor.tell(msg, null);
                }
                Logger.info("pushed: " + records.size() + " records");
            } else {
                Logger.info("repository: " + repository.title + " not active");
            }
        }
    }

    // Deposit Schedules
    private void scheduleDeposit() {
        Akka.system().scheduler().schedule(
                Duration.create(nextExecutionInSeconds(4, 57), TimeUnit.SECONDS),
                Duration.create(24, TimeUnit.HOURS),
                new Runnable() {
                    @Override
                    public void run() {
                        Logger.info("Deposit Schedule ---    " + new Date());
                        runDeposit();
                    }
                },
                Akka.system().dispatcher()
        );
    }
    private void runDeposit() {
        List<Repository> repositories = new Model.Finder(String.class, Repository.class).all();
        ActorSelection rootActor = actorSystem.actorSelection("user/RootActor");
        for (Repository repository:repositories) {
            if (repository.active) {
                List<Record> records = Record.limit(repository.repository_id, Record.STATUSEXPORTED, repository.joblimit);
                for (Record record : records) {
                    CommandMessage msg = new CommandMessage(StatusMessage.DEPOSITJOB, false, record.recordId, 0);
                    rootActor.tell(msg, null);
                }
                Logger.info("deposited: " + records.size() + " records");
            } else {
                Logger.info("repository: " + repository.title + " not active");
            }
        }
    }

    // Status Schedules
    private void scheduleStatus() {
        Akka.system().scheduler().schedule(
                Duration.create(0, TimeUnit.SECONDS),
                Duration.create(30, TimeUnit.MINUTES),
                new Runnable() {
                    @Override
                    public void run() {
                        Logger.info("Status Schedule ---    " + new Date());
                        runStatus();
                    }
                },
                Akka.system().dispatcher()
        );
    }
    private void runStatus() {
        List<Repository> repositories = new Model.Finder(String.class, Repository.class).all();
        ActorSelection rootActor = actorSystem.actorSelection("user/RootActor");
        for (Repository repository:repositories) {
            if (repository.active) {
                List<Record> records = Record.limit(repository.repository_id, Record.STATUSINGESTED, repository.joblimit);
                for (Record record : records) {
                    CommandMessage msg = new CommandMessage(StatusMessage.SIPSTATUSJOB, false, record.recordId, 0);
                    msg.setThreadcount(50);
                    rootActor.tell(msg, null);
                }
                Logger.info("checked status for: " + records.size() + " records");
            } else {
                Logger.info("repository: " + repository.title + " not active");
            }
        }
    }
    // Duration Helpers
    private int nextExecutionInSeconds(int hour, int minute){
        return Seconds.secondsBetween(
                new DateTime(),
                nextExecution(hour, minute)
        ).getSeconds();
    }

   private DateTime nextExecution(int hour, int minute){
        DateTime next = new DateTime()
                .withHourOfDay(hour)
                .withMinuteOfHour(minute)
                .withSecondOfMinute(0)
                .withMillisOfSecond(0);

        return (next.isBeforeNow())
                ? next.plusHours(24)
                : next;
    }

}