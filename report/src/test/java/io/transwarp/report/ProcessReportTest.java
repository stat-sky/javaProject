package io.transwarp.report;

import io.transwarp.util.SessionTool;

import org.junit.Test;

public class ProcessReportTest {

	private String ipAddress = "172.16.1.110";
	private String nodeUser = "root";
	private String nodePwd = "654321";
	
	@Test
	public void test() {
		try {
			SessionTool session = SessionTool.getSession(ipAddress, nodeUser, nodePwd);
			ProcessReport report = new ProcessReport(session);
			System.out.println(report.getProcessReport());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
