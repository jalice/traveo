/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xml;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.*;
import java.util.*;
/**
 *
 * @author user
 */
public class Parse extends DefaultHandler{
	
	public LinkedHashMap root = new LinkedHashMap();
	protected LinkedHashMap current ;

	public LinkedHashMap attrToHashMap(ArrayList attr) {
		LinkedHashMap buf = new LinkedHashMap();
		LinkedHashMap elem;

		for (int i=0; i<attr.size(); i++) {
			elem = (LinkedHashMap)attr.get(i);
			buf.put(elem.get("name"), elem.get("value"));
		}

		return buf;
	}

	@Override
	public void startDocument() {
		root = new LinkedHashMap();
		root.put("attr", new ArrayList());
		root.put("name", "root");
		root.put("value", "");

		current = root;
	}

	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {

		LinkedHashMap node = new LinkedHashMap();
		node.put("parent", current);
		node.put("attr", new ArrayList());
		node.put("name", qName);
		node.put("value", "");

		((ArrayList)(current.get("attr"))).add(node);
		current = node;
	}

	@Override
	public void characters(char[] ch, int start, int length){
		current.put("value", current.get("value")+String.copyValueOf(ch, start, length));
	}

	@Override
	public void endElement(String namespaceURI, String localName, String qName) {
		current = (LinkedHashMap)(current.get("parent"));
	}

	@Override
	public void endDocument() {
		//System.out.println("Stop parse XML...");
	}

}
