package actors;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Iterator;
import java.util.List;

import models.SeriesData;
import models.SeriesPoint;
import play.Logger;
import play.Play;
import play.libs.Akka;
import play.libs.F;
import play.libs.Json;
import play.libs.WS;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.WebSocket;
import scala.concurrent.duration.Duration;
import akka.actor.Cancellable;
import akka.actor.UntypedActor;

import com.fasterxml.jackson.databind.JsonNode;

import controllers.StatisticsConstants;


/**
 * Actor used to probe Solr server for specified statistics type. Actor is initialized by
 * the controller. A reference to the WebSocket's output "channel" is passed in as a mechanism
 * for pushing statistics data back to the browser.
 */
public class StatsActor extends UntypedActor {

    private final WebSocket.Out<JsonNode> out;

    private int statsRefreshInterval = Play.application().configuration().getInt("stats.refresh.interval");

    /** The scheduled task */
    private Cancellable cancellable;

    /** Stats name, relates to the type of statistics the actor will retrieve and relay to the websocket
     *  listener.
     */
    private final String statsName;



    /**
     * Instantiates the actor, passing in the output channel of the web socket.
     *
     * @param out the WebSocket output channel to push result on.
     * @param statsName represents the message that will passed to the actor. Later used to determine the execution path
     */
    public StatsActor(WebSocket.Out<JsonNode> out, String statsName) {
        this.out = out;

        this.statsName = statsName;

        //Start the repeating and scheduled task
        startCancellable();
    }


    /**
     * Create a cancellable schedule class that will dispatch a message of type "statsName" on a pre-configured
     * interval
     */
    private void startCancellable() {
        cancellable = Akka.system().scheduler().schedule(
                Duration.create(statsRefreshInterval, SECONDS),
                Duration.create(statsRefreshInterval, SECONDS),
                this.self(),
                statsName,
                Akka.system().dispatcher(),
                null);
    }


    /**
     * Handles inbound messages for the actor.
     *
     * @param message used to determine the execution path of the actor.
     */
    public void onReceive(Object message) {
    	Logger.debug("Received message: " + message);
    	
        if (message.equals(StatisticsConstants.QUERY_STATS_MESSAGE)) {
            processStatsMessage(StatisticsConstants.QUERY_STAT_CATEGORY,
            		StatisticsConstants.QUERY_STATS_KEYS,
            		StatisticsConstants.QUERY_STATS_FIELD_NAMES);
        }
        else if (message.equals(StatisticsConstants.CACHE_STATS_MESSAGE)) {
            processStatsMessage(StatisticsConstants.CACHE_STAT_CATEGORY,
            		StatisticsConstants.CACHE_STATS_KEYS,
            		StatisticsConstants.CACHE_STATS_FIELD_NAMES);
        }
        else if (message.equals(StatisticsConstants.PAUSE_MESSAGE)) {
            Logger.info("Pausing actor...");
            cancellable.cancel();
        }
        else if (message.equals(StatisticsConstants.RESUME_MESSAGE)) {
            Logger.info("Resuming actor...");
            startCancellable();
        }

    }


    /**
     * Invoked when the actor is stopped. The scheduled task is also stopped here.
     * @throws Exception
     */
    @Override
    public void postStop() throws Exception {
        cancellable.cancel();
        super.postStop();
    }


    /**
     * Utility method used to handle the response from the statistic data response from the embedded
     * Solr server.
     *
     * @param statCategory represents the type of statistic being retrieved
     * @param statKeys is a list of statistics types that will be retrieved from Solr
     * @param statFieldNames is a list of statistics field for each statistic type  that we're interested in
     *                       returning to the browser
     */
    private void processStatsMessage(String statCategory, final List<String> statKeys, final List<String> statFieldNames) {
    	Logger.debug("Processing message for: " + statCategory + " on port " + 
    			Play.application().configuration().getString("solr.port") + " for url: " + StatisticsConstants.SOLR_MBEAN_URL);
    	
        WS.url(StatisticsConstants.SOLR_MBEAN_URL)
            .setQueryParameter("cat", statCategory)
            .setQueryParameter("stats", "true")
            .setQueryParameter("wt", "json").get().map(
            new F.Function<WS.Response, Object>() {
                public Result apply(WS.Response response) {

                	Logger.debug("Got response from Solr Query...");
                	
                    JsonNode jsonNode = response.asJson();
                    int status = jsonNode.findValue("status").asInt();

                    if (status == 0) {

                        for (String statKey : statKeys) {

                            //extract the stats node
                            JsonNode cacheNode = jsonNode.findValue(statKey);
                            JsonNode statsNode = cacheNode.findValue("stats");

                            //Add Timestamp
                            long currentTime = System.currentTimeMillis();

                            //Build chart data
                            SeriesData seriesData = new SeriesData();
                            seriesData.seriesName = statKey;

                            Iterator<String> iterator = statsNode.fieldNames();
                            while (iterator.hasNext()) {
                                String fieldName = iterator.next();

                                if (statFieldNames.contains(fieldName)) {
                                    SeriesPoint seriesPoint = new SeriesPoint();
                                    seriesPoint.title = fieldName;
                                    seriesPoint.x = currentTime;
                                    seriesPoint.y = Json.fromJson(statsNode.findValue(fieldName), Float.class);

                                    //Add the point to the series data
                                    seriesData.seriesPoints.add(seriesPoint);
                                }
                            }

                            out.write(Json.toJson(seriesData));

                        }
                }

                return Results.ok();
            }
        }
        );
    }
}
