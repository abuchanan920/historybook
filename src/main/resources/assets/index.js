$(document).ready(function () {
   var query = GetParameterValues('q');
   if (query) query = decodeURIComponent(query);

   var debug = GetParameterValues('debug');
   if (debug) $('.debug').show();

   $('.search-form').submit(function(e) {
     e.preventDefault();
     window.location = '/search?q=' + encodeURIComponent($('#query').val());
   });

   if (query) {
     $('#query').val(query);

     var queryUrl = '/collections/default?q=' + encodeURIComponent(query);
     if (debug == 'true') queryUrl += '&debug=true';

     $.ajax({
       url: queryUrl
      })
      .done(function(msg) {
        if (msg.results.length > 0) {
          $('#status').text("Search results for " + query);
          if (msg.debugInfo) $('#status-debug').text(msg.debugInfo);

          for (var i = 0; i < msg.results.length; i++) {
            var result = msg.results[i];

            var html = [
              "<div class='row'>",
              "  <div class='col-md-12'>",
              "    <h4><a href='" + result.url + "'>" + result.title + "</a></h4>",
              "  </div>",
              "  <div class='col-md-12'>",
              "    <p><a href='" + result.url + "'>" + result.url + "</a></p>",
              "  </div>",
              "  <div class='col-md-3'>",
              "    <p><nobr>" + result.timestamp + "</nobr></p>",
              "  </div>",
              "  <div class='col-md-3'>",
              "    <p>" + result.score + "</p>",
              "  </div>",
              "  <div class='col-md-12'>",
              "    <p>" + result.snippet + "</p>",
              "  </div>"
            ];
            if (result.debugInfo) {
              html = html.concat([
                "  <div class='col-md-12'>",
                "    <pre>" + result.debugInfo + "</pre>",
                "  </div>"
              ]);
            }
            html.push("</div>");

            var $html = $(html.join("\n"));

            $('#results').append($html);
          }
        } else {
          $('#status').text("No results found for " + query);
        }
      });
   }

   function GetParameterValues(param) {
       var url = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
       for (var i = 0; i < url.length; i++) {
           var urlparam = url[i].split('=');
           if (urlparam[0] == param) {
               return urlparam[1];
           }
       }
   }
});
