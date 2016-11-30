package io.transwarp.report;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class CheckTDHLogTest {

	private String ipAddress = "172.16.1.110";
	private List<String> roleTypes = new ArrayList<String>();
	
	@Test
	public void test() {
		roleTypes.add("INCEPTOR_SERVER");
		CheckTDHLog checkLog = new CheckTDHLog("172.16.1.110", "root", "654321", roleTypes);
		checkLog.checkLog();
	}
}
