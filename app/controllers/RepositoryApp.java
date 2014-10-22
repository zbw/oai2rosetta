package controllers;

import actors.Jobstatus;
import actors.StatusMessage;
import models.Record;
import models.Repository;
import oai.*;
import play.data.Form;
import play.db.ebean.Model;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utils.Utils;
import views.html.monitor;
import views.html.records;

import java.util.*;

import static play.data.Form.form;
import static play.libs.Json.toJson;

/**
 * Created by Ott Konstantin on 21.08.2014.
 */

public class RepositoryApp extends Controller {

    public static Result list() {
        return ok(views.html.repositories.render(""));
    }

    @Security.Authenticated(Secured.class)
    public static Result edit(String id) {
        Repository repository = Repository.findById(id);
        Form<Repository> repoForm  = form(Repository.class).fill(repository);
        return ok(views.html.repository.render(repoForm));
    }
    @Security.Authenticated(Secured.class)
    public static Result delete(String id) {
        Repository repository = Repository.findById(id);
        repository.delete();
        return redirect(routes.RepositoryApp.list());
    }

    @Security.Authenticated(Secured.class)
    public static Result submit() {
        Form<Repository> repoForm  = form(Repository.class).bindFromRequest();
        if (repoForm.hasErrors()) {
            flash("error","Bitte korrekt ausfüllen.");
            return badRequest(views.html.repository.render(repoForm));
        }
        Repository repos = repoForm.get();
        repos.update();
        flash("success", String.format("%s erfolgreich gespeichert.",repos.title));
        return list();
    }

    @Security.Authenticated(Secured.class)
    public static Result addRepository() {
        Repository repository = Form.form(Repository.class).bindFromRequest().get();
        repository.save();
        return redirect(routes.RepositoryApp.list());
    }

    public static Result getRepositories() {
        List<Repository> repositories = new Model.Finder(String.class, Repository.class).all();
        return ok(toJson(repositories));
    }

    @Security.Authenticated(Secured.class)
    public static Result getRecords(String id) {
        Repository repository = Repository.findById(id);
        OAIClient oaiClient = new OAIClient(repository.oaiUrl);
        try {
            IdentifiersList identifiersList = oaiClient.listIdentifiers("oai_dc", null, null, id);
            ResumptionToken token = identifiersList.getResumptionToken();
            readList(identifiersList, repository);
            if (identifiersList.getResumptionToken() != null) {
                listIdentifiers(identifiersList.getResumptionToken(), repository, oaiClient);
            }
        } catch (OAIException e) {
            e.printStackTrace();
        }
        return redirect(routes.RecordApplication.list(id,0,null,null,null,-1));
    }

    private static void listIdentifiers( ResumptionToken resumptionToken,Repository repository,OAIClient oaiClient) throws OAIException {
        IdentifiersList identifiersList = oaiClient.listIdentifiers(resumptionToken);
        readList(identifiersList, repository);
        if (identifiersList.getResumptionToken() != null) {
           listIdentifiers(identifiersList.getResumptionToken(), repository, oaiClient);
        }
    }

    private static void readList(IdentifiersList identifiersList,Repository repository)  {
        LinkedList<Header> headerlist = (LinkedList<Header>) identifiersList.asList();
        for (int i = 0;i<headerlist.size();i++ ) {
            String ident = headerlist.get(i).getIdentifier();
            Record existrecord = Record.findByIdentifier(ident);
            if (existrecord == null) {
                Record record = new Record();
                record.identifier = ident;
                record.repository = repository;
                record.save();
            }
        }
    }

    public static Result showRepository(String id) {
        Repository repository = Repository.findById(id);
        int count = repository.records.size();
        return ok(records.render(repository));
    }

    @Security.Authenticated(Secured.class)
    public static Result monitor() {
        Vector<Jobstatus> jobs = Utils.getJobMessages();
        // get Stats
        Map<String,Map<String,Integer>> stats = new HashMap<>();
        List<Repository> repositories = new Model.Finder(String.class, Repository.class).all();
        for (Repository repository: repositories) {
            HashMap<String, Integer> reposStat = new HashMap<>();
            for (int i = 0; i < Record.ALLSTATUS.length; i++) {
                Integer count = repository.countStatus(Record.ALLSTATUS[i]);
                if (count>0)
                reposStat.put("" + Record.ALLSTATUS[i],count);
            }
            stats.put(repository.title,reposStat);
        }

        return ok(monitor.render(jobs,stats));
    }

    @Security.Authenticated(Secured.class)
    public static Result stop(String type) {
        Utils.stopJob(type);

        return ok();
    }

}
