package io.transwarp.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.transwarp.util.Constant;
import io.transwarp.util.PrintToTableUtil;
import io.transwarp.util.SessionTool;
import io.transwarp.util.UtilTool;

import org.apache.log4j.Logger;
import org.dom4j.Element;

public class ProcessReport {

	private static Logger logger = Logger.getLogger(ProcessReport.class);
	private SessionTool session;
	
	public ProcessReport(SessionTool session) {
		this.session = session;
	}
	
	public String getProcessReport() {
		StringBuffer answer = new StringBuffer();
		List<Element> configs = Constant.prop_processCheck.getAll();
		for(Element config : configs) {
			//列出标题
			String name = config.elementText("name");
			//执行shell语句获取信息
			String command = config.elementText("command");
			logger.info("executor command is : " + command);
			try {
				String exec_result = this.session.executeDis(command);
				String delimited = config.elementText("delimited");
				if(delimited != null) {
					answer.append(this.analysisResultByDelimited(exec_result, config, delimited)).append("\n\n");
				}else {
					answer.append(this.analysisResultNotDelimited(exec_result, config)).append("\n\n");
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return answer.toString();
	}
	
	private String analysisResultByDelimited(String exec_result, Element config, String delimited) throws Exception{
		StringBuffer buffer = new StringBuffer(config.elementText("name")).append("\n");
		String[] lines = exec_result.trim().split("\n");
		String property = config.elementText("property");
		//存储数据，用来生成表格
		List<String[]> maps = new ArrayList<String[]>();
		if(property != null) {
			maps.add(new String[]{"key", "value"});  //表格标题
			String[] props = property.split(";");
			for(String line : lines) {
				//对该行进行切分
				String[] items = line.trim().split(delimited);
				int itemsNum = items.length;
				for(String prop : props) {
					//获取括号的标记，以此判断是否有需要过滤的子项
					int tempId = prop.indexOf("(");
					if(tempId != -1) {  //存在过滤子项，则对配置项进行解析
						String key = prop.substring(0, tempId);
						if(items[0].indexOf(key) == -1) continue;  //每行第一项作为关键字与配置中的提取项进行比对
						StringBuffer itemGet = new StringBuffer(); //存放过滤结果
						String[] values = prop.substring(tempId + 1, prop.length() - 1).split(",");
						for(int i = 1; i < itemsNum; i++) {
							for(String value : values) {
								if(items[i].indexOf(value) != -1) {
									itemGet.append(items[i]).append(" ");
								}
							}
						}
						maps.add(new String[]{key, itemGet.toString()});
					}else {
						if(items[0].indexOf(prop) == -1) continue;
						StringBuffer itemGet = new StringBuffer();
						for(int i = 0; i < itemsNum; i++) {
							if(!items[i].equals("=")) {
								itemGet.append(items[i]).append(" ");
							}
						}
						maps.add(new String[]{prop, itemGet.toString()});
					}
				}
			}
			buffer.append(PrintToTableUtil.printToTable(maps, 50));
		}else {
			if(delimited.equals("1")) delimited = "\\s+";
			else if(delimited.equals("2")) delimited = " \\s+";
			int columnCount = 0;
			for(String line : lines) {
				String[] items = line.trim().replaceAll(delimited, "\n").split("\n");
				int len = items.length;
				if(len > 1) {
					columnCount = len; 
					maps.add(items);
				}else if(len == 1){
					if(columnCount != 0) {
						List<String> buf = new ArrayList<String>();
						buf.add(items[0]);
						for(int i = 1; i < columnCount; i++) {
							buf.add(null);
						}
						maps.add(UtilTool.changeListToStrings(buf));
					}
				}
			}
			int centLength = 200 / maps.get(0).length;
			buffer.append(PrintToTableUtil.printToTable(maps, centLength));
		}
		return buffer.toString();
	}
	
	private String analysisResultNotDelimited(String exec_result, Element config) throws Exception{
		String property = config.elementText("property");
		String name = config.elementText("name");
		String[] lines = exec_result.split("\n");
		List<String[]> maps = new ArrayList<String[]>();
		maps.add(new String[]{"key", "value"});
		if(property != null) {
			for(String line : lines) {
				if(line.indexOf(property) != -1) {
					maps.add(new String[]{name, line});
				}
			}
		}else {
			for(String line : lines){
				maps.add(new String[]{name, line});
			}
		}
		StringBuffer buffer = new StringBuffer("\n");
		buffer.append(PrintToTableUtil.printToTable(maps, 60));
		return buffer.toString();
	}
}
