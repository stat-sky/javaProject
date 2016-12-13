package io.transwarp.thread;

import java.util.List;

import io.transwarp.bean.TableBean;
import io.transwarp.util.Constant;

import org.junit.Test;


public class SqlQueryCallableTest {

	private String url = "jdbc:hive2://172.16.1.110:10000/default";
	private String security = "simple";
	
	@Test
	public void test() {
		SqlQueryCallable sqlQuery = new SqlQueryCallable(security, url, Constant.SELECT_TABLES, TableBean.class);
		List<Object> tables = sqlQuery.call();
		for(Object item : tables) {
			TableBean table = (TableBean)item;
			StringBuffer buffer = new StringBuffer();
			buffer.append(table.getDatabase_name());
			buffer.append(table.getTable_name());
			buffer.append(table.getTable_type());
			System.out.println(buffer.toString());
		}
	}
}
