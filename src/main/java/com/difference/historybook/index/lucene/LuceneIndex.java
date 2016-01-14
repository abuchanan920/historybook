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
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
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
			//TODO: need to filter to collection
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
			Query q = queryBuilder.build();
			
			QueryScorer queryScorer = new QueryScorer(q, IndexDocumentAdapter.FIELD_SEARCH);
			Fragmenter fragmenter = new SimpleSpanFragmenter(queryScorer);
			Highlighter highlighter = new Highlighter(queryScorer);
			highlighter.setTextFragmenter(fragmenter);
			
			TopDocs docs = searcher.search(q, offset + size);
			ScoreDoc[] hits = docs.scoreDocs;
			
			ArrayList<SearchResult> results = new ArrayList<>(size);
			for (int i = offset; i < offset + size && i < hits.length; i++) {
				ScoreDoc scoreDoc = hits[i];
				Document luceneDoc = searcher.doc(scoreDoc.doc);
				IndexDocumentAdapter doc = new IndexDocumentAdapter(luceneDoc);

				TokenStream tokenStream = TokenSources.getTokenStream(
						IndexDocumentAdapter.FIELD_SEARCH, 
						reader.getTermVectors(scoreDoc.doc), 
						luceneDoc.get(IndexDocumentAdapter.FIELD_SEARCH), 
						analyzer, 
						highlighter.getMaxDocCharsToAnalyze() - 1);
	            String snippet = highlighter.getBestFragment(tokenStream, luceneDoc.get(IndexDocumentAdapter.FIELD_SEARCH));
	            
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
					.setResultCount(docs.totalHits)
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
