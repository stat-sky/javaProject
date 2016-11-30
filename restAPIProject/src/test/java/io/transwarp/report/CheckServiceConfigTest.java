package io.transwarp.report;

import java.util.Iterator;
import java.util.Map;

import org.junit.Test;

public class CheckServiceConfigTest {

	@Test
	public void test() {
		CheckServiceConfig check = new CheckServiceConfig();
		Map<String, String> answer = check.getAllNodeConfig();
		for(Iterator<String> keys = answer.keySet().iterator(); keys.hasNext(); ) {
			String key = keys.next();
			String buffer = answer.get(key);
			System.out.println(buffer);
		}
	}
}
