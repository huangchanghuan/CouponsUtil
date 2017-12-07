package com.sunstar.business;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.sunstar.jdbc.JdbcConnection;
import com.sunstar.jsoup.JsoupUtils;
import com.sunstar.pojo.HjCategory;

public class ProductQuantities {
	private static final Logger logger = Logger.getLogger(ProductQuantities.class);
	
	private static String url = "jdbc:mysql://localhost:3306/coupons-aggregate-product?useUnicode=true&characterEncoding=UTF-8";
	private static String user = "root";
	private static String password = "123456";
	private static Connection connection = JdbcConnection.getConn(url, user, password);
	private static PreparedStatement ps = null;
	
	private static String checkQuantitySql = "select quantity_id from ss_hj_quantity where quantity_name = ?";
	private static String insertQuantitySql = "INSERT INTO ss_hj_quantity(quantity_id, quantity_name, priority) VALUES (?, ?, ?);";
	private static String checkValueSql = "select value_id from ss_hj_value where quantity_id = ? and value_name = ?";
	private static String insertValueSql = "INSERT INTO ss_hj_value(value_id, value_name, quantity_id, priority) VALUES (?, ?, ?, ?);";
	private static String insertCqvSql = "INSERT INTO ss_hj_cqv_relation(cqv_id, cls_id, quantity_id, value_id, search_index) VALUES (?, ?, ?, ?, ?);";
	
	public static List<HjCategory> getCategories() {
		List<HjCategory> list = new ArrayList<HjCategory>();
		ResultSet rs = null;
		try {
			ps = connection.prepareStatement("select cls_id, url from ss_hj_category where cls_level = 2");
			rs = ps.executeQuery();
			HjCategory hjCategory = null;
			while (rs.next()) {
				hjCategory = new HjCategory();
				hjCategory.setClsId(rs.getInt("cls_id"));
				hjCategory.setUrl(rs.getString("url"));
				list.add(hjCategory);
			}
		} catch (Exception e) {
			logger.error("获取分类url失败", e);
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
		
		return list;
	}
	
	public static void getQuantities(List<HjCategory> list) {
		Document doc = null;
		ResultSet rs = null;
		int quantityIdIncrement = 1;
		int valueIdIncrement = 1;
		int cqvIdIncrement = 1;
		int i = 0;
		for (HjCategory hjCategory : list) {
			int searchIndex = 0;
			doc = JsoupUtils.getDocument(hjCategory.getUrl());
			Elements elements = doc.getElementsByClass("prop-line");
			for (Element element : elements) {
				String quantityName = element.getElementsByClass("a-key").text().replace("：", "");
				try {
					// 录入特征量
					ps = connection.prepareStatement(checkQuantitySql);
					ps.setString(1, quantityName);
					rs = ps.executeQuery();
					Integer quantityId = null;
					while (rs.next()) {
						quantityId = rs.getInt("quantity_id");
					}
					if (quantityId == null) {
						quantityId = quantityIdIncrement;
						ps = connection.prepareStatement(insertQuantitySql);
						ps.setInt(1, quantityId);
						ps.setString(2, quantityName);
						ps.setInt(3, quantityId);
						ps.execute();
						quantityIdIncrement++;
					}
					
					// 录入特征值
					Elements valueElements = element.getElementsByTag("li");
					for (Element valueElement : valueElements) {
						String valueName = valueElement.text();
						if (valueName == null || "".equals(valueName) || "全部".equals(valueName) || "~".equals(valueName))
							continue;
						ps = connection.prepareStatement(checkValueSql);
						ps.setInt(1, quantityId);
						ps.setString(2, valueName);
						rs = ps.executeQuery();
						Integer valueId = null;
						while (rs.next()) {
							valueId = rs.getInt("value_id");
						}
						
						if (valueId == null) {
							valueId = valueIdIncrement;
							ps = connection.prepareStatement(insertValueSql);
							ps.setInt(1, valueId);
							ps.setString(2, valueName);
							ps.setInt(3, quantityId);
							ps.setInt(4, valueId);
							ps.execute();
							valueIdIncrement++;
						}
						// 录入分类，特征量，特征值关系
						ps = connection.prepareStatement(insertCqvSql);
						ps.setInt(1, cqvIdIncrement);
						ps.setInt(2, hjCategory.getClsId());
						ps.setInt(3, quantityId);
						ps.setInt(4, valueId);
						ps.setInt(5, searchIndex);
						ps.execute();
						cqvIdIncrement++;
					}
				} catch (SQLException e) {
					logger.error("录入特征量失败", e);
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
				searchIndex++;
			}
			System.out.println(i);
			i++;
		}
	}

	public static void main(String[] args) {
		List<HjCategory> list = getCategories();
		getQuantities(list);
	}

}
