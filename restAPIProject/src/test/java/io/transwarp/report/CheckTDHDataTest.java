package io.transwarp.report;

import io.transwarp.util.SessionTool;

import org.junit.Test;

public class CheckTDHDataTest {

	private String ipAddress = "172.16.2.93";
	private String securityType = "simple";
	private String nodeUser = "root";
	private String nodePwd = "123456";
/*	@Test
	public void test_getCommand() throws Exception{
		CheckTDHData check = new CheckTDHData(securityType, ipAddress);
		System.out.println(check.getHDFSReport());
	}*/
	
	@Test
	public void test_getTable() {
		CheckTDHData check = new CheckTDHData(securityType, ipAddress, nodeUser, nodePwd);
		String result = check.checkTables();
		System.out.println(result);
	}
}
