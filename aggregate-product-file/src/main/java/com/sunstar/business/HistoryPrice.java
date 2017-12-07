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

import com.sunstar.jdbc.JdbcConnection;

public class HistoryPrice {
	private static final Logger logger = Logger.getLogger(HistoryPrice.class);
	
	private static Connection connection = JdbcConnection.getConn();
	private static PreparedStatement ps = null;
	
	private static String insertHistoryPriceSql = "INSERT INTO ss_hj_history_price(product_id, min_price, avg_price, price_date) VALUES (?, ?, ?, ?);";
	
	private static Document doc = null;
	
	static {
		try {
			ps = connection.prepareStatement(insertHistoryPriceSql);
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
			doc = Jsoup.parse(file, "gb2312", "");
			String data = doc.data();
			if (data.indexOf("var usdeur1") == -1)
				return;
			if (data.indexOf("$(function()") == -1)
				return;
			
			String[] priceData = data.substring(data.indexOf("var usdeur1"), data.indexOf("$(function()")).replace("\r\n", "").replace(" ", "").split(";");
			String avgPrice = priceData[0].replace("varusdeur1=", "");
			String minPrice = priceData[1].replace("varusdeur2=", "");
			
			if ("[]".equals(avgPrice) || "[]".equals(minPrice))
				return;
			
			avgPrice = avgPrice.replace("[[", "").replace("]]", "");
			minPrice = minPrice.replace("[[", "").replace("]]", "");
			
			String[] avgPriceArr = avgPrice.split("\\],\\[");
			String[] minPriceArr = minPrice.split("\\],\\[");
			if (avgPriceArr.length != minPriceArr.length)
				return;
			
			try {
				// 录入聚合商品历史价格
				for (int i = 0; i < avgPriceArr.length; i++) {
					ps.setInt(1, Integer.parseInt(fileName.split("&id=")[1]));
					ps.setBigDecimal(2, new BigDecimal(minPriceArr[i].split("\\),")[1]));
					ps.setBigDecimal(3, new BigDecimal(avgPriceArr[i].split("\\),")[1]));
					ps.setString(4, avgPriceArr[i].substring(avgPriceArr[i].lastIndexOf("(") + 1, avgPriceArr[i].lastIndexOf(")")).replace(",", "-"));
					ps.execute();
				}
			} catch (SQLException e) {
				logger.error("---录入商品历史价格" + fileName + "失败---", e);
			}
		} catch (IOException e) {
			logger.error("---解析文件" + fileName + "失败---", e);
		}
	}

	public static void main(String[] args) {
		processFiles("C:\\Users\\adms\\Desktop\\history-price\\tool.manmanbuy.com");
	}

}
