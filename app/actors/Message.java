package actors;

import java.io.Serializable;

/**
 * Created by Ott Konstantin on 02.10.2014.
 */
public class Message implements Serializable {

    private boolean isBatch;
    private String identifier;
    private int limit;

    public Message(boolean isBatch, String identifier, int limit) {
        this.isBatch = isBatch;
        this.identifier = identifier;
        this.limit = limit;
    }

    public boolean isBatch() {
        return isBatch;
    }

    public void setBatch(boolean isBatch) {
        this.isBatch = isBatch;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
