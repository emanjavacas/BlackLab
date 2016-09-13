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
import java.nio.ByteBuffer;

import org.apache.lucene.search.spans.Spans;

import nl.inl.blacklab.search.Span;

/**
 * Gets spans for a certain XML element.
 */
class SpansTagsPayload extends BLSpans {

	private Spans tags;

	private int end = -1; // -1: not nexted yet. -2: payload not read yet.

	public SpansTagsPayload(Spans startTags) {
		this.tags = startTags;
	}

	@Override
	protected void passHitQueryContextToClauses(HitQueryContext context) {
		// NOP
	}

	@Override
	public void getCapturedGroups(Span[] capturedGroups) {
		// NOP
	}

	@Override
	public int nextDoc() throws IOException {
		end = -1; // not nexted yet
		return tags.nextDoc();
	}

	@Override
	public int advance(int target) throws IOException {
		end = -1; // not nexted yet
		return tags.advance(target);
	}

	@Override
	public int docID() {
		return tags.docID();
	}

	@Override
	public int nextStartPosition() throws IOException {
		end = -2; // payload not read yet
		return tags.nextStartPosition();
	}

	@Override
	public int startPosition() {
		return tags.startPosition();
	}

	@Override
	public int endPosition() {
		if (tags.startPosition() == NO_MORE_POSITIONS)
			return NO_MORE_POSITIONS;
		try {
			if (end == -2) {
				byte[] payload = tags.getPayload().iterator().next();
				ByteBuffer bb = ByteBuffer.wrap(payload);
				end = bb.getInt();
			}
			return end;
		} catch (IOException e) {
			throw new RuntimeException("Error getting payload");
		}

	}



}
