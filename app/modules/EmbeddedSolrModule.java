package modules;

import com.google.inject.AbstractModule;
import services.EmbeddedServer;
import services.SolrEmbeddedServer;

/**
 * Manages the lifecycle of the embedded Solr server
 */
public class EmbeddedSolrModule extends AbstractModule {

    protected void configure() {
        bind(EmbeddedServer.class).to(SolrEmbeddedServer.class);
    }
}
