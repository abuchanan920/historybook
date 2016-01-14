# Overview
HistoryBook is a browser history search engine that enables you to perform searches against the content of web pages you have previously visited.

We have all had this problem - we are trying to find a page we were looking at a few weeks/months ago. We try Google, and can't seem to find the right page. We try searching our browser history, but it only searches page titles. HistoryBook is the solution you have been looking for! It searches the actual content of those pages so you can find that page again.

# Getting Started
Note that HistoryBook is in alpha state. There are numerous features that need to be implemented before you would want to use it in "production". 

For example:

* the proxy can't download large files in a memory efficient way yet
* security/privacy concerns have not been addressed yet
* App is a Minimum Viable Product (with an emphasis on *minimum*)

Still here?

First, start the service:

```
prompt> ./gradlew run
```

Then set your browser proxy to point to ```localhost:8082```
On a Mac, you can do this at:
System Preferences -> Network -> Advanced -> Proxies -> Web Proxy (HTTP)

Start browsing. As you browse, the pages you visit will be added to the search index in real time.

To perform a search, point your browser to [http://localhost:8080/search]()

Note that you can get search engine debug information (useful for development) by appending "&debug=true" to the URL.

If you are using Chrome and want to bulk load your browser history, take a look at the historybook-import project.

# Architecture

HistoryBook consists of a single process that exposes two services.

One is web service that exposes a search index allowing searching and adding pages to the index. It also has an endpoint that provides a web user interface to the search. Lucene is used as the backend search engine.

The second service is a web proxy that transparently adds viewed pages to the search index. 

# License
Copyright 2016 Andrew W. Buchanan (buchanan@difference.com)

Licensed under the Apache License, Version 2.0