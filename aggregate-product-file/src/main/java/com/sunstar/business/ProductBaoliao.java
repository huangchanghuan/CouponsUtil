package com.sunstar.business;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.sunstar.jdbc.JdbcConnection;

public class ProductBaoliao {
	private static final Logger logger = Logger.getLogger(ProductBaoliao.class);
	
	private static Connection connection = JdbcConnection.getConn();
	private static PreparedStatement ps = null;
	
	private static String insertProductBaoliaoSql = "INSERT INTO ss_hj_product_baoliao(product_name, product_price, product_price_currency, product_picture, product_url, baoliao_title, baoliao_content, baoliao_news) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
	
	private static Document doc = null;
	private static Elements mainElements = null;
	private static Element mainElement = null;
	
	static {
		try {
			ps = connection.prepareStatement(insertProductBaoliaoSql);
		} catch (SQLException e) {
			logger.error("获取ps失败", e);
		}
	}
	
	/**
	 * 解析目录下的文件
	 * @param path
	 */
	public static void processFiles(String path) {
		File root = new File(path);
		File[] subFiles = root.listFiles();
		for (File f : subFiles) {
			if (!f.isDirectory()) {// 是文件
				processFile(f);
			} else {// 是目录
				logger.info("解析目录" + f.getName() + "开始");
				processFiles(f.getAbsolutePath());
				logger.info("解析目录" + f.getName() + "结束");
			}
		}
	}
	
	/**
	 * 解析文件
	 * @param file
	 */
	public static void processFile(File file) {
		String fileName = file.getName();
		if (!"htm".equals(fileName.substring(fileName.lastIndexOf(".") + 1)))
			return;
		try {
			doc = Jsoup.parse(file, "utf-8", "");
			mainElements = doc.getElementsByTag("article");
			if (mainElements == null || mainElements.size() == 0)
				return;
			
			mainElement = mainElements.get(0);
			
			try {
				Elements articleTitleElements = mainElement.getElementsByClass("article_title");
				if (articleTitleElements == null || articleTitleElements.size() == 0)
					return;
				
				Elements emElements = articleTitleElements.get(0).getElementsByTag("em");
				for (Element em : emElements) {
					if ("name".equals(em.attr("itemprop")))
						ps.setString(1, em.text());
					else if ("price".equals(em.attr("itemprop"))) {
						try {
							ps.setBigDecimal(2, new BigDecimal(em.text()));
						} catch (Exception e) {
							ps.setNull(2, java.sql.Types.DECIMAL);
						}
					}
				}
				
				Elements curElements = articleTitleElements.get(0).getElementsByTag("meta");
				if (curElements != null && curElements.size() > 0)
					ps.setString(3, curElements.get(0).attr("content"));
				else
					ps.setNull(3, java.sql.Types.VARCHAR);
				
				try {
					String picUrl = mainElement.getElementsByClass("pic-Box").get(0).getElementsByTag("img").get(0).attr("src");
					ps.setString(4, picUrl.substring(picUrl.lastIndexOf("../") + 3));
				} catch (Exception e) {
					ps.setNull(4, java.sql.Types.VARCHAR);
				}
				
				try {
					String buyUrl = mainElement.getElementsByClass("buy").get(0).getElementsByTag("a").get(0).attr("href");
					ps.setString(5, buyUrl.substring(buyUrl.lastIndexOf("../") + 3));
				} catch (Exception e) {
					ps.setNull(5, java.sql.Types.VARCHAR);
				}
				
				Element baoliaoElement = mainElement.getElementsByClass("item-box item-preferential").get(0);
				
				try {
					ps.setString(6, baoliaoElement.getElementsByTag("strong").get(0).text());
				} catch (Exception e) {
					ps.setNull(6, java.sql.Types.VARCHAR);
				}
				
				try {
					ps.setString(7, baoliaoElement.getElementsByClass("baoliao-block").get(0).getElementsByTag("p").text());
				} catch (Exception e) {
					ps.setNull(7, java.sql.Types.VARCHAR);
				}
				
				try {
					Elements baoliaoNewsElements = baoliaoElement.getElementsByClass("baoliao-block news_content");
					ps.setString(8, baoliaoNewsElements.get(0).getElementsByTag("p").get(0).text());
				} catch (Exception e) {
					ps.setNull(8, java.sql.Types.VARCHAR);
				}
				
				// 录入商品爆料
				ps.execute();
			} catch (SQLException e) {
				logger.error("---录入商品爆料" + fileName + "失败---", e);
			}
		} catch (IOException e) {
			logger.error("---解析文件" + fileName + "失败---", e);
		}
	}

	public static void main(String[] args) {
		processFiles("C:\\Users\\adms\\Desktop\\smzdm");
		logger.info("----关闭资源----");
		if (ps != null) {
			try {
				ps.close();
			} catch (SQLException e) {
				logger.error("---关闭ps失败---", e);
			}
		}
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				logger.error("---关闭connection失败---", e);
			}
		}
		logger.info("------end------");
	}

}
