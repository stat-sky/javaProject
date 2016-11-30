package io.transwarp.report;

import org.junit.Test;

public class CheckSystemTest {

	private String ipAddress = "172.16.2.73";
	private String username = "root";
	private String password = "123456";
	
//	@Test
//	public void test() {
//		CheckSystem checkSystem;
//		try {
//			checkSystem = new CheckSystem(this.ipAddress, this.username, this.password);
//			System.out.println(checkSystem.toString());
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	@Test
	public void test_OS() {
		CheckSystem checkSystem;
		try {
			checkSystem = new CheckSystem(this.ipAddress, this.username, this.password);
			System.out.println(checkSystem.getConfigByOS());
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
