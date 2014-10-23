package controllers;

import actors.CommandMessage;
import actors.RootActorSystem;
import actors.StatusMessage;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import models.Record;
import models.Repository;
import models.Resource;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.index;
import views.html.recordlist;

import java.util.List;

public class RecordApplication extends Controller {
    //static ActorSystem actorSystem = ActorSystem.create( "zbwSubApp" );
    static ActorSystem actorSystem = RootActorSystem.getInstance().getActorSystem();


    public static Result index() {
        return ok(index.render("ZBW Hosting subapps"));
    }

    public static Result show(String identifier) {
        Record record = Record.findByIdentifier(identifier);
        return ok(views.html.record.render(record));
    }

    /**
     * Display the paginated list of records.
     *
     * @param page Current page number (starts from 0)
     * @param sortBy Column to be sorted
     * @param order Sort order (either asc or desc)
     * @param filter Filter applied on record titles
     */
    @Security.Authenticated(Secured.class)
    public static Result list(String id, int page, String sortBy, String order, String filter, int status) {
      Repository repository = Repository.findById(id);
      return ok(
              recordlist.render(
                      Record.page(repository, page, 10, sortBy, order, filter, status),
                      sortBy,
                      order,
                      filter,
                      status,
                      repository
              )
      );
    }

    @Security.Authenticated(Secured.class)
    public static Result reset(String id) {
        Record record = Record.findByIdentifier(id);
        if (record.status < Record.STATUSINGESTED) {
            List<Resource> resources =record.getResources();
            for (Resource resource: resources) {
                resource.delete();
            }
            record.getResources().clear();
            record.status = Record.STATUSNEW;
            record.save();
        }
        return show(id);
    }



    @Security.Authenticated(Secured.class)
    public static Result bfetchOAI(String identifier) {
        Repository repository = Repository.findById(identifier);
        List<Record> records = Record.limit(identifier, Record.STATUSNEW, repository.joblimit);
        for (Record record : records) {
            CommandMessage msg = new CommandMessage(StatusMessage.FETCHJOB,false, record.identifier,0);
            startJob(msg);
        }
        return ok();
    }


    @Security.Authenticated(Secured.class)
    public static Result fetchOAI(String identifier)  {
        startJob(new CommandMessage(StatusMessage.FETCHJOB,false,identifier,0));
        return show(identifier);
    }

    @Security.Authenticated(Secured.class)
    public static Result createIE(String identifier) {
        startJob(new CommandMessage(StatusMessage.CREATEJOB,false,identifier,0));
        return show(identifier);
    }

    @Security.Authenticated(Secured.class)
    public static Result bcreateIE(String identifier) {
        Repository repository = Repository.findById(identifier);
        List<Record> records = Record.limit(identifier, Record.STATUSIMPORTED, repository.joblimit);
        for (Record record : records) {
            CommandMessage msg = new CommandMessage(StatusMessage.CREATEJOB,false, record.identifier,0);
            startJob(msg);
        }
        return ok();
    }

    @Security.Authenticated(Secured.class)
    public static Result push(String identifier) {
        startJob(new CommandMessage(StatusMessage.PUSHJOB, false, identifier, 0));
        return show(identifier);
    }

    @Security.Authenticated(Secured.class)
    public static Result bpush(String identifier) {
        Repository repository = Repository.findById(identifier);
        List<Record> records = Record.limit(identifier, Record.STATUSIECREATED, repository.joblimit);
        for (Record record : records) {
            CommandMessage msg = new CommandMessage(StatusMessage.PUSHJOB,false, record.identifier,0);
            startJob(msg);
        }
        return ok();
    }

    @Security.Authenticated(Secured.class)
    public static Result deposit(String identifier) {
        startJob(new CommandMessage(StatusMessage.DEPOSITJOB, false, identifier, 0));
        return show(identifier);
    }

    @Security.Authenticated(Secured.class)
    public static Result bdeposit(String identifier) {
        Repository repository = Repository.findById(identifier);
        List<Record> records = Record.limit(identifier, Record.STATUSEXPORTED, repository.joblimit);
        for (Record record : records) {
            CommandMessage msg = new CommandMessage(StatusMessage.DEPOSITJOB,false, record.identifier,0);
            startJob(msg);
        }
        return ok();
    }

    @Security.Authenticated(Secured.class)
    public static Result sipstatus(String identifier) {
        startJob(new CommandMessage(StatusMessage.SIPSTATUSJOB, false, identifier, 0));
        return show(identifier);
    }

    @Security.Authenticated(Secured.class)
    public static Result bsipstatus(String identifier) {
        Repository repository = Repository.findById(identifier);
        List<Record> records = Record.limit(identifier, Record.STATUSINGESTED, repository.joblimit);
        for (Record record : records) {
            CommandMessage msg = new CommandMessage(StatusMessage.SIPSTATUSJOB,false, record.identifier,0);
            startJob(msg);
        }
        return ok();
    }




    private static void startJob(CommandMessage msg) {
        ActorSelection rootActor = actorSystem.actorSelection("user/RootActor");
        rootActor.tell(msg,null);
    }


}
