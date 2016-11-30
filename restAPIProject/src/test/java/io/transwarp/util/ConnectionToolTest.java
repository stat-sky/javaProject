package io.transwarp.util;

import io.transwarp.bean.TableBean;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Test;

public class ConnectionToolTest {

	@Test
	public void test() {
		Connection connection = ConnectionTool.getConnection();
		try {
			Statement stat = connection.createStatement();
			String sql = "select database_name, table_name, table_type, transactional, table_format, table_location, owner_name from system.tables_v";
			ResultSet rs = stat.executeQuery(sql);
			while(rs.next()) {
				TableBean table = new TableBean();
				table.setDatabase_name(rs.getString("database_name"));
				table.setTable_name(rs.getString("table_name"));
				table.setTable_type(rs.getString("table_type"));
				table.setTransactional(rs.getString("transactional"));
				table.setTable_format(rs.getString("table_format"));
				table.setOwner_name(rs.getString("owner_name"));
				table.setTable_location(rs.getString("table_location"));
				System.out.println(table.getTable_name() + " : " + table.checkTableType());
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
}
