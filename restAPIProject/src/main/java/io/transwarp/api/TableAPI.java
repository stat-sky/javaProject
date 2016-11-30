package io.transwarp.api;

import io.transwarp.bean.TableBean;
import io.transwarp.util.CommonString;
import io.transwarp.util.ConnectionTool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class TableAPI {

	private static Logger logger = Logger.getLogger(TableAPI.class);
	private Connection connection = null;
	
	public TableAPI() {
		super();
	}
	/*public TableAPI(String securityType, String inceptorServerIp) {
		this(securityType, inceptorServerIp, CommonString.prop_env.getProperty("jdbcUser"), CommonString.prop_env.getProperty("jdbcPwd"));
	}
	public TableAPI(String securityType, String inceptorServerIp, String jdbcUser, String jdbcPwd) {
		this.securityType = securityType;
		this.inceptorServerIp = inceptorServerIp;
		this.jdbcUser = jdbcUser;
		this.jdbcPwd = jdbcPwd;
	}*/
	
	public List<TableBean> getTableInfos() {
		List<TableBean> tables = new ArrayList<TableBean>();
		connection = ConnectionTool.getConnection();
		try {
			Statement stat = connection.createStatement();
			String sql = CommonString.SQL_SELECT;
			ResultSet rs = stat.executeQuery(sql);
			while(rs.next()) {
				TableBean table = new TableBean();
				table.setDatabase_name(rs.getString("database_name"));
				table.setTable_name(rs.getString("table_name"));
				table.setTable_type(rs.getString("table_type"));
				table.setTable_format(rs.getString("table_format"));
				table.setTable_location(rs.getString("table_location"));
				table.setTransactional(rs.getString("transactional"));
				table.setOwner_name(rs.getString("owner_name"));
				tables.add(table);
			}
		} catch (SQLException e) {
			logger.error("execute sql error : " + e.getMessage());
		}
		return tables;
	}

}
