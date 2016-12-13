package io.transwarp.report;

import org.junit.Test;

public class TDHDataReportTest {
	
	private String nodeUser = "root";
	private String nodePwd = "654321";
	private String security = "simple";
	private String ipAddress = "172.16.1.109";
	private String url = "jdbc:hive2://172.16.1.110:10000/default";
	private String jdbcUser = "";
	private String jdbcPwd = "";

	@Test
	public void test() {
		TDHDataReport report = new TDHDataReport(security, ipAddress, nodeUser, nodePwd, url, jdbcUser, jdbcPwd, null);
//		System.out.println(report.getHDFSReport());
		System.out.println(report.getTableInfoReport());
	}
}
