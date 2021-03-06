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

package com.difference.historybook.index.lucene;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.queries.function.FunctionQuery;
import org.apache.lucene.queries.function.valuesource.LongFieldSource;
import org.apache.lucene.queries.function.valuesource.ReciprocalFloatFunction;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.grouping.GroupingSearch;
import org.apache.lucene.search.grouping.TopGroups;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.difference.historybook.index.Index;
import com.difference.historybook.index.IndexException;
import com.difference.historybook.index.SearchResult;
import com.difference.historybook.index.SearchResultWrapper;
import com.difference.historybook.textutils.HtmlTextExtractor;

/**
 * An implementation of @Index backed by Lucene
 */
public class LuceneIndex implements Index {
	private static final Logger LOG = LoggerFactory.getLogger(LuceneIndex.class);
	
	private static final String INDEXDIR = "index";
	
	// reciprical of number of seconds in year
	private static final float RECIP = 1F / (60 * 60 * 24 * 365);
		
	private final Path path;
	private final Directory dir;
	private final IndexWriter writer;
	private IndexReader reader;
	private IndexSearcher searcher;
	private final Analyzer analyzer;
	private final QueryParser parser;
	
	/**
	 * Constructor for LuceneIndex
	 * 
	 * @param dataDirectory   Path to the directory to create an index directory within.
	 * @throws IndexException
	 */
	public LuceneIndex(Path dataDirectory) throws IndexException {
		
		//TODO: Check to make sure directory is read/writable
		path = dataDirectory.resolve(INDEXDIR);
		
		try {
			dir = FSDirectory.open(path);
			analyzer = new StandardAnalyzer();
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			writer = new IndexWriter(dir, iwc);

			reader = DirectoryReader.open(writer, false);
			searcher = new IndexSearcher(reader);
			parser = new QueryParser(IndexDocumentAdapter.FIELD_SEARCH, analyzer);
		} catch (IOException e) {
			LOG.error(e.getLocalizedMessage());
			throw new IndexException(e);
		}
	}
	
	@Override
	public void indexPage(
			String collection, 
			String url, 
			Instant timestamp, 
			String body) throws IndexException {
		HtmlTextExtractor extractor = new HtmlTextExtractor(body, url);
		
		Document doc = new IndexDocumentAdapter()
				.setCollection(collection)
				.setUrl(url)
				.setTimestamp(timestamp)
				.setTitle(extractor.getTitle())
				.setContent(extractor.getContent())
				.getAsDocument();
						
		try {
			writer.addDocument(doc);
			writer.commit();
			reader.close();
			reader = DirectoryReader.open(writer, false);
			searcher = new IndexSearcher(reader);
		} catch (IOException e) {
			LOG.error(e.getLocalizedMessage());
			throw new IndexException(e);
		}
	}
	
	@Override
	public SearchResultWrapper search(
			String collection, String query, 
			int offset, int size, boolean includeDebug) throws IndexException {
		try {
			//TODO: make age be a component in the ranking?
			BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
			queryBuilder.add(parser.parse(query), Occur.MUST);
			queryBuilder.add(new TermQuery(new Term(IndexDocumentAdapter.FIELD_COLLECTION, collection)), Occur.FILTER);
			Query baseQuery = queryBuilder.build();

			FunctionQuery boostQuery = new FunctionQuery(
					new ReciprocalFloatFunction(
							new DurationValueSource(
									new Date().getTime()/1000, 
									new LongFieldSource(IndexDocumentAdapter.FIELD_TIMESTAMP)), 
							RECIP, 1F, 1F));
			
			Query q = new CustomScoreQuery(baseQuery, boostQuery);
			
			QueryScorer queryScorer = new QueryScorer(q, IndexDocumentAdapter.FIELD_SEARCH);
			Fragmenter fragmenter = new SimpleSpanFragmenter(queryScorer);
			Highlighter highlighter = new Highlighter(queryScorer);
			highlighter.setTextFragmenter(fragmenter);
			
			GroupingSearch gsearch = new GroupingSearch(IndexDocumentAdapter.FIELD_URL_GROUP)
					.setGroupDocsLimit(1)
					.setAllGroups(true)
					.setIncludeMaxScore(true);
			TopGroups<?> groups = gsearch.search(searcher, q, offset, size);
			
			ArrayList<SearchResult> results = new ArrayList<>(size);
			for (int i = offset; i < offset + size && i < groups.groups.length; i++) {
				ScoreDoc scoreDoc = groups.groups[i].scoreDocs[0];
				Document luceneDoc = searcher.doc(scoreDoc.doc);
				IndexDocumentAdapter doc = new IndexDocumentAdapter(luceneDoc);

				TokenStream tokenStream = TokenSources.getTokenStream(
						IndexDocumentAdapter.FIELD_SEARCH, 
						reader.getTermVectors(scoreDoc.doc), 
						luceneDoc.get(IndexDocumentAdapter.FIELD_SEARCH), 
						analyzer, 
						highlighter.getMaxDocCharsToAnalyze() - 1);
				
	            String[] snippets = highlighter.getBestFragments(tokenStream, luceneDoc.get(IndexDocumentAdapter.FIELD_SEARCH), 3);
	            String snippet = Arrays.asList(snippets).stream().collect(Collectors.joining("\n"));
	            snippet = Jsoup.clean(snippet, Whitelist.simpleText());
	            
	            String debugInfo = null;
	            if (includeDebug) {
	            	Explanation explanation = searcher.explain(q, scoreDoc.doc);
	            	debugInfo = explanation.toString();
	            }
	            
				results.add(new SearchResult(
						doc.getKey(),
						doc.getCollection(),
						doc.getTitle(),
						doc.getUrl(),
						doc.getDomain(),
						doc.getTimestampText(),
						snippet,
						debugInfo,
						scoreDoc.score));
			}
			
			SearchResultWrapper wrapper = new SearchResultWrapper()
					.setQuery(query)
					.setOffset(offset)
					.setMaxResultsRequested(size)
					.setResultCount(groups.totalGroupCount != null ? groups.totalGroupCount : 0)
					.setResults(results);
			
			if (includeDebug) {
				wrapper.setDebugInfo(q.toString());
			}
			
			return wrapper;
			
		} catch (IOException | ParseException | InvalidTokenOffsetsException e) {
			LOG.error(e.getLocalizedMessage());
			throw new IndexException(e);
		}
	}
	
	@Override
	public void close() throws IndexException {
		try {
			writer.close();
		} catch (IOException e) {
			LOG.error(e.getLocalizedMessage());
			throw new IndexException(e);
		}
	}
}
