import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.File;
import java.util.List;

import org.junit.*;

import play.*;
import play.Logger;
import play.libs.*;
import play.libs.ws.*;
import play.test.Helpers;
import actors.StatsActor;
import akka.actor.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.*;

import controllers.StatisticsConstants;

import javax.inject.Inject;


/**
 * Test Actor Capabilities
 *
 */
public class StatsActorTest extends Helpers {
	private Configuration additionalConfiguration;

	@Before
	public void init() {
		Config testConfig =  ConfigFactory.parseFile(new File("conf/test.conf"));
		
		additionalConfiguration = new Configuration(testConfig);
	}
	
	
	/**
	 * Test call for query statistics
	 */
	@Test
	public void testQueryStats() {
		running(testServer(9999, fakeApplication(additionalConfiguration.asMap())), new Runnable() {
			public void run() {
				validateStats(StatisticsConstants.QUERY_STATS_MESSAGE,
						StatisticsConstants.QUERY_STATS_KEYS,
						StatisticsConstants.QUERY_STATS_FIELD_NAMES);
			}
		});
	}

	
	/**
	 * Test call for cache statistics
	 */
	@Test
	public void testCacheStats() {
		running(testServer(9999, fakeApplication(additionalConfiguration.asMap())), new Runnable() {
			public void run() {
				validateStats(StatisticsConstants.CACHE_STATS_MESSAGE,
						StatisticsConstants.CACHE_STATS_KEYS,
						StatisticsConstants.CACHE_STATS_FIELD_NAMES);
			}
		});
	}


	/**
	 * Utility method to validate specified stat type
	 */
	private void validateStats( final String statsMessage, 
		final List<String> statsKeys, final List<String> statFieldNames) {
		
		StubWebSocketOut out = new StubWebSocketOut();
		
        ActorRef statsActor = Akka.system().actorOf(
        		Props.create(StatsActor.class, out, statsMessage, WS.client()));

         Logger.info("Sending test Statistic message targeted at [" + statsMessage + "]...");
         
         statsActor.tell(statsMessage, statsActor);

         //To ensure test integrity, utilizing the same refresh interval for our pause duration
         //in scheduled actor...
         try { 
        	 Thread.sleep(Play.application().configuration().getLong("stats.refresh.interval")*1000);
         }
         catch (Exception e) {
        	 throw new RuntimeException(e);
         }
         
		JsonNode node = out.getNode();
		 
		//Expecting data back for the query
		assertThat(node, notNullValue());
		
		Logger.info("Received valid non-null results node for [" + statsMessage + "] sent to actor.");
		
		//# of stats fetched should equal to the stat keys defined in the config
		List<JsonNode> statKeyNames = node.findValues("seriesName");

		Logger.info("Validating  statistics keys in  results node  for [" + statsMessage + "] match predefined values...");

		for (JsonNode statKeyName : statKeyNames) {
			assertTrue(statsKeys.contains(statKeyName.asText()));
			Logger.info("Valid match found for statistics key: [" + statKeyName + "]");
		}
		
		//Series points can't be null and must be an array format
		JsonNode seriesPoints = node.get("seriesPoints");

		Logger.debug("seriesPoints: " + seriesPoints);

		assertThat(seriesPoints, notNullValue());
		assertTrue(seriesPoints.isArray());
		Logger.info("Received valid non-null list of SeriesPoints.");
		
		//Validate field names defined in config matches the response
		Logger.info("Validating  statistics field names in each SeriesPoint matches predefined values...");

		for (JsonNode seriesPointNode : seriesPoints) {
			assertTrue(statFieldNames.contains(seriesPointNode.get("title").textValue()));
			Logger.info("Valid match found for statistics field name: [" + seriesPointNode.get("title") + "]");
		}
	}

}
