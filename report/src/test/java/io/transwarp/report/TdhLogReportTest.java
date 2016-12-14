package io.transwarp.report;

import org.junit.Test;

import io.transwarp.bean.NodeBean;

public class TdhLogReportTest {

	private NodeBean node;
	private String nodeUser = "root";
	private String nodePwd = "123456";
	
	@Test
	public void test() {
		node = new NodeBean();
		node.setIpAddress("172.16.1.33");
		node.addRole("INCEPTOR_SERVER");
		
		TdhLogReport report = new TdhLogReport(node, nodeUser, nodePwd);
		report.getLogReport();
	}
}
