package io.transwarp.util;

import org.junit.Test;

public class PrintToTableTest {

	@Test
	public void test() {
		String[][] maps = {
				{"Rack",null,"rack1",null,null,null,null},
				{"Node",null,"node01","node02","node03","node04","node5"},
				{"TranswarpManager","Manager","H","","","",""},
				{"TranswarpZookeeper", "Zookeeper","H","H","", "",""},
				{"TranswarpHDFS", "NameNode","H","","","",""},
				{null,"JournalNode","H","","H","",""},
				{null,"DataNode","","H","H","H","H"}
		};
		
		try {
			System.out.println(PrintToTableUtil.printToTable(maps, 20));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
