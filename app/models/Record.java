package models;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Page;
import com.fasterxml.jackson.annotation.JsonBackReference;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by Ott Konstantin on 21.08.2014.
 */
@Entity
public class Record extends Model {

    @Id
    public String identifier;
    public String id;

    @ManyToOne
    public Repository repository;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "record")   @JsonBackReference
    public List<Resource> resources;
    public String title;
    public String metadata;

    public Date logcreated;
    public Date logmodified;
    public String loguser;
    public long sipId;
    public int status;
    public String sipStatus;
    public String sipModul;
    public String sipActive;

    public static final int STATUSNEW = 0;
    public static final int STATUSCREATED = 1;
    public static final int STATUSIMPORTEDERROR = 2;
    public static final int STATUSIMPORTED = 3;
    public static final int STATUSIMPORTING = 4;
    public static final int STATUSIECREATEDERROR = 5;
    public static final int STATUSIECREATED = 6;
    public static final int STATUSIECREATING = 7;
    public static final int STATUSEXPORTEDERROR = 8;
    public static final int STATUSEXPORTED = 9;
    public static final int STATUSEXPORTING = 10;
    public static final int STATUSINGESTEDERROR = 11;
    public static final int STATUSINGESTED = 12;
    public static final int STATUSINGESTING = 13;
    public static final int STATUSFINISHED = 100;
    public static final int[] ALLSTATUS = {
            STATUSNEW,
            STATUSCREATED,
            STATUSIMPORTED,
            STATUSIMPORTEDERROR,
            STATUSIMPORTING,
            STATUSIECREATING,
            STATUSIECREATEDERROR,
            STATUSIECREATED,
            STATUSEXPORTED,
            STATUSEXPORTING,
            STATUSEXPORTEDERROR,
            STATUSINGESTEDERROR,
            STATUSINGESTED,
            STATUSINGESTING,
            STATUSFINISHED
    };


    public static Finder<String, Record> find = new Finder<String, Record>(String.class, Record.class);
    public static Finder<String, Resource> rfind = new Finder<String, Resource>(String.class, Resource.class);


    public static Record findByHandle(int handle) {
        return find.where().eq("handlepost", handle).findUnique();
    }
    public static Record findByIdentifier(String identifier) {
        return find.where().eq("identifier", identifier).findUnique();
    }
    public List<Resource> getResources() {
      return Resource.find.where().eq("record_identifier",this.identifier).findList();
    }

    public static Integer countStatus(String identifier, int status) {
        return new Integer(find.where().eq("repository_id",identifier).eq("status", status).findRowCount());

    }
    public boolean existResource(String origfile) {
        for (Resource resource : resources) {
            if (resource.localFile.equals(origfile)) {
                return true;
            }
        }
        return false;
    }



    /**
     * Return a page of records
     *
     * @param page Page to display
     * @param pageSize Number of records per page
     * @param sortBy Record property used for sorting
     * @param order Sort order (either or asc or desc)
     * @param filter Filter applied on the name column
     */
    public static Page<Record> page(Repository repository,int page, int pageSize, String sortBy, String order, String filter, int status) {
        if (status == -1) {
            return
                    find.where()
                            .eq("repository", repository)
                            .or(Expr.ilike("identifier", "%" + filter + "%"), Expr.ilike("sipId", "%" + filter + "%"))
                            .orderBy(sortBy + " " + order)
                            .findPagingList(pageSize)
                            .setFetchAhead(false)
                            .getPage(page);
        } else {
            return
                    find.where()
                            .eq("repository", repository)
                            .eq("status", status)
                            .or(Expr.ilike("identifier", "%" + filter + "%"), Expr.ilike("sipId", "%" + filter + "%"))
                            .orderBy(sortBy + " " + order)
                            .findPagingList(pageSize)
                            .setFetchAhead(false)
                            .getPage(page);
        }
    }

    public static List<Record> limit(String repository, int status, int limit) {
        return
                find.where()
                        .eq("repository_id",repository)
                        .eq("status",status)
                        .setMaxRows(limit).findList();
    }
}
