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

    private boolean inCorpusFileHeader;

    public DocIndexerMBG(Indexer indexer, String fileName, Reader reader) {
	super(indexer, fileName, reader);

	// Document Handler:
	// (The default DocumentElementHandler will make sure BlackLab knows where our
	// documents start and end and get added to the index in the correct way.)
	addHandler("mbg", new DocumentElementHandler());

	addHandler("corpusFile", new ElementHandler() {
		@Override
		public void startElement(String uri, String localName, String qName,
					 Attributes attributes) {
		    inCorpusFileHeader = true;
		}
		@Override
		public void endElement(String uri, String localName, String qName) {
		    inCorpusFileHeader = false;
		}
	    });

	// Header metadata
	addHandler("author", new MetadataElementHandler() {
		@Override
		public void startElement(String uri, String localName, String qName,
					 Attributes attributes) {
		    if (!inCorpusFileHeader) return;
		    super.startElement(uri, localName, qName, attributes);
		    // System.out.println("author:" + attributes.getValue("id"));
		    addMetadataField("authorId", attributes.getValue("id"));
		}
		@Override
		public void endElement(String uri, String localName, String qName) {
		    if (!inCorpusFileHeader) return;
		    super.endElement(uri, localName, qName);
		    // System.out.println("author:" + getElementContent());
		    addMetadataField("author", getElementContent());
		}
	    });
	
	addHandler("date", new MetadataElementHandler() {
		@Override
		public void startElement(String uri, String localName, String qName,
					 Attributes attributes) {
		    if(!inCorpusFileHeader) return; // make sure we only index date from header
		    super.startElement(uri, localName, qName, attributes);
		}

		@Override
		public void endElement(String uri, String localName, String qName) {
		    if(!inCorpusFileHeader) return;
		    super.endElement(uri, localName, qName);
		}
	    });
	addHandler("translation", new MetadataElementHandler());
	addHandler("genre", new MetadataElementHandler());
	addHandler("domain", new MetadataElementHandler());

	// Corpus
	// main properties
	final ComplexFieldProperty propMain = getMainProperty();
	final ComplexFieldProperty propPunct = getPropPunct();
	// extra properties
	final ComplexFieldProperty propId = addProperty("id");

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
		    propId.addValue(attributes.getValue("id"));
		}

		@Override
		public void endElement(String uri, String localName, String qName) {
		    if (!doc.insideElement()) return;
		    super.endElement(uri, localName, qName);
		    propMain.addValue(StringUtil.normalizeWhitespace(consumeCharacterContent().trim()));
		    propPunct.addValue(" ");
		}
	    });

	addHandler("div", new InlineTagHandler() {

		private String textType;
		@Override
		public void startElement(String uri, String localName, String qName,
					 Attributes attributes) {
		    String textType = attributes.getValue("type");
		    // System.out.println("textType:" + textType);
		    if (textType==null) return;
		    // hack: treat type attribute as if it were an xml tag
		    // see InlineTagHandler implementation for why this works
		    else super.startElement(uri, textType, qName, attributes);
		    // textType = attributes.getValue("type");
		}
		@Override
		public void endElement(String uri, String localName, String qName) {
		    if (textType == null) return;
		    else super.endElement(uri, localName, qName);
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
