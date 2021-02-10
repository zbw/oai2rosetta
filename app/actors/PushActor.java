package actors;

import akka.actor.UntypedActor;
import com.jcraft.jsch.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import models.Record;
import org.apache.commons.io.FileUtils;
import play.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * Created by Ott Konstantin on 25.09.2014.
 */
public class PushActor extends UntypedActor {


    @Override
    public void onReceive(Object message) throws Exception {
        StatusMessage statusMessage = new StatusMessage();
        statusMessage.setType(StatusMessage.PUSHJOB);
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
            push(identifier);
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
    private void push(int identifier) {
        Record record = Record.findById(identifier);
        if (record != null) {
            record.status = record.STATUSEXPORTING;
            record.save();

            if (move(record)) {
                record.status = record.STATUSEXPORTED;
            } else {
                record.status = record.STATUSEXPORTEDERROR;
            }
            record.save();
        }
    }
    /**
     * New implementation by Hunar Karim on 10.02.2021.
     */
    public static boolean move(Record record) {
        boolean ok = false;
        Config conf = ConfigFactory.load();
        String importdirectory = conf.getString("importdirectory");

        try {
            copyFiles(importdirectory+record.repository.id+"/"+record.id, record.repository.ftpDir+"/"+record.id);
            FileUtils.deleteDirectory(new File(importdirectory+record.repository.id+"/"+record.id));
            ok = true;
        } catch (IOException e) {
            Logger.error("moveError for: " + record.identifier + " - "+ e.getMessage(), e);
            ok = false;
        }

        return ok;
    }
   
    public static void copyFiles(String sourcePath, String dest) throws IOException {

      try {
        Path path = Paths.get(dest);
        Files.createDirectories(path);
        FileUtils.copyDirectory(new File(sourcePath), new File(dest));
        Logger.info("Directory is created!");

      } catch (IOException e) {
        Logger.error("Failed to create directory!" + e.getMessage(), e);
      }
     
    
    }
}
