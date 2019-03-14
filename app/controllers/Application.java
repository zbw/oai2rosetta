package controllers;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import models.User;
import org.apache.commons.net.util.SubnetUtils;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.index;
import views.html.login;

import java.util.StringTokenizer;

import static play.data.Form.form;

public class Application extends Controller {

    @Security.Authenticated(Secured.class)
    public static Result index() {
        Config conf = ConfigFactory.load();
        String appname = conf.getString("institution.appname");
        return ok(index.render(appname));
    }

    public static Result login() {

        return ok(
                login.render(form(User.class))
        );
    }
    public static Result logout() {
        session().clear();
        flash("success", "You've been logged out");
        return redirect(
                routes.Application.login()
        );
    }
    public static Result authenticate() {
        Form<User> userForm  = form(User.class).bindFromRequest();

        String ip = request().remoteAddress();
        if (request().getHeader("X-Forwarded-For")!=null) {
            ip = request().getHeader("X-Forwarded-For");
        }
        if (userForm.hasErrors()) {
            return badRequest(login.render(userForm));
        } else if (!validIP(ip)) {
            flash("error", "This application can only be used on defined IP range. Your IP was: " + ip);
            return redirect(
                    routes.Application.login()
            );
        } else {
            User user = userForm.get();
            flash("success", "You've been logged in");
            session().clear();
            session("email", user.email);
            return redirect(
                    routes.Application.index()
            );
        }
    }

    private static boolean validIP(String ip) {
        Logger.info("validating IP: " + ip);
        Config conf = ConfigFactory.load();
        String iprange = conf.getString("ip.range");
        if (iprange== null || iprange.equals("")) {
            Logger.info("validating IP: not range given");
            return true;
        } else {
            StringTokenizer st = new StringTokenizer(iprange);
            while (st.hasMoreElements()) {
                String checkIP = st.nextToken();
                Logger.info("validating IP: checking- " + checkIP);
                if (checkIP.indexOf("/")>0) {
                    //CIDR Notation
                    try {
                        SubnetUtils utils = new SubnetUtils(checkIP);
                        if (utils.getInfo().isInRange(ip)) {
                            return true;
                        }
                    } catch ( IllegalArgumentException e) {
                        Logger.error(e.getMessage());
                    }
                } else {
                    //single IP
                    if (ip.equals(checkIP)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }




}
