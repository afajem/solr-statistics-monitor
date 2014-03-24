import org.apache.solr.client.solrj.embedded.JettySolrRunner;

import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.Play;

public class Global extends GlobalSettings {

    //Embedded Jetty wrapper for running a Solr process
    private JettySolrRunner solrRunner;

    /**
     * Invoked at the start of a play application. Global initializations can be performed here/
     *
     * @param app
     */
    @Override
    public void onStart(Application app) {

        Logger.info("Starting embedded Solr server...");

        try {
            //Instantiate and start the embedded Solr server
            solrRunner = new JettySolrRunner("solrnode", "/solr", 
            		Integer.parseInt(Play.application().configuration().getString("solr.port")));
            solrRunner.start(true);
        }
        catch (Exception e) {
            Logger.error("Unable to start the Solr server. Can't proceed any further.", e);
            throw new RuntimeException(e);
        }

        Logger.info("Embedded Solr server initialized.");

        Logger.info("Application has started");
    }


    /**
     * Invoked when the Play application terminates. Perform clean up tasks here.
     * @param app
     */
    @Override
    public void onStop(Application app) {

        if (solrRunner != null && solrRunner.isRunning()) {
            try {
                solrRunner.stop();
            }
            catch (Exception e) {
                Logger.error("Unable to properly shutdown the solr server; not much else to do.", e);
            }
        }

        Logger.info("Application shutdown...");
    }

}
