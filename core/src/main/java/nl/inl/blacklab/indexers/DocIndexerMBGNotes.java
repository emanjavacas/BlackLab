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
public class DocIndexerMBGNotes extends DocIndexerXmlHandlers {

    public DocIndexerMBGNotes(Indexer indexer, String fileName, Reader reader) {
	super(indexer, fileName, reader);

	addHandler("mbgNotes", new DocumentElementHandler());
        addHandler("/mbg/header/sourceFile/title", new MetadataElementHandler());
        // header metadata
	addHandler("/mbg/header/corpusFile/author", new MetadataElementHandler() {
		@Override
		public void startElement(String uri, String localName, String qName,
					 Attributes attributes) {
		    super.startElement(uri, localName, qName, attributes);
		    addMetadataField("authorId", attributes.getValue("id"));
		    addMetadataField("generation", attributes.getValue("generation"));
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

	// main properties
	final ComplexFieldProperty propMain = getMainProperty();
	final ComplexFieldProperty propPunct = getPropPunct();

	// Doc handler
	final ElementHandler body = addHandler("body", new ElementHandler() {
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
		    if (!body.insideElement()) return;
		    super.startElement(uri, localName, qName, attributes);
		}

		@Override
		public void endElement(String uri, String localName, String qName) {
		    if (!body.insideElement()) return;
		    super.endElement(uri, localName, qName);
		    propMain.addValue(StringUtil.normalizeWhitespace(consumeCharacterContent().trim()));
		    propPunct.addValue(" ");
		}
	    });
	// structural attributes
	addHandler("note", new InlineTagHandler());
    }
}
