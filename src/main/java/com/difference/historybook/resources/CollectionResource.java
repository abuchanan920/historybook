/*
 * Copyright 2016 Andrew W. Buchanan (buchanan@difference.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.difference.historybook.resources;

import java.time.Instant;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.difference.historybook.index.Index;
import com.difference.historybook.index.IndexException;
import com.difference.historybook.index.SearchResultWrapper;

/**
 * A resource for interacting with an index collection
 */
@Path("/collections/{collection}")
public class CollectionResource {
	private static final Logger LOG = LoggerFactory.getLogger(CollectionResource.class);
	
	private final Index index;
	
	/**
	 * Constructor for CollectionResource
	 * @param index The @Index to submit index/search requests to
	 */
	public CollectionResource(Index index) {
		this.index = index;
	}

	/**
	 * Add a page to the search index using the current timestamp
	 * 
	 * Useful for realtime indexing.
	 * 
	 * @param collection the namespaced collection to store the page within
	 * @param url the URL of the page to be indexed
	 * @param body the textual content of the page to be indexed
	 * @return 
	 * @see Response
	 * @throws IndexException
	 */
	@POST
	@Path("/{url}")
	public Response postContentToCollection(
			@PathParam("collection") String collection,
			@PathParam("url") String url,
			String body
			) throws IndexException {
		return postContentToCollectionWithDate(collection, url, Instant.now(), body);
	}
	
	/**
	 * Add a page to the search index using the provided timestamp.
	 * 
	 * Useful for batch indexing from history.
	 * 
	 * @param collection the namespaced collection to store the page within
	 * @param url the URL of the page to be indexed
	 * @param timestampString the timestamp the page was fetched in ISO-8601 format
	 * @param body the textual content of the page to be indexed
	 * @return
	 * @see Response
	 * @throws IndexException
	 */
	@POST
	@Path("/{url}/{timestamp}")
	public Response postContentToCollectionWithTimestamp(
			@PathParam("collection") String collection,
			@PathParam("url") String url,
			@PathParam("timestamp") String timestampString,
			String body
			) throws IndexException {
		Instant timestamp = Instant.parse(timestampString);
		return postContentToCollectionWithDate(collection, url, timestamp, body);
	}
	
	private Response postContentToCollectionWithDate(
			String collection,
			String url,
			Instant timestamp,
			String body
			) throws IndexException {
		LOG.info("Received: {} {} {}", collection, url, timestamp.toString());
		index.indexPage(collection, url, timestamp, body);
		return Response.accepted().build(); //TODO: What is the correct response code?
	}
	
	//TODO: support date range queries

	/**
	 * Execute a search against the index
	 * 
	 * @param collection the namespaced collection to search within
	 * @param query the query to pass to the index
	 * @param offsetString 0 based offset within the search results (used for paging)
	 * @param sizeString maximum number of results to return
	 * @return the search results within a metadata wrapper
	 * @see Response
	 * @throws NumberFormatException
	 * @throws IndexException
	 */
	@GET
	@Produces("application/json")
	public Response getSearchResult(
			@PathParam("collection") String collection,
			@QueryParam("q") String query,
			@QueryParam("offset") @DefaultValue("0") String offsetString,
			@QueryParam("size") @DefaultValue("10") String sizeString
			) throws NumberFormatException, IndexException {
		LOG.info("Query: {} query:{} offset:{} size:{}", collection, query, offsetString, sizeString);
		SearchResultWrapper results = index.search(
				collection, query, 
				Integer.parseInt(offsetString), Integer.parseInt(sizeString));
		return Response.ok().entity(results).build();
	}

}
