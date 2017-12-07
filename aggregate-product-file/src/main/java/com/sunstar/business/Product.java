package com.sunstar.business;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.sunstar.jdbc.JdbcConnection;

public class Product {
	private static final Logger logger = Logger.getLogger(Product.class);
	
	private static Connection connection = JdbcConnection.getConn();
	private static PreparedStatement ps = null;
	
	private static String selectCategorySql = "select cls_id from ss_hj_category where cls_name = ?";
	private static String selectValueSql = "select value_id from ss_hj_value where quantity_id = 1 and value_name = ?";
	
	private static String insertProductSql = "INSERT INTO ss_hj_product(product_id, cls_id, product_name, priority, brand_id) VALUES (?, ?, ?, ?, ?);";
	private static String insertProductPictureSql = "INSERT INTO ss_hj_product_picture(product_id, product_url, product_view_url) VALUES (?, ?, ?);";
	private static String insertProductRelationSql = "INSERT INTO ss_hj_product_relation(product_id, autoid) VALUES (?, ?);";
	
	private static Document doc = null;
	
	private static Integer productId = null;
	
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
	}
	
	/**
	 * 解析文件
	 * @param file
	 */
	@SuppressWarnings("resource")
	public static void processFile(File file) {
		String fileName = file.getName();
		if (!"aspx".equals(fileName.substring(fileName.lastIndexOf(".") + 1)))
			return;
		try {
			doc = Jsoup.parse(file, "gb2312", "");
			Elements mainElements = doc.getElementsByClass("pro-detail-box");
			if (mainElements == null || mainElements.size() == 0)
				return;
			
			Integer clsId = null;
			Integer valueId = null;
			
			ResultSet rs = null;
			try {
				Element categoryElement = doc.getElementsByClass("breadcrumbs").get(0);
				Elements cvElements = categoryElement.getElementsByTag("a");
				String category = cvElements.get(1).text();
				ps = connection.prepareStatement(selectCategorySql);
				ps.setString(1, category);
				rs = ps.executeQuery();
				while (rs.next()) {
					clsId = rs.getInt("cls_id");
				}
				
				String brand = cvElements.get(2).text().replace(category, "");
				ps = connection.prepareStatement(selectValueSql);
				ps.setString(1, brand);
				rs = ps.executeQuery();
				while (rs.next()) {
					valueId = rs.getInt("value_id");
				}
				
				Element mainElement = mainElements.get(0);
				String productName = mainElement.getElementsByTag("h1").text();
				
				// 录入聚合商品
				productId = Integer.parseInt(fileName.substring(fileName.lastIndexOf("_") + 1, fileName.lastIndexOf(".")).replace("5F", ""));
				ps = connection.prepareStatement(insertProductSql);
				ps.setInt(1, productId);
				if (clsId == null) {
					ps.setNull(2, java.sql.Types.INTEGER);
				} else {
					ps.setInt(2, clsId);
				}
				ps.setString(3, productName);
				ps.setInt(4, productId);
				if (valueId == null) {
					ps.setNull(5, java.sql.Types.INTEGER);
				} else {
					ps.setInt(5, valueId);
				}
				ps.execute();
				
				// 录入聚合商品图片
				Elements pictureElements = doc.getElementsByClass("pro-detail-img").get(0).getElementsByClass("items").get(0).getElementsByTag("img");
				for (Element picture : pictureElements) {
					ps = connection.prepareStatement(insertProductPictureSql);
					ps.setInt(1, productId);
					ps.setString(2, picture.attr("src").replace("../", "http://"));
					ps.setString(3, picture.attr("bimg"));
					ps.execute();
				}
				
				// 录入聚合商品与平台商品关联关系
				Elements siteProductElements = doc.getElementsByClass("singlebj");
				if (siteProductElements != null && siteProductElements.size() > 0) {
					for (Element siteProduct : siteProductElements) {
						int id = Integer.parseInt(siteProduct.id().replace("bj_", ""));
						ps = connection.prepareStatement(insertProductRelationSql);
						ps.setInt(1, productId);
						ps.setInt(2, id);
						ps.execute();
					}
				}
			} catch (Exception e) {
				logger.error("解析文件" + fileName + "失败", e);
			} finally {
				if (rs != null) {
					try {
						rs.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				if (ps != null) {
					try {
						ps.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			logger.error("---解析文件" + fileName + "失败---", e);
		}
	}

	public static void main(String[] args) {
		processFiles("C:\\Users\\adms\\Desktop\\product\\www.manmanbuy.com");
	}

}
