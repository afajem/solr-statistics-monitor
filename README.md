# Solr Statistics Monitor
 
## Overview
            
This is a Play Framework Activator Template that demonstrates a reactive dashboard which displays usage statistics data for an embedded [Apache Solr Server](http://lucene.apache.org/solr). To run the activator template you must have already installed the Type Activator. Please visit http://www.typesafe.com/platform/getstarted for more information on getting started.

Once installed clone this repository and double click on the `activator` or `activator.bat` to run the template.

The application, once compiled and running at [http://localhost:9000](http://localhost:9000), starts by displaying real-time statistics collected from an embedded [Apache Solr Server](http://lucene.apache.org/solr). The starting page will show a collection of charts that are updated periodically based on a predefined interval. The charts shown on the starting page are related to Cache Statistics that are collected by the Solr node; additional statistics are available by clicking the **Query Stats** tab. 

As Solr primarily records statistics only when there's an interaction with the core, a form is provided at the top of the page to simulate a query against the embedded server. When the query is issued against the embedded server, some of the graphs should be updated depending on the query that was submitted.
			
A toggle button is provided to pause the server side push of statistics data to the UI. Simply push the button again to resume the flow of data to the front end.

For more detailed information on how the template works, please refer to documentation provided within the template console.
          