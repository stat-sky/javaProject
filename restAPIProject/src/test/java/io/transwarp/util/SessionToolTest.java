package io.transwarp.util;

import org.junit.Test;

public class SessionToolTest {

	@Test
	public void test() {
		String cmd = "ls /home/xhy/temp ";
		try {
			String result = SessionTool.executeLocal(cmd);
			String[] lines = result.split("\n");
			for(String line : lines) {
				if(line.matches("hadoop-hdfs-namenode-*\\S+")) {
					System.out.println(line);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
