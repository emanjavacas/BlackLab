package nl.inl.blacklab.indexers;

import java.io.Reader;

import nl.inl.blacklab.index.DocIndexerXmlHandlers;
import nl.inl.blacklab.index.HookableSaxHandler.ContentCapturingHandler;
import nl.inl.blacklab.index.HookableSaxHandler.ElementHandler;
import nl.inl.blacklab.index.Indexer;
import nl.inl.blacklab.index.complex.ComplexFieldProperty;
import nl.inl.util.StringUtil;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.xml.sax.Attributes;

/**
 * Index a MBG file
 *
 */
public class DocIndexerMBG extends DocIndexerXmlHandlers {

    public DocIndexerMBG(Indexer indexer, String fileName, Reader reader) {
	super(indexer, fileName, reader);

	addHandler("mbg", new DocumentElementHandler());

        // header metadata
	addHandler("/mbg/header/corpusFile/author", new MetadataElementHandler() {
		@Override
		public void startElement(String uri, String localName, String qName,
					 Attributes attributes) {
		    super.startElement(uri, localName, qName, attributes);
		    String authorId = attributes.getValue("id");
		    if (authorId == null) {
			System.out.println("Author id is missing from document");
			return;
		    }
		    addMetadataField("authorId", authorId);
		}
		@Override
		public void endElement(String uri, String localName, String qName) {
		    super.endElement(uri, localName, qName);
		    addMetadataField("author", getElementContent());
		}
	    });
	
	addHandler("/mbg/header/corpusFile/date", new MetadataElementHandler());
	addHandler("/mbg/header/corpusFile/translation", new MetadataElementHandler());	
	addHandler("/mbg/header/corpusFile/genre", new MetadataElementHandler());
	addHandler("/mbg/header/corpusFile/PTC", new MetadataElementHandler());
	addHandler("/mbg/header/corpusFile/textForm", new MetadataElementHandler());

        // extra properties
	// main properties
	final ComplexFieldProperty propMain = getMainProperty();
	final ComplexFieldProperty propPunct = getPropPunct();

	// Doc handler
	final ElementHandler doc = addHandler("doc", new ElementHandler() {
		@Override
		public void startElement(String uri, String localName, String qName,
					 Attributes attributes) {
		    consumeCharacterContent();
		}
		@Override
		public void endElement(String uri, String localName, String qName) {
		    propPunct.addValue(StringUtil.normalizeWhitespace(consumeCharacterContent().trim()));
		    super.endElement(uri, localName, qName);
		}
	    });
    
	addHandler("w", new WordHandlerBase() {
		@Override
		public void startElement(String uri, String localName, String qName,
					 Attributes attributes) {
		    if (!doc.insideElement()) return;
		    super.startElement(uri, localName, qName, attributes);
		}

		@Override
		public void endElement(String uri, String localName, String qName) {
		    if (!doc.insideElement()) return;
		    super.endElement(uri, localName, qName);
		    propMain.addValue(StringUtil.normalizeWhitespace(consumeCharacterContent().trim()));
		    propPunct.addValue(" ");
		}
	    });

	// structural attributes
	addHandler("div", new InlineTagHandler());
	addHandler("head", new InlineTagHandler());
	addHandler("hi", new InlineTagHandler());
	addHandler("l", new InlineTagHandler());
	addHandler("lg", new InlineTagHandler());
	addHandler("note", new InlineTagHandler());
	addHandler("opener", new InlineTagHandler());
	addHandler("postscript", new InlineTagHandler());
	addHandler("q", new InlineTagHandler());
	addHandler("sp", new InlineTagHandler());
	addHandler("opener", new InlineTagHandler());
    }
}
