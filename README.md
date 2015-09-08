# Solr Statistics Monitor
 
## Overview
            
This is a Typesafe Activator Template that demonstrates a reactive dashboard, which displays usage statistics data for an embedded [Apache Solr Server](http://lucene.apache.org/solr). To run the activator template you must have already installed the Type Activator. Please visit http://www.typesafe.com/platform/getstarted for more information on getting started.

Once installed clone this repository and double click on the `activator` or `activator.bat` to run the template.

The application, once compiled and running at [http://localhost:9000](http://localhost:9000), starts by displaying real-time statistics collected from an embedded [Apache Solr Server](http://lucene.apache.org/solr). The starting page will show a collection of charts that are updated periodically based on a predefined interval. The charts shown on the starting page are related to Cache Statistics that are collected by the Solr node; additional statistics are available by clicking the **Query Stats** tab. 

As Solr primarily records statistics only when there's an interaction with the core, a form is provided at the top of the page to simulate a query against the embedded server. When the query is issued against the embedded server, some of the graphs should be updated depending on the query that was submitted.
			
A toggle button is provided to pause the server side push of statistics data to the UI. Simply push the button again to resume the flow of data to the front end.


## Reactive Dashboard

The application attempts to demonstrate how the Play Framework can be used to create a dashboard application that showcases some of the key tenets of a Reactive Application. The application, once started, establishes a WebSocket connection to the Server for the desired statistics type (e.g. CACHE or QUERYHANDLER). Behind the WebSocket connection is an Akka-based Actor, [StatsActor.java](app/actors/StatsActor.java) that fetches the statistics data from the embedded Solr server on the behalf of the user. The returned statistics data is pushed to the browser on the output channel of the WebSocket connection. The use of a WebSocket connection between the browser and the backed Play Framework application demonstrates a lightweight and efficient protocol for pushing statistics information to the browser as it becomes available; the browser doesn't poll the server for this information. 

The application embraces a responsive design by ensuring that a dedicated actor services the browser's initial connection by pushing retrieved statistics data  to it. More importantly, this direct connection ensures that there's a near constant response time for servicing  browser requests; in other words guaranteeing almost linear scalability of the application regardless of the number of browser applications requesting statistics data.

A reactive interaction between the user (browser) and the application is demonstrated as in the user's ability to pause/restart the flow of statistics data from the "backing" actor. The actor receives the asynchronous non-blocking request from the client to stop the flow of data from the dedicated actor. Such a request simply stops the actor, releasing system resources back to the server. Subsequent requests to start the actor simply schedules a new actor to start serving new statistics data to the browser. The browser maintains the same WebSocket connection and as such is not aware of the intricacies of the underlying interaction between both tiers. The search simulation use case also demonstrates a reactive interaction between the application and the user; where a search request is simulated by the user to demonstrate a change in the statistical data returned from the embedded Solr instance.  

Finally the search simulation provides a responsive example where a user gets to interact with the Solr server by submitting a search query and the Play application _reacts_ to the search by pushing the updated statistics through the existing WebSocket connection.   

## Embedded Solr Server

A key component of the application is the embedded [Solr Server](http://lucene.apache.org/solr) that is the source for the statistical data that is displayed to the user. The Solr server is started from the [SolrEmbeddedServer.java](app/services/SolrEmbeddedServer.java) class. This class starts the embedded server on the defined port in the [application.conf](conf/application.conf) file. The Solr server search index configurations are located in the [solrnode](solrnode) directory.

The embedded Solr server is pre-loaded with a Wikipedia export of a lists of various places in the world. The compressed XML file used to load the index has been included in the source at [wikipedia-export.zip](wikipedia-export.zip). This file can be used to refresh the search index.

## Real-Time Charts

At the core of the client-side UI is the [Twitter Bootstrap](http://getbootstrap.com/) framework which is used to layout the page. On initial request of the application at [http://localhost:9000](http://localhost:9000), the route specified in the [routes](conf/routes) file at: ``GET / controllers.Application.index`` is invoked. This handler in [StatsController.java](app/controllers/StatsController.java) will retrieve the statistic types for the **CACHE** statistics category. The statistics types are aggregated and passed into the [stats.scala.html](app/views/stats.scala.html) Scala template file. The page loads the CSS and JavaScript libraries needed to setup the interaction between the front-end and back-end services and specifies the region in which the dynamic content will reside. For each statistic type returned, a chart is rendered and prepared for receiving data.  

The UI uses JQuery for most JavaScript interactions. Once the page is loaded, the UI creates a WebSocket connection and submits it to the server along with the statistics category type at the route: ``GET  /ws/:statType  controllers.Application.ws(statType : String)``. This route in the controller will initialize the actor represented by the [StatsActor.java](app/actors/StatsActor.java) class with the specific statistics category type. The actor is provided a reference to the WebSocket connection and from this point onwards will push statistics data retrieved from the Solr server through the connection in real-time. The actor is scheduled to run on a predefined interval that is configurable in the [application.conf](conf/application.conf) file. 

The user has the ability to **pause** the flow of statistic data from the actor by clicking on the **pause** button. This request is submitted over the existing WebSocket connection; as is the case for any interaction that needs to occur between the UI and the actor. A pause message sent to the actor simply cancels the actor, which releases any associated resources.  In the absence of data, the chart will no longer be updated as there is no data originating from the WebSocket connection. The state of the button is toggled such that a subsequent submit will **resume** the actor; simply starting another "cancellable" actor instance.

The UI uses the [Highcharts](http://www.highcharts.com/) charting framework for displaying statistics data.  

NOTE: The application currently only supports two statistics category types: **CACHE** and **QUERYHANDLER**, but additional categories can be easily added.)
         

## Search Simulation

As previously stated, the user has the ability to initiate search requests against the embedded Solr server. The search requests are needed to allow the server to record new statistics information which can then be pushed out to the UI and displayed in the charts. A form is provided at the top of the page that allows for basic queries to the submitted. 

The searches provided through this form only impacts a few of the statistic types. As the embedded Solr server listens on a port that is readily accessible outside of the application, one can submit additional (more sophisticated) queries and updates that will impact more statistics type displayed in UI. Please refer to the Solr Wiki for more information on [Querying](http://wiki.apache.org/solr/CommonQueryParameters) and [Updating](https://wiki.apache.org/solr/UpdateXmlMessages) a Solr index.
