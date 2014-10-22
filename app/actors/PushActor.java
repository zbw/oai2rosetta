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
import java.util.Date;
import java.util.Hashtable;

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
            String identifier = myMessage.getIdentifier();
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
    private void push(String identifier) {
        Record record = Record.findByIdentifier(identifier);
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

    public static boolean move(Record record) {
        boolean ok = false;
        Config conf = ConfigFactory.load();
        String importdirectory = conf.getString("importdirectory");
        JSch jsch = new JSch();
        Session sftpSession;
        File keyFile = new File(record.repository.ftpKey);
        try {
            sftpSession = jsch.getSession(
                    record.repository.ftpUser,
                    record.repository.ftpHost,
                    Integer.parseInt(record.repository.ftpPort)
            );
            Hashtable config = new Hashtable();
            config.put("StrictHostKeyChecking", "no");
            sftpSession.setConfig(config);
            if (keyFile.exists()) {
                jsch.addIdentity(keyFile.getAbsolutePath(), "");
            } else {
                Logger.info("keyfile not found: " + record.repository.ftpKey);
                return false;
            }
            sftpSession.connect();

            ChannelSftp sftpChannel = (ChannelSftp) sftpSession.openChannel("sftp");
            sftpChannel.connect();
            if (sftpChannel.isConnected()) {
                sftpChannel.cd(record.repository.ftpDir);
            }
            File baseDir = new File(importdirectory+record.repository.id+"/"+record.id);
            if (copyFiles(sftpChannel,baseDir)) {
                //delete local files

                try {
                    FileUtils.deleteDirectory(baseDir);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            ok = true;
            sftpSession.disconnect();
        } catch (JSchException e) {
            Logger.error("moveError for: " + record.identifier + " - "+ e.getMessage());
            e.printStackTrace();
        } catch (SftpException e) {
            Logger.error("moveError for: " + record.identifier + " - "+ e.getMessage());
            e.printStackTrace();
        }

        return ok;
    }
    private static boolean copyFiles(ChannelSftp sftpChannel, File src) {
        boolean overwrite = true;
        File[] list = src.listFiles();
        if(list==null){
            return false;
        }
        try {
            sftpChannel.mkdir(src.getName());
            // take parent rights
            try {
                SftpATTRS attr = sftpChannel.lstat(".");
                sftpChannel.setStat(src.getName(),attr);
            } catch (Exception e) {
                //Logger.info(e.getMessage() +" permissions for: " + src.getName() + " : " + sftpChannel.lstat(src.getName()));
            }
            //Logger.info("permissions for: " + src.getName() + " : " + sftpChannel.lstat(src.getName()));
        } catch (SftpException e) {
            if (!overwrite) {
                return false;
            }
        }
        try {
            sftpChannel.cd(src.getName());
        } catch (SftpException e1) {

        }
        // Start copying files in the directory
        for (File curFile : list) {
            if (curFile.isDirectory()) {
                copyFiles(sftpChannel,curFile);
            } else {
                try {
                    sftpChannel.put(curFile.getAbsolutePath(), curFile.getName(),ChannelSftp.OVERWRITE);
                } catch (SftpException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        try {
            sftpChannel.cd("..");
        } catch (SftpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }
}
