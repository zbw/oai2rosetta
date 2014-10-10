package controllers;

import models.Repository;
import play.*;
import play.data.Form;
import play.db.ebean.Model;
import play.mvc.*;
import java.util.List;

import views.html.*;

import static play.libs.Json.toJson;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render("ZBW Hosting subapps"));
    }



}
