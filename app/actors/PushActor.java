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

    public static boolean move(Record record) {
        Config conf = ConfigFactory.load();
        String importdirectory = conf.getString("importdirectory");
        if(record.repository.localImport){
            return setFtp(record, importdirectory);
        }else {
            return setSftp(record, importdirectory);
        }
    }
    /**
     * new function created by Hunar Karim on 12.05.2021.
     */
    private static boolean setFtp(Record record, String importdirectory){
        boolean ok = false;
        try {
            setCopyFileFtp(importdirectory+record.repository.id+"/"+record.id, record.repository.ftpDir+"/"+record.id);
            FileUtils.deleteDirectory(new File(importdirectory+record.repository.id+"/"+record.id));
            ok = true;
        } catch (IOException e) {
            Logger.error("moveError for: " + record.identifier + " - "+ e.getMessage(), e);
            ok = false;
        }
        return ok;
    }
    /**
     * new function created by Hunar Karim on 12.05.2021.
     */
    private static void setCopyFileFtp(String sourcePath, String dest) throws IOException{
        try {
            Path path = Paths.get(dest);
            Files.createDirectories(path);
            FileUtils.copyDirectory(new File(sourcePath), new File(dest));
            Logger.info("Directory is created!");

        } catch (IOException e) {
            Logger.error("Failed to create directory!" + e.getMessage(), e);
        }
    }

    private static boolean setSftp(Record record, String importdirectory){
        boolean ok = false;
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
                Logger.info("keyfile not found: " + keyFile.getAbsolutePath() + " - "+record.repository.ftpKey);
                record.errormsg= "keyfile not found: " + keyFile.getAbsolutePath() + " - "+record.repository.ftpKey;
                record.save();
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
                ok = true;
            } else {
                ok = false;
            }
            sftpChannel.disconnect();
            sftpSession.disconnect();
        } catch (JSchException e) {
            Logger.error("moveError for: " + record.identifier + " - "+ e.getMessage(), e);
        } catch (SftpException e) {
            Logger.error("moveError for: " + record.identifier + " - "+ e.getMessage(), e);
        }
        return ok;
    }


    private static boolean copyFiles(ChannelSftp sftpChannel, File src) {
        File[] list = src.listFiles();
        if(list==null){
            return false;
        }
        try {
            sftpChannel.cd(src.getName());
        } catch (SftpException e1) {
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
                return false;
            }
            try {
                sftpChannel.cd(src.getName());
            } catch (SftpException e2) {

            }
        }

        // Start copying files in the directory
        for (File curFile : list) {
            if (curFile.isDirectory()) {
                if (!copyFiles(sftpChannel,curFile)) return false;
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
            Vector remotefiles = sftpChannel.ls(".");
            sftpChannel.cd("..");
            if (remotefiles.size()-2 != list.length) {
                Logger.error("sftp not matching: Sent: " + list.length + " received: " + (remotefiles.size()-2));
                return false;
            }
        } catch (SftpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }
}
