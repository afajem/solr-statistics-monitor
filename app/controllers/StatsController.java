package controllers;

import com.fasterxml.jackson.databind.JsonNode;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import actors.StatsActor;

import akka.actor.ActorRef;
import akka.actor.Props;

import models.Statistics;

import play.Logger;
import play.libs.Akka;
import play.libs.Json;
import play.libs.F.*;
import play.libs.ws.*;
import play.mvc.*;

import scala.collection.JavaConverters;

import services.EmbeddedServer;
import views.html.stats;


/**
 * The application controller
 */
public class StatsController extends Controller {

    @Inject WSClient ws;

    @Inject EmbeddedServer embeddedServer;

    /**
     * Delegates to the common stats method requesting the Cache statistics
     * by default.
     *
     * @return
     */
	public Promise<Result> index() {
        return stats(StatisticsConstants.CACHE_STAT_TYPE);
	}



    /**
     * WebSocket endpoint that the browser will attach to for full duplexed communication
     * @return
     */
    public WebSocket<JsonNode> ws(String statType) {
        
    	return new WebSocket<JsonNode>() {
            public void onReady(final WebSocket.In<JsonNode> in, final WebSocket.Out<JsonNode> out) {
                // create a new StatsActor to service this websocket connection
            	String message = null;
            	if (StatisticsConstants.CACHE_STAT_TYPE.equals(statType)) {
            		message = StatisticsConstants.CACHE_STATS_MESSAGE;
            	}
            	else if (StatisticsConstants.QUERY_STAT_TYPE.equals(statType)) {
            		message = StatisticsConstants.QUERY_STATS_MESSAGE;
            	}

                final ActorRef statsActor = Akka.system().actorOf(Props.create(StatsActor.class, out, message, ws));

                // send all WebSocket message to actor
                in.onMessage(new Callback<JsonNode>() {
                    @Override
                    public void invoke(JsonNode jsonNode) throws Throwable {
                        String message = jsonNode.get("message").textValue();
                        statsActor.tell(message, statsActor);

                        Logger.debug("Received message: " + message);
                    }
                });

                // on close, tell the stats actor to shutdown
                in.onClose(new Callback0() {
                    @Override
                    public void invoke() throws Throwable {
                        Akka.system().stop(statsActor);
                    }
                });
            }
        };
    }

    
    /**
     * Simulate a search in Solr to demonstrate a change in statistics.
     *
     * @return
     */
	public Promise<Result> simulateSearch(String keywords) throws Exception {
	    
		String query = StatisticsConstants.SOLR_QUERY_URL;

        WSRequest solrQueryRequest = ws.url(query)
                .setQueryParameter("q", URLEncoder.encode(keywords, "UTF-8"))
                .setQueryParameter("wt", "json");

        Promise<Result> resultPromise = solrQueryRequest.get().map( response -> {
            JsonNode jsonNode = response.asJson();
            int status = jsonNode.findValue("status").asInt(BAD_REQUEST);

            if (status == 0) {
                return ok();
            }
            else {
                return internalServerError();
            }
        });

		return resultPromise;
	}

	
	/**
	 * Common stats endpont uses for both Cache and Query statistics
	 * @param statType
	 * @return
	 */
    public Promise<Result> stats(String statType) {

    	if (StatisticsConstants.CACHE_STAT_TYPE.equals(statType)) {
    		return fetchStats(StatisticsConstants.CACHE_STAT_CATEGORY, 
    						StatisticsConstants.CACHE_STATS_KEYS, 
    						StatisticsConstants.CACHE_STAT_TYPE,
    						StatisticsConstants.CACHE_PAGE_TITLE_KEY);
    	}
    	else if (StatisticsConstants.QUERY_STAT_TYPE.equals(statType)) {
    		return fetchStats(StatisticsConstants.QUERY_STAT_CATEGORY,
    						StatisticsConstants.QUERY_STATS_KEYS, 
    						StatisticsConstants.QUERY_STAT_TYPE,
							StatisticsConstants.QUERY_PAGE_TITLE_KEY);
    	}
    	else {
    		return Promise.promise(
				new Function0<Result>() {
					public Result apply() {
						return internalServerError("Unable to determine the requested statistics type");
					}
				});
    	}
    }

    
    /**
     * Fetch the initial list of statistics types for the specified statistics
     * category. 
     * 
     * @param statCategory
     * @param statKeys
     * @param statType
     * @param statPageTitle
     * @return
     */
    private Promise<Result> fetchStats (
    	String statCategory, final List<String> statKeys, 
    	final String statType, final String statPageTitle) {

        WSRequest mbeanRequest =  ws.url(StatisticsConstants.SOLR_MBEAN_URL)
                .setQueryParameter("cat", statCategory)
                .setQueryParameter("stats", "true")
                .setQueryParameter("wt", "json");

        Promise<Result> resultPromise = mbeanRequest.get().map(response -> {
            JsonNode jsonNode = response.asJson();
            int status = jsonNode.findValue("status").asInt(BAD_REQUEST);

            if (status == 0) {
                List<Statistics> statsList = new ArrayList<Statistics>();
                for (String statKey : statKeys) {
                    Statistics stat =  extractStatistics(statKey, jsonNode);

                    if (stat != null) {
                        statsList.add(stat);
                    }
                }
                scala.collection.immutable.List<Statistics> scalaStatsList =
                        JavaConverters.asScalaBufferConverter(statsList).asScala().toList();

                return ok(stats.render(statType, statPageTitle, scalaStatsList));
            } else {
                return internalServerError();
            }
        });

		return resultPromise;    	
    }
    
    
    /**
     * Extract the statistics from a JsonNode for a specific statistic
     * @param statKey
     * @param responseJsonNode
     * @return
     */
    private Statistics extractStatistics(String statKey, JsonNode responseJsonNode) {
        JsonNode jsonNode = responseJsonNode.findValue(statKey);

        Statistics stat = null;

        if (jsonNode != null) {
            //extract the stats node , convert to Statistics object and return
            stat = Json.fromJson(jsonNode.findValue("stats"), Statistics.class);
            stat.key = statKey;
            stat.name = statKey;

            //Add Timestamp
            stat.timestamp = System.currentTimeMillis();
        }

        return stat;
    }
	
}