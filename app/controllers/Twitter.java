package controllers;

import play.Play;
import play.libs.F.Option;
import play.libs.OAuth;
import play.libs.OAuth.ConsumerKey;
import play.libs.OAuth.RequestToken;
import play.libs.OAuth.ServiceInfo;
import play.mvc.Controller;
import play.mvc.Result;

import com.google.common.base.Strings;

public class Twitter extends Controller {
    static final ConsumerKey KEY = new ConsumerKey(Play.application().configuration().getString("twitter.consumer.key"),
            Play.application().configuration().getString("twitter.consumer.secret"));

    private static final ServiceInfo SERVICE_INFO = new ServiceInfo("https://api.twitter.com/oauth/request_token",
            "https://api.twitter.com/oauth/access_token",
            "https://api.twitter.com/oauth/authorize",
            KEY);

    private static final OAuth TWITTER = new OAuth(SERVICE_INFO);

    public static Result auth() {
        String verifier = request().getQueryString("oauth_verifier");
        if (Strings.isNullOrEmpty(verifier)) {
//            String url = controllers.routes.Twitter.auth().absoluteURL(request());
            String url = "";

            RequestToken requestToken = TWITTER.retrieveRequestToken(url);
            saveSessionTokenPair(requestToken);
            return redirect(TWITTER.redirectUrl(requestToken.token));
        } else {
            RequestToken requestToken = getSessionTokenPair().get();
            RequestToken accessToken = TWITTER.retrieveAccessToken(requestToken, verifier);
            saveSessionTokenPair(accessToken);
            return redirect(controllers.routes.Application.index());
        }
    }

    private static void saveSessionTokenPair(RequestToken requestToken) {
        session("token", requestToken.token);
        session("secret", requestToken.secret);
    }

    static Option<RequestToken> getSessionTokenPair() {
        if (session().containsKey("token")) {
            return Option.Some(new RequestToken(session("token"), session("secret")));
        }
        return Option.None();
    }
}