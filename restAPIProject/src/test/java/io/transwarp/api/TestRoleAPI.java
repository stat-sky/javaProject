package io.transwarp.api;

import org.junit.Test;

public class TestRoleAPI {

	@Test
	public void test_outputToExecl() {
		String path = "E:/temp/roleMap.xls";
		RoleAPI roleAPI = new RoleAPI();
		roleAPI.writeRoleMapToExecl(path);
//		System.out.println("ok");
	}
}
