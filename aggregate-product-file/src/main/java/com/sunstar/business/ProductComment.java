package com.sunstar.business;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.sunstar.jdbc.JdbcConnection;

public class ProductComment {
	private static final Logger logger = Logger.getLogger(ProductComment.class);
	
	private static Connection connection = JdbcConnection.getConn();
	private static PreparedStatement ps = null;
	
	private static String insertProductComment = "INSERT INTO ss_hj_product_comment(product_id, commentator, origin, origin_url, comment, score, comment_time) VALUES (?, ?, ?, ?, ?, ?, ?);";
	
	private static Document doc = null;
	
	static {
		try {
			ps = connection.prepareStatement(insertProductComment);
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
				File[] files = f.listFiles();
				for (File pf : files) {
					processFile(pf);
				}
				logger.info("解析目录" + f.getName() + "结束");
			}
		}
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
	
	/**
	 * 解析文件
	 * @param file
	 */
	public static void processFile(File file) {
		String fileName = file.getName();
		if (fileName.indexOf(".aspx") == -1)
			return;

		try {
			Integer productId = Integer.parseInt(fileName.split("_")[1].replace("5F", ""));
			
			doc = Jsoup.parse(file, "gb2312", "");
			Element listcomment = doc.getElementById("listcomment");
			if (listcomment == null)
				return;
			
			Elements comments = listcomment.getElementsByClass("hp");
			if (comments == null || comments.size() == 0)
				return;
			
			for (Element comment : comments) {
				try {
					// 录入优选评价
					ps.setInt(1, productId);
					
					Element commentatorE = comment.child(1);
					ps.setString(2, commentatorE.getElementsByTag("p").get(0).text().trim());
					ps.setString(3, commentatorE.getElementsByClass("comment_sitename").get(0).text());
					ps.setString(4, commentatorE.getElementsByClass("comment_sitename").get(0).attr("href").replace("../", "http://"));
					
					Element commentE = comment.child(0);
					ps.setBlob(5, new ByteArrayInputStream(commentE.getElementsByClass("comment_nr").get(0).text().getBytes()));
					
					Element scoreE = commentE.getElementsByClass("comment_fsanddate").get(0);
					String scoreImg = scoreE.getElementsByTag("img").get(0).attr("src");
					ps.setString(6, scoreImg.substring(scoreImg.lastIndexOf(".") - 1, scoreImg.lastIndexOf(".")));
					ps.setString(7, scoreE.child(1).text().trim());
					
					ps.execute();
				} catch (Exception e) {
					logger.error("---录入优选评价" + fileName + "失败---");
					continue;
				}
			}
		} catch (IOException e) {
			logger.error("---解析文件" + fileName + "失败---", e);
		}
	}

	public static void main(String[] args) {
		processFiles("C:\\Users\\adms\\Desktop\\mmmpj");
	}

}
