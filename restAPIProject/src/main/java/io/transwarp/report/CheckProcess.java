package io.transwarp.report;

import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import io.transwarp.api.JmxInfoAPI;
import io.transwarp.util.CommonString;
import io.transwarp.util.CommonUtil;
import io.transwarp.util.SessionTool;

public class CheckProcess {

	private static Logger logger = Logger.getLogger(CheckProcess.class);
	private SessionTool session;
	private List<String> roleTypes;
	
	public CheckProcess(SessionTool session, List<String> roleTypes) throws Exception{
		this.roleTypes = roleTypes;
		this.session = session;
	}
	
/*	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer("");
		try {
			
			buffer.append("端口检测 ：\n");
			buffer.append(this.checkPort()).append("\n");
		}catch(Exception e) {
			logger.error(e.getCause());
		}

		
		return buffer.toString();
	}*/
	
	public String checkProcess() {
		StringBuffer answer = new StringBuffer();
		JmxInfoAPI jmx = new JmxInfoAPI(this.session);
		List<Element> configs = CommonString.config_jvm.getElements("processCheck");
		for(Element config : configs) {
			String roleType = config.elementText("service");
			logger.info("service role Type is : " + roleType);
			if(roleType == null || roleType.equals("") || this.roleTypes.indexOf(roleType) != -1) {
				String cmd_result = jmx.getInfoByShell(config);
				if(cmd_result == null || cmd_result.equals("")) {
					logger.warn("cmd : " + config.elementText("command") + " , result is null");
				}
				answer.append(config.elementText("name")).append("\n");
				answer.append(CommonUtil.paddingBegin(cmd_result, "  ")).append("\n");
			}
		}
		answer.append("\n");
		return answer.toString();
	}
	
}
