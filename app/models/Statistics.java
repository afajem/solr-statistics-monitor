package models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Statistics {

    public String key;
    public String name;
    
    //Cache
    public long lookups;
    public long hits;
    public long  inserts;
    public long evictions;
    public long size;

    //Query
    public long requests;
    public long errors;
    public long timeouts;
    public long totalTime;
    public long avgTimePerRequest;

    public long timestamp;
}
