package io.transwarp.report;

import io.transwarp.bean.NodeBean;

import org.junit.Test;

public class NodeBaseReportTest {

	private String ipAddress = "172.16.1.109";
	private String nodeUser = "root";
	private String nodePwd = "654321";
	
/*	@Test
	public void portCheckTest() {
		NodeBean node = new NodeBean();
		node.setIpAddress(ipAddress);
		NodeBaseReport report = new NodeBaseReport(node, nodeUser, nodePwd);
		System.out.println(report.getPortCheck());
	}*/
	
	@Test 
	public void test() {
		NodeBean node = new NodeBean();
		node.setIpAddress(ipAddress);
		NodeBaseReport report = new NodeBaseReport(node, nodeUser, nodePwd);
		System.out.println(report.getNodeCheckReport());
	}
}
