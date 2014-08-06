package controllers;


import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.*;
import play.libs.F.Function;
import play.libs.F.Option;
import play.libs.OAuth.OAuthCalculator;
import play.libs.OAuth.RequestToken;
import play.libs.WS;
import play.libs.WS.Response;
import play.mvc.Controller;
import play.mvc.Result;
import play.Play;

import com.typesafe.plugin.RedisPlugin;
import play.cache.Cache;
import redis.clients.jedis.*;

//import securesocial.core.java.SecureSocial;
import views.html.*;

import java.util.ArrayList;
import java.util.List;

public class Application extends Controller {

    public static Result index() {
        return ok(views.html.index.render());
    }


    @BodyParser.Of(BodyParser.Json.class)
//    @SecureSocial.SecuredAction(ajaxCall = true)
    public static Result secureAction() {
        ObjectNode result = Json.newObject();
        result.put("status", "OK");
        result.put("tweeters", "hi");
        return ok(result);
    }

    public static Promise<Result> getCommonTweeters() {

        //Make calls to twitter API for each tweeter ans keep a hold of the responses
        //then store the responses in Redis
        //then call intersect set on the data and whatever other functions the user wants.
        //keep the data stored in redis until user exits?
        Option<RequestToken> sessionTokenPair = Twitter.getSessionTokenPair();
        if (sessionTokenPair.isDefined()) {

            final Promise<Result> resultPromise =
                    WS.url("https://api.twitter.com/1.1/statuses/home_timeline.json")
                            .sign(new OAuthCalculator(Twitter.KEY, sessionTokenPair.get()))
                            .get()
                            .map(new Function<Response, Result>() {
                                @Override
                                public Result apply(Response result) throws Throwable {
                                    return ok(result.asJson());
                                }
                            });




            JedisPool pool = Play.application().plugin(RedisPlugin.class).jedisPool();
            Jedis jedis = pool.getResource();


            return resultPromise;
        }
        return Promise.pure((Result) redirect(controllers.routes.Twitter.auth()));
    }


    public static Promise<Result> getHomeTimeline() {
        Option<RequestToken> sessionTokenPair = Twitter.getSessionTokenPair();
        if (sessionTokenPair.isDefined()) {

            final Promise<Result> resultPromise =
                    WS.url("https://api.twitter.com/1.1/statuses/home_timeline.json")
                            .sign(new OAuthCalculator(Twitter.KEY, sessionTokenPair.get()))
                            .get()
                            .map(new Function<Response, Result>() {
                                @Override
                                public Result apply(Response result) throws Throwable {
                                    return ok(result.asJson());
                                }
                            });
            return resultPromise;
        }
        return Promise.pure((Result) redirect(controllers.routes.Twitter.auth()));
    }

//    final Promise<WSResponse> responseThreePromise = WS.url(urlOne).get().flatMap(
//            new Function<WSResponse, Promise<WSResponse>>() {
//                public Promise<WSResponse> apply(WSResponse responseOne) {
//                    String urlTwo = responseOne.getBody();
//                    return WS.url(urlTwo).get().flatMap(
//                            new Function<WSResponse, Promise<WSResponse>>() {
//                                public Promise<WSResponse> apply(WSResponse responseTwo) {
//                                    String urlThree = responseTwo.getBody();
//                                    return WS.url(urlThree).get();
//                                }
//                            }
//                    );
//                }
//            }
//    );

    public static Promise<Result> getTwitterAccounts(String username, String password) {



        Promise<WS.Response> loginPagePromise = WS.url("https://www.blah.com/login").get().flatMap(
                new Function<WS.Response, Promise<WS.Response>>() {
                    public Promise<WS.Response> apply(WS.Response response) {
                        String body = response.getBody();
                        Document doc = Jsoup.parse(body);
                        String authToken = doc.select("[name=authenticity_token]").first().val();
                        List<WS.Cookie> cookieList = response.getCookies();

                        return  WS.url("https://www.blah.com/sessions")
                                .setContentType("application/x-www-form-urlencoded")
                                .setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.114 Safari/537.36")
                                .setHeader("Cookie", cookieList.toString())
                                .post("utf8=%E2%9C%93&authenticity_token=" + authToken + "&email=" + username + "&password=" + password + "&commit=Log+in")
                                ;
//                            .flatMap(
//                            new Function<WS.Response, Promise<WS.Response>>() {
//                                public Promise<WS.Response> apply (WS.Response response2) {
//                                    return WS.url("https://www.blah.com/private")
//                                            .setHeader("Cookie", response2.getCookies().toString())
//                                            .get();
//                                }
//                            }
//                    );
                    }
                }
        );

        return loginPagePromise.map(new Function<Response, Result>() {
            @Override
            public Result apply(Response result) throws Throwable {
                List<Batch> batchList = new ArrayList<Batch>();
                String allUsers = result.getBody();
                //Now parse the list of all users' twitter accounts.
                Document doc = Jsoup.parse(allUsers);
                Element batchContent = doc.getElementById("batches");
                Elements allBatches = batchContent.children().select("ul");
                for (Element batch : allBatches) {
                    Batch b = buildBatch(batch);
                    batchList.add(b);
                }
                return ok(printBatchList(batchList));
            }

            private String printBatchList(List<Batch> batchList) {
                StringBuffer sb = new StringBuffer();
                for (Batch b : batchList) {
                    sb.append(b.toString()).append("\n");
                }
                return sb.toString();
            }
        });




//        Promise<List<WS.Cookie>> cookies = loginPagePromise.map(
//                new Function<WS.Response, List<WS.Cookie>>() {
//                    public List<WS.Cookie> apply(Response response) {
//                        return response.getCookies();
//                    }
//                }
//        );
//
//
//        Promise<WS.Response> loginResult = WS.url("https://www.blah.com/sessions")
//                .setContentType("application/x-www-form-urlencoded")
//                .setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.114 Safari/537.36")
//                .setHeader("Cookie", cookies.toString())
//                .post("utf8=%E2%9C%93&authenticity_token=" + authToken.get(200000) + "&email=dcbfernandez%40gmail.com&password=Hornet9332%21&commit=Log+in");


//        final Promise<Result> resultPromise =
//                WS.url("https://www.blah.com/private")
//                        .setHeader("Cookie", cookies.toString())
//                        .get()
//
//                        .map(new Function<Response, Result>() {
//                            @Override
//                            public Result apply(Response result) throws Throwable {
//                                return ok(result.getBody());
//                            }
//                        });
//        return resultPromise;
    }

    private static Batch buildBatch(Element batchElement) {
        String batchName = batchElement.attr("id");
        Batch batch = new Batch(batchName, batchName);
        Elements batchlings = batchElement.getElementsByTag("li");
        for (Element batchling : batchlings) {
            batch.addBatchling(batchling.select("div.name > a").text(), getTwitterHandle(batchling));
        }
        return batch;
    }

    private static String getTwitterHandle(Element batchling) {
        Element twitterLink = batchling.select("[href*=twitter.com]").first();
        if (twitterLink != null) {
            String twitterUrl = twitterLink.attr("href");
            String twitterHandle = twitterUrl.substring(twitterUrl.lastIndexOf("/") + 1, twitterUrl.length());
            return twitterHandle;
        } else {
            return null;
        }
    }

    static class Batch {
        private String batchId;
        private String batchName;

        private List<Batchling> batchlings;

        public Batch(String batchId, String batchName) {
            this.batchId = batchId;
            this.batchName = batchName;
            this.batchlings = new ArrayList<Batchling>();
        }

        public void addBatchling(String name, String twitterHandle) {
            this.batchlings.add(new Batchling(name, twitterHandle));
        }

        public String getBatchId() {
            return this.batchId;
        }

        public String getBatchName() {
            return this.batchName;
        }

        public List<Batchling> getBatchlings() {
            return this.batchlings;
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("Batch ID: ").append(batchId).append("\n");
            for (Batchling b : batchlings) {
                sb.append(b.toString());
            }
            return sb.toString();
        }


        static class Batchling {
            private String name;
            private String twitterHandle;

            public Batchling(String name, String twitterHandle) {
                this.name = name;
                this.twitterHandle = twitterHandle;
            }

            public String getName() {
                return name;
            }

            public String getTwitterHandle() {
                return twitterHandle;
            }

            @Override
            public String toString() {
                StringBuffer sb = new StringBuffer();
                sb.append(name).append(" - ").append(twitterHandle).append("\n");
                return sb.toString();
            }
        }

    }

}
