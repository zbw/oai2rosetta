package controllers;

import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Security;

/**
 * Created by Ott Konstantin on 16.10.2014.
 */
public class Secured extends Security.Authenticator {
    @Override
    public String getUsername(Context ctx) {
        return ctx.session().get("email");
    }

    @Override
    public Result onUnauthorized(Context ctx) {
        return redirect(routes.Application.login());
    }
}
