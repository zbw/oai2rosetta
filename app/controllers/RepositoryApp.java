package controllers;

import actors.StatusMessage;
import models.Record;
import models.Repository;

import oai.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import play.data.Form;
import play.db.ebean.Model;
import play.mvc.*;

import static play.data.Form.form;
import static play.libs.Json.toJson;

import utils.Utils;
import views.html.monitor;
import views.html.records;
import views.html.repository;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Ott Konstantin on 21.08.2014.
 */
public class RepositoryApp extends Controller {

    public static Result list() {
        return ok(views.html.repositories.render(""));
    }

    public static Result edit(String id) {
        Repository repository = Repository.findById(id);
        Form<Repository> repoForm  = form(Repository.class).fill(repository);
        return ok(views.html.repository.render(repoForm));
    }

    public static Result delete(String id) {
        Repository repository = Repository.findById(id);
        repository.delete();
        return redirect(routes.RepositoryApp.list());
    }

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
    public static Result addRepository() {
        Repository repository = Form.form(Repository.class).bindFromRequest().get();
        repository.save();
        return redirect(routes.RepositoryApp.list());
    }

    public static Result getRepositories() {
        List<Repository> repositories = new Model.Finder(String.class, Repository.class).all();
        return ok(toJson(repositories));
    }

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

    public static Result monitor() {
        java.util.Set<StatusMessage> statusMessages = new HashSet<>();
        statusMessages.add(Utils.getStatusMessage(StatusMessage.FETCHJOB));
        statusMessages.add(Utils.getStatusMessage(StatusMessage.CREATEJOB));
        statusMessages.add(Utils.getStatusMessage(StatusMessage.PUSHJOB));
        statusMessages.add(Utils.getStatusMessage(StatusMessage.DEPOSITJOB));
        statusMessages.add(Utils.getStatusMessage(StatusMessage.SIPSTATUSJOB));

        return ok(monitor.render(statusMessages));
    }
}
