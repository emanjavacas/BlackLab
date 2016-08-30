package nl.inl.blacklab.indexers;

import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.inl.blacklab.index.DocIndexerXmlHandlers;
import nl.inl.blacklab.index.HookableSaxHandler.ElementHandler;
import nl.inl.blacklab.index.Indexer;
import nl.inl.blacklab.index.complex.ComplexFieldProperty;
import nl.inl.util.StringUtil;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.xml.sax.Attributes;

public class DocIndexerBrownTei extends DocIndexerXmlHandlers {

    private static int wordCount = 0;

    public DocIndexerBrownTei(Indexer indexer, String fileName, Reader reader){
	super(indexer, fileName, reader);

	addHandler("TEI", new DocumentElementHandler());

	// header metadata
	addHandler("title", new MetadataElementHandler());
	addHandler("bibl", new MetadataElementHandler());
	addHandler("idno", new MetadataElementHandler());
	// addHandler("text", new MetadataAttributeValueHandler("decls"));

	// extra properties
	final ComplexFieldProperty propPos = addProperty("pos");
	final ComplexFieldProperty propSubpos = addProperty("subpos");
	final ComplexFieldProperty propId = addProperty("id");
	// default properties
	final ComplexFieldProperty propMain = getMainProperty();
	final ComplexFieldProperty propPunct = getPropPunct();

	// body Handler
		// body Handler
	final ElementHandler body = addHandler("body", new ElementHandler() { // customize for final punct
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
		    consumeCharacterContent();
		}

		@Override
		public void endElement(String uri, String localName, String qName) {
		    // add punct before leaving the body
		    propPunct.addValue(StringUtil.normalizeWhitespace(consumeCharacterContent()));
		    super.endElement(uri, localName, qName);
		}
	    });

	// Word elements: index as main contents
	addHandler("w", new WordHandlerBase(){
		@Override
		public void startElement(String uri, String localName, String qName, 
					 Attributes attributes) {
		    if (!body.insideElement()) return;
		    super.startElement(uri, localName, qName, attributes);

		    String pos = attributes.getValue("type");
		    if (pos == null) pos = "?";
		    propPos.addValue(pos);

		    String subpos = attributes.getValue("subtype");
		    if (subpos == null) subpos = "?";
		    propSubpos.addValue(subpos);

		    propId.addValue(DocIndexerBrownTei.newId());

		    // Add punctuation (in between word elements)
		    propPunct.addValue(StringUtil.normalizeWhitespace(consumeCharacterContent()));
		}
		
		@Override
		public void endElement(String uri, String localName, String qName){
		    if (!body.insideElement()) return;
		    super.endElement(uri, localName, qName);
		    
		    propMain.addValue(consumeCharacterContent());
		}
	    });
	// pc elements
	addHandler("c", new WordHandlerBase(){
		Pattern puncTokenPattern = Pattern.compile("\\p{Punct}+");// , Pattern.UNICODE_CHARACTER_CLASS);
		// check if <c> element is actual punctuation
		private boolean checkPuncToken(String puncToken){
		    Matcher m = puncTokenPattern.matcher(puncToken);
		    if (puncToken == null) return false;
		    if (!m.matches()) {
			System.err.println("Doesn't match: " + puncToken);
			return false;
		    }
		    return true;
		}   
		@Override
		public void startElement(String uri, String localName, String qName, 
					 Attributes attributes){
		    consumeCharacterContent(); // get text before <pc>
		    if(!body.insideElement()) return;
		    super.startElement(uri, localName, qName, attributes);

		    String pos = attributes.getValue("type");
		    propPos.addValue(pos);

		    propId.addValue(DocIndexerBrownTei.newId());
		}
		@Override
		public void endElement(String uri, String localName, String qName){
		    if (!body.insideElement()) return;
		    super.endElement(uri, localName, qName);

		    propMain.addValue(consumeCharacterContent());
		}
	    });

	// Sentence tags: index as tags in the content
	addHandler("s", new InlineTagHandler());

	// Paragraph tags: index as tags in the content
	addHandler("p", new InlineTagHandler());

	
	
    } // end constructor

    private static String newId(String prefix){
	return prefix + Integer.toString(wordCount++);
    }
    
    private static String newId() {
	return newId("");
    }
}
