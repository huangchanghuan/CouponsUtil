package com.sunstar.jdbc;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import org.apache.log4j.Logger;

public class JdbcConnection {
	private static final Logger logger = Logger.getLogger(JdbcConnection.class);
	
	private static Properties properties = new Properties();
	static {
		try {
			InputStream in = ClassLoader.getSystemResourceAsStream("jdbc.properties");
			properties.load(in);
			
			Class.forName(properties.getProperty("jdbc.Driver"));
		} catch (Exception e) {
			logger.error("加载JDBC配置文件失败", e);
		}
	}
	
	public static Connection getConn(String url, String user, String password) {
		try {
			return DriverManager.getConnection(url, user, password);
		} catch (Exception e) {
			logger.error("获取JDBC连接失败", e);
			return null;
		}
	}
	
	public static Connection getConn() {
		return getConn(properties.getProperty("jdbc.url"), properties.getProperty("jdbc.user"), properties.getProperty("jdbc.password"));
	}
}
