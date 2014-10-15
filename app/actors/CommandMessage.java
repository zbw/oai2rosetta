package actors;

import java.io.Serializable;

/**
 * Created by Ott Konstantin on 08.10.2014.
 */
public class CommandMessage implements Serializable {

    private String command;
    private Message message;

    public CommandMessage(String command,boolean isBatch, String identifier, int limit) {
        this.command = command;
        this.message = new Message(isBatch,identifier,limit);
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
