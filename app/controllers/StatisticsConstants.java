package controllers;

import java.text.MessageFormat;
import java.util.List;

import play.Play;

public final class StatisticsConstants {
	
	public static final String SOLR_MBEAN_URL = 
	    	MessageFormat.format(
    			Play.application().configuration().getString("solr.mbeans.url"),
    			Play.application().configuration().getString("solr.port"));

	public static final String SOLR_QUERY_URL = 
	    	MessageFormat.format(
    			Play.application().configuration().getString("solr.query.url"),
    			Play.application().configuration().getString("solr.port"));

    public static final List<String> CACHE_STATS_FIELD_NAMES =
            Play.application().configuration().getStringList("cache.stats.field.names");

    public static final List<String> CACHE_STATS_KEYS =
            Play.application().configuration().getStringList("cache.stats.keys");
    
    public static String CACHE_STAT_CATEGORY = "CACHE";

    public static final List<String> QUERY_STATS_FIELD_NAMES =
            Play.application().configuration().getStringList("query.stats.field.names");

    public static final List<String> QUERY_STATS_KEYS =
            Play.application().configuration().getStringList("query.stats.keys");

    public static String QUERY_STAT_CATEGORY = "QUERYHANDLER";

    // Actor messages. Strings for now...
    public static final String CACHE_STATS_MESSAGE = "CacheStats";
    public static final String QUERY_STATS_MESSAGE = "QueryStats";
    public static final String PAUSE_MESSAGE = "Pause";
    public static final String RESUME_MESSAGE = "Resume";


    public static final String CACHE_STAT_TYPE = "cache";
    public static final String QUERY_STAT_TYPE = "query";
    
    //Message keys for the page content title
    public static final String CACHE_PAGE_TITLE_KEY = "content.title.cache.stats";
    public static final String QUERY_PAGE_TITLE_KEY = "content.title.query.stats";


}