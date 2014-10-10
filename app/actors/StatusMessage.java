package actors;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Ott Konstantin on 06.10.2014.
 */
public class StatusMessage implements Serializable {
    private String type;
    private boolean exists;
    private int count;
    private String status;
    private boolean active;
    private String error;
    private Date started;
    private Date finished;

    public static final String FETCHJOB =       "FetchActor";
    public static final String CREATEJOB =      "CreateIEActor";
    public static final String PUSHJOB =        "PushActor";
    public static final String DEPOSITJOB =     "DepositActor";
    public static final String SIPSTATUSJOB =   "SipStatusActor";

    public  StatusMessage() {
         this.exists= true;
    }
    public StatusMessage(boolean active) {
        this.active = active;
        this.exists = true;
    }
    public StatusMessage(boolean active, String status) {
        this.active = active;
        this.status = status;
        this.exists = true;
    }

    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public Date getStarted() {
        return started;
    }

    public void setStarted(Date started) {
        this.started = started;
    }

    public Date getFinished() {
        return finished;
    }

    public void setFinished(Date finished) {
        this.finished = finished;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String toString() {
        return type
                + " status: " + status
                + " active: " + active
                + " finished: " + count
                + " started: " + started
                + " ended: " + finished;
    }
}

