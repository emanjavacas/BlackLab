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
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.util.Bits;

/**
 * Makes sure the resulting hits do not contain consecutive duplicate hits. These may arise when
 * e.g. combining multiple SpanFuzzyQueries with OR.
 */
public class SpanQueryUnique extends SpanQuery {
	private SpanQuery src;

	public SpanQueryUnique(SpanQuery src) {
		this.src = src;
	}

	@Override
	public Spans getSpans(LeafReaderContext context, Bits acceptDocs, Map<Term,TermContext> termContexts)  throws IOException {
		Spans srcSpans = src.getSpans(context, acceptDocs, termContexts);
		if (srcSpans == null)
			return null;
		return new SpansUnique(srcSpans);
	}

	@Override
	public String toString(String field) {
		return "SpanQueryUnique(" + src + ")";
	}

	@Override
	public String getField() {
		return src.getField();
	}

	@Override
	protected void extractTerms(Set<Term> terms) {
		try {
			// FIXME: temporary extractTerms hack
			Method methodExtractTerms = SpanQuery.class.
			        getDeclaredMethod("extractTerms", Set.class);
			methodExtractTerms.setAccessible(true);

		    methodExtractTerms.invoke(src, terms);
			//src.extractTerms(terms);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
