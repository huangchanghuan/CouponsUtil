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

import com.sunstar.jdbc.JdbcConnection;

public class ProductSpecific {
	private static final Logger logger = Logger.getLogger(ProductSpecific.class);
	
	private static Connection connection = JdbcConnection.getConn();
	private static PreparedStatement ps = null;
	
	private static String insertProductSpecificSql = "INSERT INTO ss_hj_product_specific(product_id, specification) VALUES (?, ?);";
	
	private static Document doc = null;
	private static Element mainElement = null;
	
	static {
		try {
			ps = connection.prepareStatement(insertProductSpecificSql);
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
		if (!"aspx".equals(fileName.substring(fileName.lastIndexOf(".") + 1)))
			return;
		try {
			doc = Jsoup.parse(file, "gb2312", "");
			mainElement = doc.getElementById("Div2");
			if (mainElement == null)
				return;
			
			try {
				// 录入聚合商品参数
				ps.setInt(1, Integer.parseInt(fileName.substring(fileName.lastIndexOf("_") + 1, fileName.lastIndexOf(".")).replace("5F", "")));
				ps.setBlob(2, new ByteArrayInputStream(mainElement.toString().getBytes()));
				ps.execute();
			} catch (SQLException e) {
				logger.error("---录入商品参数" + fileName + "失败---", e);
			}
		} catch (IOException e) {
			logger.error("---解析文件" + fileName + "失败---", e);
		}
	}

	public static void main(String[] args) {
		processFiles("C:\\Users\\adms\\Desktop\\product-specific\\www.manmanbuy.com");
	}

}
