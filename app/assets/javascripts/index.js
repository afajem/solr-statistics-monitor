$(document).ready(function() {

    var charts = {};

    var webSocket = new WebSocket($("#charts").data("ws-url"));

    var running = true;

    //Handle inbound messages on the web socket connection
    webSocket.onmessage = function(event) {
        var message;
        message = JSON.parse(event.data);


        //Fetch matching chart
        var chart = charts[message.seriesName];

        var seriesPoints = message.seriesPoints;

        //initialize the series if necessary
        if (!chart.series || chart.series.length === 0) {
            for (var i = 0; i < seriesPoints.length; ++i) {
                chart.addSeries({
                    name: seriesPoints[i].title,
                    data: []
                });
            }
        }

        // prepare series
        var series = chart.series[0],
            shift = series.data.length > 20; // shift if the series is longer than 20 ticks

        // update series
        for (var j = 0; j < seriesPoints.length; ++j) {
            // add current point
            var point = seriesPoints[j];
            chart.series[j].addPoint([point.x, point.y], true, shift);
        }

    };

    Highcharts.setOptions({
        global : {
            useUTC : false
        }
    });

	//Locate all chart containers...use id attr as the key
	$(".chart-container").each(function() {
		var container = $(this);

		var statsKey = container.attr("id");

		// Create a chart for the current stats key
        var chart = new Highcharts.Chart({
            chart: {
                renderTo: statsKey,
                defaultSeriesType: 'spline'
            },
            title: {
                text: ' '
            },
            xAxis: {
                type: 'datetime',
                tickPixelInterval: 150,
                maxZoom: 20 * 1000
            },
            yAxis: {
                minPadding: 0.2,
                maxPadding: 0.2,
                title: {
                    text: 'Value',
                    margin: 80
                }
            }
        });

        //Add chart reference to the map
        charts[statsKey] = chart;

	});


	/*
	 * Handle click event on stats pause/resume button 
	 */
    $(".pause-resume-btn>button").click(function() {
        var pauseButton = $(this);

        if (running) {
            webSocket.send(JSON.stringify({"message": pauseButton.data("pause-message")}));

            running = false;

            var pauseButtonLabel = pauseButton.find(".glyphicon.glyphicon-pause");
            pauseButtonLabel.removeClass("glyphicon-pause").addClass("glyphicon-play");
            pauseButtonLabel.text(pauseButton.data("play-tooltip"));
        }
        else {
            webSocket.send(JSON.stringify({"message": pauseButton.data("resume-message")}));

            running = true;

            var resumeButtonLabel = pauseButton.find(".glyphicon.glyphicon-play");
            resumeButtonLabel.removeClass("glyphicon-play").addClass("glyphicon-pause");
            resumeButtonLabel.text(pauseButton.data("pause-tooltip"));
        }
    }) ;
    

    /*
     * Handle Simulating a search request
     */
    var handleSearch = function() {
		var keywordsField = $("#keywords");
	
		var keywords = keywordsField.val();
	
		if (keywords) {
			var request = $.ajax({
				url : $("#searchForm").attr("action")
						+ escape(keywords),
				type : "POST"
			});
	
			request.done(function(msg) {
				keywordsField.parent(".form-group").addClass(
						"has-success");
			});
	
			request.fail(function(jqXHR, textStatus) {
				keywordsField.parent(".form-group").addClass(
						"has-error");
			});
	
		}
	};
    

    /*
     * Handle enter key to initiate ajax search.
     */
	$("#keywords").keypress(function(event) { 
		//clear our previous border, if exists
		$(this).parent(".form-group").removeClass("has-success");

		if (event.keyCode == 13) {
			event.preventDefault();
			
			handleSearch();
			return false;
		}
	});

	
	/*
	 * Handle submit of query simulation 
	 */
    $("#searchButton").click(function() {
		handleSearch();
    });

});

