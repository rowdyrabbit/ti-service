package controllers;


import akka.japi.Option;
import com.google.common.base.Strings;
import play.api.mvc.Result;
import play.libs.F;
import play.libs.OAuth;
import play.mvc.Controller;

public class Twitter extends Controller {

    static final OAuth.ConsumerKey KEY = new ConsumerKey("...", "...");

    private static final OAuth.ServiceInfo SERVICE_INFO = new OAuth.ServiceInfo("https://api.twitter.com/oauth/request_token",
            "https://api.twitter.com/oauth/access_token",
            "https://api.twitter.com/oauth/authorize",
            KEY);

    private static final OAuth TWITTER = new OAuth(SERVICE_INFO);

    public static Result auth() {
        String verifier = request().getQueryString("oauth_verifier");
        if (Strings.isNullOrEmpty(verifier)) {
            String url = controllers.routes.Twitter.auth().absoluteURL(request());
            OAuth.RequestToken requestToken = TWITTER.retrieveRequestToken(url);
            saveSessionTokenPair(requestToken);
            return redirect(TWITTER.redirectUrl(requestToken.token));
        } else {
            OAuth.RequestToken requestToken = getSessionTokenPair().get();
            OAuth.RequestToken accessToken = TWITTER.retrieveAccessToken(requestToken, verifier);
            saveSessionTokenPair(accessToken);
            return redirect(controllers.routes.Application.index());
        }
    }

    private static void saveSessionTokenPair(OAuth.RequestToken requestToken) {
        session("token", requestToken.token);
        session("secret", requestToken.secret);
    }

    static Option<OAuth.RequestToken> getSessionTokenPair() {
        if (session().containsKey("token")) {
            return Option.Some(new OAuth.RequestToken(session("token"), session("secret")));
        }
        return Option.None();
    }


}
