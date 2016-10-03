/*******************************************************************************
 * Copyright (c) 2010, 2012 Institute for Dutch Lexicology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package nl.inl.blacklab.search.lucene;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanWeight;
import org.apache.lucene.search.spans.Spans;

/**
 * Returns all n-grams of certain lengths.
 */
public class SpanQueryNGrams extends SpanQueryBase {

	/** if true, we assume the last token is always a special closing token and ignore it */
	boolean ignoreLastToken = false;

	private int min;

	private int max;

	/**
	 * Returns all n-grams of certain lengths
	 * @param ignoreLastToken ignore special closing token
	 * @param fieldName what field to produce n-grams for
	 * @param min minimum n-gram length
	 * @param max maximum n-gram length
	 */
	public SpanQueryNGrams(boolean ignoreLastToken, String fieldName, int min, int max) {
		this.ignoreLastToken = ignoreLastToken;
		baseFieldName = fieldName;
		this.min = min;
		this.max = max;
		clauses = new SpanQuery[0];
	}

	@Override
	public SpanWeight createWeight(final IndexSearcher searcher, boolean needsScores) throws IOException {
		return new SpanWeight(SpanQueryNGrams.this, searcher, null) {
			@Override
			public void extractTerms(Set<Term> terms) {
				// No terms
			}

			@Override
			public void extractTermContexts(Map<Term, TermContext> contexts) {
				// No terms
			}

			@Override
			public Spans getSpans(final LeafReaderContext context, Postings requiredPostings) throws IOException {
				return new SpansNGrams(ignoreLastToken, context.reader(), baseFieldName, min, max);
			}
		};
	}

	@Override
	public String toString(String field) {
		return "SpanQueryNGrams(" + min + ", " + max + ")";
	}

	/** Set whether to ignore the last token.
	 *
	 * @param ignoreLastToken if true, we assume the last token is always a special closing token and ignore it
	 */
	public void setIgnoreLastToken(boolean ignoreLastToken) {
		this.ignoreLastToken = ignoreLastToken;
	}

	@Override
	public Query rewrite(IndexReader reader) throws IOException {
		return this; // cannot rewrite
	}

}
