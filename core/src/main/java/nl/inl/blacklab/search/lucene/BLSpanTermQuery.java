package nl.inl.blacklab.search.lucene;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.spans.SpanWeight;

import nl.inl.blacklab.index.complex.ComplexFieldUtil;

/**
 * BL-specific subclass of SpanTermQuery that changes what getField() returns
 * (the complex field name instead of the full Lucene field name) in order to be
 * able to combine queries in different Lucene fields using AND and OR. Also makes
 * sure the SpanWeight returned by createWeight() produces a BLSpans, not a regular
 * Spans.
 */
public class BLSpanTermQuery extends SpanTermQuery {

	/** Construct a SpanTermQuery matching the named term's spans.
	 *
	 * @param term term to search
	 */
	public BLSpanTermQuery(Term term) {
		super(term);
	}

	public BLSpanTermQuery(SpanTermQuery termQuery) {
		super(termQuery.getTerm());
	}

	/**
	 * Expert: Construct a SpanTermQuery matching the named term's spans, using
	 * the provided TermContext.
	 *
	 * @param term term to search
	 * @param context TermContext to use to search the term
	 */
	public BLSpanTermQuery(Term term, TermContext context) {
		super(term, context);
	}

	/**
	 * Overriding getField to return only the name of the complex field, not the
	 * property name or alt name following after that.
	 *
	 * i.e. for 'contents%lemma@s', this method will just return 'contents',
	 * which is the complex field we're searching.
	 *
	 * This makes it possible to use termqueries with different fieldnames in
	 * the same AND or OR query.
	 *
	 * @return String field
	 */
	@Override
	public String getField() {
		return ComplexFieldUtil.getBaseName(term.field());
	}

	@Override
	public SpanWeight createWeight(IndexSearcher searcher, boolean needsScores) throws IOException {
		final TermContext context;
		final IndexReaderContext topContext = searcher.getTopReaderContext();
		if (termContext == null || termContext.topReaderContext != topContext) {
			context = TermContext.build(topContext, term);
		} else {
			context = termContext;
		}
		Map<Term, TermContext> contexts = needsScores ? Collections.singletonMap(term, context) : null;
		SpanTermWeight weight = new SpanTermWeight(context, searcher, contexts);
		return new BLSpanWeightWrapper(weight, searcher, contexts);
	}

	@Override
	public String toString(String field) {
		return "BL" + super.toString(field);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ 0xB1ACC1AB;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj)) {
			return false;
		}
		BLSpanTermQuery other = (BLSpanTermQuery) obj;
		return term.equals(other.term);
	}

	public static BLSpanTermQuery from(SpanTermQuery q) {
		return new BLSpanTermQuery(q);
	}

}
