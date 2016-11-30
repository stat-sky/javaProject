package io.transwarp.report;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import io.transwarp.util.CommonString;
import io.transwarp.util.CommonUtil;
import io.transwarp.util.SessionTool;

public class CheckSystem {

	private static Logger logger = Logger.getLogger(CheckSystem.class);
	private SessionTool session = null;
	
	public CheckSystem(String ipAddress, String nodeUser, String nodePwd) throws Exception{
		this(SessionTool.getSession(ipAddress, nodeUser, nodePwd));
	}
	public CheckSystem(SessionTool session) {
		this.session = session;
	}
	
	@Override
	public String toString() {
		StringBuffer answer = new StringBuffer("");
		String[] cmdTypes = CommonString.prop_report.getProperty("systems").split(";");
		for(String cmdType : cmdTypes) {
			String result;
			if(cmdType.equals("OS")) {
				result = this.getConfigByOS();
			}else {
				result = this.getConfig(cmdType);
			}
			if(result != null) {
				answer.append(cmdType).append(" :\n");
				answer.append(CommonUtil.paddingBegin(result, "  ")).append("\n");
			}
		}
		return answer.toString();
	}
	
	public String getConfig(String cmdType) {
		StringBuffer buffer = new StringBuffer("");
		String[] params = CommonString.prop_report.getProperty(cmdType).split(";");
		for(String param : params) {
			String cmd = CommonString.prop_report.getProperty(param);
			try {
				String result = this.session.executeDis(cmd);
				if(result != null) {
					buffer.append(param).append(" :\n");
					buffer.append(CommonUtil.paddingBegin(result, "  ")).append("\n");
				}
			} catch (Exception e) {
				logger.error(e.getCause());
			}
		}
		return buffer.toString();
	}
	
	public String getConfigByOS() {
		StringBuffer answer = new StringBuffer();
		String[] parameters = CommonString.prop_report.getProperty("OS").split(";");
		//单元格长度
		int length = 40;
		
		List<String> lines = new ArrayList<String>();
		for(String parameter : parameters) {
			String cmd = CommonString.prop_report.getProperty(parameter);
			try {
				String result = this.session.executeDis(cmd);
				result = result.replace("\n", " ");
				lines.add(parameter + "," + result);
			} catch (Exception e) {
				logger.error("error at get config of os : " + e.getMessage());
			}			
		}
		
		answer.append(CommonUtil.printByTable(new String[]{"parameterName", "value"}, lines, length));
		return answer.toString();
	}
	
	
}
