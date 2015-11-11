package actors;

import akka.actor.UntypedActor;
import models.Repository;
import oai.*;
import play.Logger;

import java.util.Date;
import java.util.LinkedList;

/**
 * Created by Ott Konstantin on 25.09.2014.
 */
public class GetRecordActor extends UntypedActor {



    @Override
    public void onReceive(Object message) throws Exception {
        StatusMessage statusMessage = new StatusMessage();
        statusMessage.setType(StatusMessage.GETRECORDJOB);
        statusMessage.setCount(0);
        statusMessage.setStatus("Started");
        statusMessage.setStarted(new Date());
        int count = 1;
        if (message instanceof Message) {
            statusMessage.setActive(true);
            Message myMessage = (Message) message;
            int identifier = myMessage.getId();
            statusMessage.setStatus("Running");
            statusMessage.setCount(count);
            getSender().tell(statusMessage, getSelf());
            getRecords(identifier);
            statusMessage.setActive(false);
            statusMessage.setStatus("Finished");
            statusMessage.setFinished(new Date());
            getSender().tell(statusMessage, getSelf());
        } else if (message instanceof StatusMessage){
            getSender().tell(statusMessage,getSelf());

        } else {
            unhandled(message);
        }
    }

    private void getRecords(int identifier) {
        Repository repository = Repository.findById(identifier);
        String set = repository.id;
        if (set.equals("root")) {set=null;}
        OAIClient oaiClient = new OAIClient(repository.oaiUrl);
        try {
            IdentifiersList identifiersList = oaiClient.listIdentifiers("oai_dc", null, null, set);
            ResumptionToken token = identifiersList.getResumptionToken();
            readList(identifiersList, repository);
            if (identifiersList.getResumptionToken() != null) {
                listIdentifiers(identifiersList.getResumptionToken(), repository, oaiClient);
            }
        } catch (OAIException e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
        }
    }

    private void listIdentifiers( ResumptionToken resumptionToken,Repository repository,OAIClient oaiClient) throws OAIException {
        IdentifiersList identifiersList = oaiClient.listIdentifiers(resumptionToken);
        readList(identifiersList, repository);
        if (identifiersList.getResumptionToken() != null) {
            listIdentifiers(identifiersList.getResumptionToken(), repository, oaiClient);
        }
    }

    private void readList(IdentifiersList identifiersList,Repository repository) {
        LinkedList<Header> headerlist = (LinkedList<Header>) identifiersList.asList();
        for (int i = 0; i < headerlist.size(); i++) {
            String ident = headerlist.get(i).getIdentifier();
            models.Record existrecord = models.Record.findByIdentifierAndRepos(ident, repository.repository_id);
            if (existrecord == null) {
                models.Record record = new models.Record();
                record.identifier = ident;
                record.repository = repository;
                record.save();
            }
        }
    }
}
