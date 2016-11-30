package io.transwarp.report;


import org.junit.Test;

public class MainReprotTest {

	@Test
	public void test() {
		MainReport report = new MainReport();
		report.getReport("/home/xhy/temp/report.txt");
	}
	
/*	public static void main(String[] args) {
		MainReport report = new MainReport();
		report.getReport("E:/temp/report.txt");		
	}*/
}
