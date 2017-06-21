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
    public Integer repository_id;
    public String id;
    @Constraints.Required
    public String title;

    public String oaiUrl;
    public String oaiTitle;
    public String oaiMapping;
    public String dcingest;
    public String nomimetypes;
    public String metadataPrefix;
    public String resourcesPrefix;
    public String cms;
    public String cmsfield;
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
    public String source_mdformat;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "repository")   @JsonBackReference
    public List<Record> records;

    public Repository() {
        source_mdformat="";
    }

    public static Finder<String, Repository> find = new Finder<String, Repository>(String.class, Repository.class);

    public static Repository findById(int id) {
        return find.where().eq("repository_id", id).findUnique();
    }

    public Integer countStatus(int status) {
        return Record.countStatus(this.repository_id, status);
    }
}
