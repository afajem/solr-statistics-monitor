import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import play.Configuration;
import play.Play;
import play.mvc.Result;
import play.test.Helpers;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;


/**
 * General Application tests
 *
 */
public class ApplicationTest extends Helpers {
	private Configuration additionalConfiguration;

	@Before
	public void init() {
		Config testConfig =  ConfigFactory.parseFile(new File("conf/test.conf"));
		
		additionalConfiguration = new Configuration(testConfig);
	}
	
	
	@Test
	public void testSimulateSearch() {
		running(testServer(9999, fakeApplication(additionalConfiguration.asMap())), new Runnable() {
			public void run() {
			    Result result = callAction(controllers.routes.ref.Application.simulateSearch(
				    	Play.application().configuration().getString("test.query")));   
				    
			    assertThat(status(result)).isEqualTo(OK);
			}
		});
	}	
}
