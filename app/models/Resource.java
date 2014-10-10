package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Created by Ott Konstantin on 21.08.2014.
 */
@Entity
public class Resource extends Model {
    @Id
    public long id;
    public String origFile;
    public String localFile;
    public String description;
    public String mime;
    @ManyToOne
    public Record record;

    public static Finder<String, Resource> find = new Finder<String, Resource>(String.class, Resource.class);

}
