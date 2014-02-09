/**
 * 
 */
package com.blacklighting.tianfuyunv2.dompraser;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.blacklighting.tianfuyunv2.models.Passage;

/**
 * @author Liu Yajun@blacklighting UESTC
 * 
 */
public class PassageParser {

	public static Passage paerser(InputStream in)
			throws ParserConfigurationException, SAXException, IOException {
		Passage passage = new Passage();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document dom = builder.parse(in);
		Element root = dom.getDocumentElement();
		passage.setTitle(root.getAttribute("title"));
		NodeList cNodes = root.getChildNodes();
		for (int j = 0; j < cNodes.getLength(); j++) {
			Node node = cNodes.item(j);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element cNode = (Element) node;
				if (cNode.getNodeName().equals("id")) {
					passage.setId(cNode.getFirstChild().getNodeValue());
				} else if (cNode.getNodeName().equals("source")) {
					passage.setSource(cNode.getFirstChild().getNodeValue());
				} else if (cNode.getNodeName().equals("content")) {
					passage.setContent(cNode.getFirstChild().getNodeValue());
				} else if (cNode.getNodeName().equals("image")) {
					passage.setImage(cNode.getFirstChild().getNodeValue());
				} else if (cNode.getNodeName().equals("createTime")) {
					passage.setCreateTime(cNode.getFirstChild().getNodeValue());
				}
			}
		}

		return passage;
	}

}
