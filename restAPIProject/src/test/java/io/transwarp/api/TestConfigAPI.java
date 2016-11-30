package io.transwarp.api;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;


public class TestConfigAPI {

/*	@Test
	public void test() {
		ConfigAPI config = new ConfigAPI("yarn");
		config.writeToFile();
	}*/
	
/*	@Test
	public void test_getShell() {
		try {
			ConfigAPI config = new ConfigAPI("HYPERBASE");
			Map<String, List<String>> configShell = config.getConfigByShell();
			for(Iterator<String> keys = configShell.keySet().iterator(); keys.hasNext(); ) {
				String key = keys.next();
				List<String> value = configShell.get(key);
				System.out.println(key);
				for(String line : value) {
					System.out.println("\t" + line);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
}
