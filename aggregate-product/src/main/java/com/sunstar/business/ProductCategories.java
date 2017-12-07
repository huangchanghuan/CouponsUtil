package com.sunstar.business;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.sunstar.jsoup.JsoupUtils;

public class ProductCategories {
	private static final Logger logger = Logger.getLogger(ProductCategories.class);

	public static void main(String[] args) {
		logger.info("3333");
		String url = "http://home.manmanbuy.com/bijia.aspx";
		Document categories = JsoupUtils.getDocument(url);
		if (categories == null)
			return;
		
		// 获取一级分类
		Elements category_1st = categories.getElementsByTag("h2");
		if (category_1st == null || category_1st.size() == 0)
			return;
		
		int category_1st_id = 1001;
		int category_2nd_id = 10001;
		int category_3rd_id = 100001;
		for (Element c1 : category_1st) {
			logger.info("INSERT INTO ss_hj_category(cls_id, parent_id, cls_name, cls_level, priority) VALUES (" + category_1st_id + ", 0, '" + c1.text() + "', '0', " + category_1st_id + ");");
			Element c2 = c1.nextElementSibling();
			if (c2 == null)
				continue;
			
			while (c2 != null) {
				// 获取二级分类
				if ("sclassBlock".equals(c2.className())) {
					Elements c2_name = c2.getElementsByClass("sclassLeft");
					logger.info("INSERT INTO ss_hj_category(cls_id, parent_id, cls_name, cls_level, priority) VALUES (" + category_2nd_id + ", " + category_1st_id + ", '" + c2_name.get(0).text() + "', '1', " + category_2nd_id + ");");
					// 获取三级分类
					Elements c3_div = c2.getElementsByClass("sclassRight");
					Elements c3_a = c3_div.get(0).getElementsByTag("a");
					for (Element c3 : c3_a) {
						logger.info("INSERT INTO ss_hj_category(cls_id, parent_id, cls_name, url, cls_level, priority) VALUES (" + category_3rd_id + ", " + category_2nd_id + ", '" + c3.text() + "', '" + c3.attr("href") + "', '2', " + category_3rd_id + ");");
						category_3rd_id++;
					}
					
					category_2nd_id++;
					c2 = c2.nextElementSibling();
				} else if ("h2".equals(c2.tagName())) {
					c2 = null;
				} else {
					c2 = c2.nextElementSibling();
				}
			}
			category_1st_id++;
		}
	}

}
