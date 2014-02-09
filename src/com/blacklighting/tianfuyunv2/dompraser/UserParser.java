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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.blacklighting.tianfuyunv2.models.User;

/**
 * @author blacklighting
 * 
 */
public class UserParser {

	public static User parse(InputStream in)
			throws ParserConfigurationException, SAXException, IOException {
		User user = new User();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document dom = builder.parse(in);
		Element root = dom.getDocumentElement();
		NodeList cNodes = root.getChildNodes();

		for (int i = 0; i < cNodes.getLength(); i++) {
			Element cNode = (Element) cNodes.item(i);
			if (cNode.getNodeName().equals("name")) {
				user.setName(cNode.getFirstChild().getNodeValue());
			} else if (cNode.getNodeName().equals("company")) {
				user.setCompanyId(cNode.getAttribute("id"));
				user.setCompanyName(cNode.getFirstChild().getNodeValue());
			} else if (cNode.getNodeName().equals("department")) {
				user.setDepartment(cNode.getFirstChild().getNodeValue());
			} else if (cNode.getNodeName().equals("identify")) {
				user.setIdentify(cNode.getFirstChild().getNodeValue());
			} else if (cNode.getNodeName().equals("phone")) {
				user.setPhone(cNode.getFirstChild().getNodeValue());
			} else if (cNode.getNodeName().equals("token")) {
				user.setToken(cNode.getFirstChild().getNodeValue());
			}
		}
		return user;

	}

}
