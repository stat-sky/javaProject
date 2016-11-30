package io.transwarp.util;

import org.junit.Test;

public class HttpMethodTest {

	@Test
	public void test() {
		HttpMethod method = HttpMethod.getMethod();
		method.downloadKeytab("hive");
	}
}
