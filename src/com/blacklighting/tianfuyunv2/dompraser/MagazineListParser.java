package com.blacklighting.tianfuyunv2.dompraser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.blacklighting.tianfuyunv2.SettingActivity;
import com.blacklighting.tianfuyunv2.internet.ApiAddress;
import com.blacklighting.tianfuyunv2.models.MagazineListIteam;

public class MagazineListParser {

	public static List<MagazineListIteam> parse(InputStream in)
			throws ParserConfigurationException, SAXException, IOException {
		List<MagazineListIteam> magazineList = new ArrayList<MagazineListIteam>();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document dom = builder.parse(in);
		Element root = dom.getDocumentElement();
		NodeList pNodes = root.getElementsByTagName("magazine");

		for (int i = 0; i < pNodes.getLength(); i++) {
			MagazineListIteam magazine = new MagazineListIteam();
			Element pNode = (Element) pNodes.item(i);

			magazine.setPeriod(pNode.getAttribute("id"));

			NodeList cNodes = pNode.getChildNodes();
			for (int j = 0; j < cNodes.getLength(); j++) {
				Node node = cNodes.item(j);

				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element cNode = (Element) node;
					if (cNode.getNodeName().equals("name")) {
						magazine.setName(cNode.getFirstChild().getNodeValue());
					} else if (cNode.getNodeName().equals("image")) {
						magazine.setImage(cNode.getFirstChild().getNodeValue());
					}
				}
			}
			if (magazine.getImage() != null&&SettingActivity.loadImage) {
				Bitmap bitmap = null;
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				InputStream inputStream = null;

				HttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(ApiAddress.getBaseURL()
						+ magazine.getImage());
				HttpResponse httpResponse = httpClient.execute(httpPost);
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					inputStream = httpResponse.getEntity().getContent();
					int len = 0;
					byte[] data = new byte[1024];// 读取
					while ((len = inputStream.read(data)) != -1) {
						outputStream.write(data, 0, len);// 写入
					}
					byte[] result = outputStream.toByteArray();// 声明字节数组
					bitmap = BitmapFactory.decodeByteArray(result, 0,
							result.length);
					inputStream.close();
					magazine.setRealImag(bitmap);
				}
//				inputStream.close();
//				magazine.setRealImag(bitmap);
			}
			magazineList.add(magazine);
		}

		return magazineList;
	}
}
