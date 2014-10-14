package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Created by Ott Konstantin on 21.08.2014.
 */
@Entity
public class Repository extends Model {

    @Constraints.Required
    @Id
    public String id;
    @Constraints.Required
    public String title;

    public String oaiUrl;
    public String oaiTitle;
    public String oaiMapping;
    public String pdsUrl;
    public String depositWsdlUrl;
    public String depositWsdlEndpoint;
    public String producerWsdlUrl;
    public String producerWsdlEndpoint;
    public String sipstatusWsdlUrl;
    public String sipstatusWsdlEndpoint;
    public String materialFlowId;
    public String producerId;
    public String depositSetId;
    public String userName;
    public String institution;
    public String password;
    public String ftpHost;
    public String ftpUser;
    public String ftpPort;
    public String ftpDir;
    public String ftpKey;
    public String ftpMax;
    public int joblimit;
    public boolean active;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "repository")   @JsonBackReference
    public List<Record> records;


    public static Finder<String, Repository> find = new Finder<String, Repository>(String.class, Repository.class);

    public static Repository findById(String id) {
        return find.where().eq("id", id).findUnique();
    }

    public Integer countStatus(int status) {
        return Record.countStatus(this.id, status);
    }
}
