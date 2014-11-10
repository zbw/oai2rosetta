package controllers;

import actors.CommandMessage;
import actors.RootActorSystem;
import actors.StatusMessage;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import models.Record;
import models.Repository;
import models.Resource;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.index;
import views.html.recordlist;

import java.util.List;

import static play.data.Form.form;

public class RecordApplication extends Controller {
    //static ActorSystem actorSystem = ActorSystem.create( "zbwSubApp" );
    static ActorSystem actorSystem = RootActorSystem.getInstance().getActorSystem();


    public static Result index() {
        return ok(index.render("ZBW Hosting subapps"));
    }

    public static Result show(int id) {
        Record record = Record.findById(id);
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
    public static Result list(int id, int page, String sortBy, String order, String filter, int status) {
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
    public static Result reset(int id) {
        resetRecord(id);
        return show(id);
    }

    public static Result resetStatus() {
        DynamicForm dynamicForm = form().bindFromRequest();
        int repository_id = Integer.parseInt(dynamicForm.get("repository_id"));
        int status = Integer.parseInt(dynamicForm.get("status"));
        List<Record> records = Record.limit(repository_id,status, 100);
        for (Record record:records) {
            List<Resource> resources =record.getResources();
            for (Resource resource: resources) {
                resource.delete();
            }
            record.getResources().clear();
            record.status = Record.STATUSNEW;
            record.errormsg = "";
            record.logcreated = null;
            record.save();
        }
        return redirect(routes.RepositoryApp.getRecords(repository_id));
    }

    private static void resetRecord(int id) {
        Record record = Record.findById(id);
        if (record.status < Record.STATUSINGESTED || record.sipActive.equals("DECLINED")) {
            List<Resource> resources =record.getResources();
            for (Resource resource: resources) {
                resource.delete();
            }
            record.getResources().clear();
            record.status = Record.STATUSNEW;
            record.save();
        }
    }



    @Security.Authenticated(Secured.class)
    public static Result bfetchOAI(int repository_id) {
        Repository repository = Repository.findById(repository_id);
        List<Record> records = Record.limit(repository_id, Record.STATUSNEW, repository.joblimit);
        for (Record record : records) {
            CommandMessage msg = new CommandMessage(StatusMessage.FETCHJOB,false, record.recordId,0);
            //startJob(msg);
            ActorSelection rootActor = actorSystem.actorSelection("user/RootFetchActor");
            rootActor.tell(msg,null);
        }
        return ok();
    }


    @Security.Authenticated(Secured.class)
    public static Result fetchOAI(int id)  {
        startJob(new CommandMessage(StatusMessage.FETCHJOB,false,id,0));
        return show(id);
    }

    @Security.Authenticated(Secured.class)
    public static Result createIE(int id) {
        startJob(new CommandMessage(StatusMessage.CREATEJOB,false,id,0));
        return show(id);
    }

    @Security.Authenticated(Secured.class)
    public static Result bcreateIE(int identifier) {
        Repository repository = Repository.findById(identifier);
        List<Record> records = Record.limit(identifier, Record.STATUSIMPORTED, repository.joblimit);
        for (Record record : records) {
            CommandMessage msg = new CommandMessage(StatusMessage.CREATEJOB,false, record.recordId,0);
            //startJob(msg);
            ActorSelection rootActor = actorSystem.actorSelection("user/RootCreateIEActor");
            rootActor.tell(msg,null);
        }
        return ok();
    }

    @Security.Authenticated(Secured.class)
    public static Result push(int identifier) {
        startJob(new CommandMessage(StatusMessage.PUSHJOB, false, identifier, 0));
        return show(identifier);
    }

    @Security.Authenticated(Secured.class)
    public static Result bpush(int identifier) {
        Repository repository = Repository.findById(identifier);
        List<Record> records = Record.limit(identifier, Record.STATUSIECREATED, repository.joblimit);
        for (Record record : records) {
            CommandMessage msg = new CommandMessage(StatusMessage.PUSHJOB,false, record.recordId,0);
            //startJob(msg);
            ActorSelection rootActor = actorSystem.actorSelection("user/RootPushActor");
            rootActor.tell(msg,null);
        }
        return ok();
    }

    @Security.Authenticated(Secured.class)
    public static Result deposit(int identifier) {
        startJob(new CommandMessage(StatusMessage.DEPOSITJOB, false, identifier, 0));
        return show(identifier);
    }

    @Security.Authenticated(Secured.class)
    public static Result bdeposit(int identifier) {
        Repository repository = Repository.findById(identifier);
        List<Record> records = Record.limit(identifier, Record.STATUSEXPORTED, repository.joblimit);
        for (Record record : records) {
            CommandMessage msg = new CommandMessage(StatusMessage.DEPOSITJOB,false, record.recordId,0);
            //startJob(msg);
            ActorSelection rootActor = actorSystem.actorSelection("user/RootDepositActor");
            rootActor.tell(msg,null);
        }
        return ok();
    }

    @Security.Authenticated(Secured.class)
    public static Result sipstatus(int identifier) {
        startJob(new CommandMessage(StatusMessage.SIPSTATUSJOB, false, identifier, 0));
        return show(identifier);
    }

    @Security.Authenticated(Secured.class)
    public static Result bsipstatus(int identifier) {
        Repository repository = Repository.findById(identifier);
        List<Record> records = Record.limit(identifier, Record.STATUSINGESTED, repository.joblimit);
        for (Record record : records) {
            CommandMessage msg = new CommandMessage(StatusMessage.SIPSTATUSJOB,false, record.recordId,0);
            //startJob(msg);
            ActorSelection rootActor = actorSystem.actorSelection("user/RootStatusActor");
            rootActor.tell(msg,null);
        }
        return ok();
    }




    private static void startJob(CommandMessage msg) {
        ActorSelection rootActor = actorSystem.actorSelection("user/RootActor");
        rootActor.tell(msg,null);
    }


}
