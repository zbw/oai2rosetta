package controllers;

import actors.CommandMessage;
import actors.RootActor;
import actors.RootActorSystem;
import actors.StatusMessage;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import models.Record;
import models.Repository;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;
import views.html.index;
import views.html.recordlist;

import static play.libs.Json.toJson;

public class RecordApplication extends Controller {
    //static ActorSystem actorSystem = ActorSystem.create( "zbwSubApp" );
    static ActorSystem actorSystem = RootActorSystem.getInstance().getActorSystem();
    static {
        actorSystem.actorOf(Props.create(RootActor.class),"RootActor");
    }

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


    public static Result actorStatus(String identifier) {
        System.out.println("Statusmesssages ");
        StatusMessage status = Utils.getStatusMessage(StatusMessage.FETCHJOB);
        if (status.isExists())
        System.out.println("fetch "+ status);
        status = Utils.getStatusMessage(StatusMessage.CREATEJOB);
        if (status.isExists())
        System.out.println("create " +status);
        status = Utils.getStatusMessage(StatusMessage.PUSHJOB);
        if (status.isExists())
        System.out.println("push " +status);
        status = Utils.getStatusMessage(StatusMessage.DEPOSITJOB);
        if (status.isExists())
        System.out.println("deposit " +status);
        status = Utils.getStatusMessage(StatusMessage.SIPSTATUSJOB);
        if (status.isExists())
        System.out.println("sip " +status);
        return ok();
    }



    public static Result bfetchOAI(String identifier) {
        Repository repository = Repository.findById(identifier);
        CommandMessage msg = new CommandMessage(StatusMessage.FETCHJOB,true, identifier, repository.joblimit);
        startJob(msg);
        return ok(toJson(msg));
    }

    public static Result fetchOAI(String identifier)  {
        startJob(new CommandMessage(StatusMessage.FETCHJOB,false,identifier,0));
        return show(identifier);
    }

    public static Result createIE(String identifier) {
        startJob(new CommandMessage(StatusMessage.CREATEJOB,false,identifier,0));
        return show(identifier);
    }

    public static Result bcreateIE(String identifier) {
        Repository repository = Repository.findById(identifier);
        startJob(new CommandMessage(StatusMessage.CREATEJOB,true, identifier, repository.joblimit));

        return ok();
    }

    public static Result push(String identifier) {
        startJob(new CommandMessage(StatusMessage.PUSHJOB, false, identifier, 0));
        return show(identifier);
    }

    public static Result bpush(String identifier) {
        Repository repository = Repository.findById(identifier);
        startJob(new CommandMessage(StatusMessage.PUSHJOB, true, identifier, repository.joblimit));
        return ok();
    }

    public static Result deposit(String identifier) {
        startJob(new CommandMessage(StatusMessage.DEPOSITJOB, false, identifier, 0));
        return show(identifier);
    }

    public static Result bdeposit(String identifier) {
        Repository repository = Repository.findById(identifier);
        startJob(new CommandMessage(StatusMessage.DEPOSITJOB, true, identifier, repository.joblimit));
        return ok();
    }

    public static Result sipstatus(String identifier) {
        startJob(new CommandMessage(StatusMessage.SIPSTATUSJOB, false, identifier, 0));
        return show(identifier);
    }

    public static Result bsipstatus(String identifier) {
        Repository repository = Repository.findById(identifier);
        startJob(new CommandMessage(StatusMessage.SIPSTATUSJOB, true, identifier, repository.joblimit));
        return ok();
    }



    private static void startJob(CommandMessage msg) {
        StatusMessage status = Utils.getStatusMessage(msg.getCommand());
        if (!status.isActive()) {
            ActorSelection rootActor = actorSystem.actorSelection("user/RootActor");
            rootActor.tell(msg,null);
        } else {
            System.out.println(msg.getCommand()+ " still active");
        }
    }


}
