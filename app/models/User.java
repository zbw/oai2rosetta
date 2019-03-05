package models;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by Ott Konstantin on 16.10.2014.
 */
@Entity
public class User extends Model {

    @Id
    public String email;
    public String name;
    public String password;

    public User(String email, String name, String password) {
        this.email = email;
        this.name = name;
        this.password = password;
    }

    public static Finder<String,User> find = new Finder<String,User>(
            String.class, User.class
    );

    public String validate() {
        if (authenticate(email, password) == null) {
            return "Invalid email or password";
        }
        return null;
    }

    public static User authenticate(String email, String password) {
        return find.where().eq("email", email)
                .eq("password", password).findUnique();
    }


}
