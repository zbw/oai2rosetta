package controllers;

import models.User;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import views.html.login;

import static play.data.Form.form;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render("ZBW Hosting subapps"));
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
        Form<User> userForm = form(User.class).bindFromRequest();
        if (userForm.hasErrors()) {
            return badRequest(login.render(userForm));
        } else {
            flash("success", "You've been logged in");
            session().clear();
            session("email", userForm.get().email);
            return redirect(
                    routes.Application.index()
            );
        }
    }





}
