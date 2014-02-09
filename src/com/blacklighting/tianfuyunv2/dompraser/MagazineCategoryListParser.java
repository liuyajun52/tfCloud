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
import com.blacklighting.tianfuyunv2.models.MagazineCategory;
import com.blacklighting.tianfuyunv2.models.Passage;

public class MagazineCategoryListParser {
	public static List<MagazineCategory> parser(InputStream in) {
		List<MagazineCategory> categoriers = new ArrayList<MagazineCategory>();
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document dom = builder.parse(in);
			Element root = dom.getDocumentElement();
			NodeList ctoegoryNodes = root.getElementsByTagName("category");

			for (int i = 0; i < ctoegoryNodes.getLength(); i++) {
				MagazineCategory category = new MagazineCategory();
				Element categoryNode = (Element) ctoegoryNodes.item(i);
				category.setName(categoryNode.getAttribute("name"));

				NodeList passageNodes = categoryNode
						.getElementsByTagName("passage");
				List<Passage> passages = new ArrayList<Passage>();
				for (int j = 0; j < passageNodes.getLength(); j++) {
					Passage passage = new Passage();

					Element passageNode = (Element) passageNodes.item(j);
					passage.setId(passageNode.getAttribute("id"));
					NodeList passageDetailNodes = passageNode.getChildNodes();

					for (int k = 0; k < passageDetailNodes.getLength(); k++) {
						Node passageDetailNode = passageDetailNodes.item(k);

						if (passageDetailNode.getNodeType() == Node.ELEMENT_NODE) {
							Element passageDetailElement = (Element) passageDetailNode;
							if (passageDetailElement.getNodeName().equals(
									"title")) {
								passage.setTitle(passageDetailElement
										.getFirstChild().getNodeValue());
							} else if (passageDetailElement.getNodeName()
									.equals("createTime")) {
								passage.setCreateTime(passageDetailElement
										.getFirstChild().getNodeValue());
							} else if (passageDetailElement.getNodeName()
									.equals("image")) {
								passage.setImage(passageDetailElement
										.getFirstChild().getNodeValue());
							}
						}
					}
					if (passage.getImage() != null&&SettingActivity.loadImage) {
						Bitmap bitmap = null;
						ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
						InputStream inputStream = null;

						HttpClient httpClient = new DefaultHttpClient();
						HttpPost httpPost = new HttpPost(
								ApiAddress.getBaseURL() + passage.getImage());
						HttpResponse httpResponse = httpClient
								.execute(httpPost);
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
							passage.setRealImag(bitmap);
						}
//						inputStream.close();
//						passage.setRealImag(bitmap);
					}
					passages.add(passage);

				}
				category.setPassages(passages);
				if (category.getPassages().size() != 0) {
					categoriers.add(category);
				}
			}

		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return categoriers;
	}

}
