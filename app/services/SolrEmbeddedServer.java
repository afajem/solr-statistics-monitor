package services;

import org.apache.solr.client.solrj.embedded.JettySolrRunner;
import play.Logger;
import play.Play;
import play.inject.ApplicationLifecycle;
import play.libs.F.*;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Manages the lifecycle of JettySolrRunner
 */
@Singleton
public class SolrEmbeddedServer implements EmbeddedServer{

    //Embedded Jetty wrapper for running a Solr process
    private JettySolrRunner solrRunner;

    @Inject
    public SolrEmbeddedServer(ApplicationLifecycle lifecycle) {
        Logger.info("Starting embedded Solr server...");

        try {
            //Instantiate and start the embedded Solr server
            solrRunner = new JettySolrRunner("solrnode", "/solr",
                    Integer.parseInt(Play.application().configuration().getString("solr.port")));
            solrRunner.start(true);

            lifecycle.addStopHook(() -> {
                if (solrRunner != null && solrRunner.isRunning()) {
                    try {
                        Logger.info("Shutting down Solr...");
                        solrRunner.stop();
                        Logger.info("Solr shutdown.");
                    }
                    catch (Exception e) {
                        Logger.error("Unable to properly shutdown the Solr server; not much else to do.", e);
                    }
                }

                return Promise.pure(null);
            });


        } catch (Exception e) {
            Logger.error("Unable to start the Solr server. Can't proceed any further.", e);
            throw new RuntimeException(e);
        }
        Logger.info("Embedded Solr server initialized.");

    }
}
