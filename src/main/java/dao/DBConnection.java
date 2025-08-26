package dao;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
	
	
	public static Connection connection = null;
	
	public static Connection getConnection() {
		if(connection==null) {
			try {
				connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/jobportal","root","bedsheet");
				
				
			}catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		return connection;
	}
	
	public static void closeConnection() {
		if(connection!=null) {
			try {
				connection.close();
				connection = null;
			}catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}

}
