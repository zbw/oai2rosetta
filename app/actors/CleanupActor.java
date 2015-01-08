package actors;

import akka.actor.UntypedActor;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import models.Record;
import play.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Hashtable;

/**
 * Created by Ott Konstantin on 25.09.2014.
 */
public class CleanupActor extends UntypedActor {



    @Override
    public void onReceive(Object message) throws Exception {
        StatusMessage statusMessage = new StatusMessage();
        statusMessage.setType(StatusMessage.CLEANUPJOB);
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
            cleanup(identifier);
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

    private void cleanup(int identifier) {
        Record record = Record.findById(identifier);
        if (record != null) {
            cleanup(record);
        }
    }

   public static boolean cleanup(Record record) {
       boolean ok = false;
       String recordDir = record.repository.ftpDir + "/" + record.id;
       Config conf = ConfigFactory.load();
       JSch jsch = new JSch();
       Session sshSession;
       File keyFile = new File(record.repository.ftpKey);
       try {
           sshSession = jsch.getSession(
                   record.repository.ftpUser,
                   record.repository.ftpHost,
                   Integer.parseInt(record.repository.ftpPort)
           );
           Hashtable config = new Hashtable();
           config.put("StrictHostKeyChecking", "no");
           sshSession.setConfig(config);
           if (keyFile.exists()) {
               jsch.addIdentity(keyFile.getAbsolutePath(), "");
           } else {
               Logger.info("keyfile not found: " + record.repository.ftpKey);
               return false;
           }
           sshSession.connect();
           try {
               if (existsDir(record, sshSession, recordDir)) {
                   rmDir(sshSession, recordDir);
                   if (!existsDir(record, sshSession, recordDir)) {
                       record.status = record.STATUSCLEAN;
                       record.save();
                   }
               }
           } catch (IOException e) {
               e.printStackTrace();
           }
           sshSession.disconnect();

       } catch (JSchException e) {
           Logger.error("SSL Connection error for:" + record.id + " - " + e.getMessage());
           e.printStackTrace();
       }
       return ok;
   }

    private static boolean existsDir(Record record,Session sshSession, String recordDir) throws JSchException, IOException {
        boolean ok = true;
        ChannelExec channel = (ChannelExec) sshSession.openChannel("exec");
        channel.setCommand("/bin/ls " + recordDir);
        channel.connect();
        InputStream commandOutput = channel.getExtInputStream();
        int readByte = commandOutput.read();
        StringBuilder outputBuffer = new StringBuilder();
        while(readByte != 0xffffffff)
        {
            outputBuffer.append((char)readByte);
            readByte = commandOutput.read();
        }
        if (outputBuffer.indexOf("No such file") > 0) {
            ok = false;
            record.status = record.STATUSCLEAN;
            record.save();
        }
        channel.disconnect();
        return ok;
    }

    private static void rmDir(Session sshSession, String ieFolderName) throws JSchException, IOException {
        //Logger.info("deleting: " + ieFolderName);
        ChannelExec channel = (ChannelExec) sshSession.openChannel("exec");
        channel.setCommand("/bin/rm -rf " + ieFolderName);
        //channel.setCommand("/bin/touch " + ieFolderName);
        channel.connect();
        channel.disconnect();
    }
}
