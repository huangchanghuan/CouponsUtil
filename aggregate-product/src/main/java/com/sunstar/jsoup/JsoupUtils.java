package com.sunstar.jsoup;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class JsoupUtils {
	private static final Logger logger = Logger.getLogger(JsoupUtils.class);
	
	public static Document getDocument(String url) {
		Connection.Response response = null;
		for (int i = 0; i < 3; i++) {
			try {
				response = Jsoup.connect(url).timeout(60000).execute();
				if (response.statusCode() != 200) {
					logger.warn(url + "请求失败" + (i + 1));
				} else {
					return response.parse();
				}
			} catch (IOException e) {
				logger.error(url + "请求失败", e);
			}
		}
		
		return null;
	}
}
