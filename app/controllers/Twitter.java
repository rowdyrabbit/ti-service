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
    private static final String twitterConsumerKey = Play.application().configuration().getString("securesocial.twitter.consumerKey");
    private static final String twitterConsumerSecret = Play.application().configuration().getString("securesocial.twitter.consumerSecret");
    private static final String twitterRequestTokenUrl = Play.application().configuration().getString("securesocial.twitter.requestTokenUrl");
    private static final String twitterAccessTokenUrl = Play.application().configuration().getString("securesocial.twitter.accessTokenUrl");
    private static final String twitterAuthorizationUrl = Play.application().configuration().getString("securesocial.twitter.authorizationUrl");

    static final ConsumerKey KEY = new ConsumerKey(twitterConsumerKey, twitterConsumerSecret);

    private static final ServiceInfo SERVICE_INFO = new ServiceInfo(twitterRequestTokenUrl,
            twitterAccessTokenUrl,
            twitterAuthorizationUrl,
            KEY);

    private static final OAuth TWITTER = new OAuth(SERVICE_INFO);

    public static Result auth() {

        System.out.println("consumer key: " + twitterConsumerKey);
        System.out.println("consumer secret" + twitterConsumerSecret);
        System.out.println(twitterRequestTokenUrl);
        System.out.println(twitterAccessTokenUrl);
        System.out.println(twitterAuthorizationUrl);

        String verifier = request().getQueryString("oauth_verifier");
        if (Strings.isNullOrEmpty(verifier)) {
            String url = controllers.routes.Twitter.auth().absoluteURL(request());

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