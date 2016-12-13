package io.transwarp.report;

import java.util.ArrayList;
import java.util.List;

import io.transwarp.bean.NodeBean;
import io.transwarp.util.Constant;
import io.transwarp.util.PrintToTableUtil;
import io.transwarp.util.SessionTool;
import io.transwarp.util.UtilTool;

import org.apache.log4j.Logger;
import org.dom4j.Element;

public class NodeBaseReport {

	private static Logger logger = Logger.getLogger(NodeBaseReport.class);
	private NodeBean node;
	private String nodeUser;
	private String nodePwd;
	private SessionTool session;
	
	public NodeBaseReport(NodeBean node, String nodeUser, String nodePwd) {
		this.node = node;
		this.nodeUser = nodeUser;
		this.nodePwd = nodePwd;
		try {
			this.session = SessionTool.getSession(this.node.getIpAddress(), this.nodeUser, this.nodePwd);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getNodeCheckReport() {
		StringBuffer report = new StringBuffer();
		report.append(this.getNodeInfo());
		report.append(this.getBaseCheck());
		report.append(this.getPortCheck());
		this.session.close();
		return report.toString();
	}
	
	public String getNodeInfo() {
		StringBuffer answer = new StringBuffer("节点基本信息 : \n");
		List<String[]> maps_nodeBase = new ArrayList<String[]>();
		//标题
		maps_nodeBase.add(new String[]{"parameter", "value"});
		//节点信息
		maps_nodeBase.add(new String[]{"isManager", node.getIsManaged()});
		maps_nodeBase.add(new String[]{"hostname", node.getHostName()});
		maps_nodeBase.add(new String[]{"ipAddress", node.getIpAddress()});
		maps_nodeBase.add(new String[]{"clusterName", node.getClusterName()});
		maps_nodeBase.add(new String[]{"rackName", node.getRackName()});
		maps_nodeBase.add(new String[]{"status", node.getStatus()});
		maps_nodeBase.add(new String[]{"numCores", node.getNumCores()});
		maps_nodeBase.add(new String[]{"totalPhysMemBytes", node.getTotalPhysMemBytes()});
		maps_nodeBase.add(new String[]{"cpu", node.getCpu()});
		maps_nodeBase.add(new String[]{"osType", node.getOsType()});
		try {
			answer.append(PrintToTableUtil.printToTable(maps_nodeBase, 50));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return answer.toString();
	}
	
	public String getBaseCheck() {
		StringBuffer answer = new StringBuffer();
		try {
			//系统参数检测
			Element systemCheckConfig = Constant.prop_report.getElement("topic", "OS");
			answer.append(systemCheckConfig.elementText("name")).append("\n");
			String[] system_params = systemCheckConfig.elementText("property").split(";");
			List<String[]> maps_systemCheck = new ArrayList<String[]>();
			maps_systemCheck.add(new String[]{"parameter", "value"});
			for(String param : system_params) {
				Element config = Constant.prop_report.getElement("topic", param);
				String command = config.elementText("command");
				String result = this.session.executeDis(command).trim();
				maps_systemCheck.add(new String[]{param, result});
			}
			answer.append(UtilTool.retract(PrintToTableUtil.printToTable(maps_systemCheck, 20), "  ")).append("\n\n");
			

			//系统环境检测
			Element env_config = Constant.prop_report.getElement("topic", "environment");
			answer.append(env_config.elementText("name"));
			
			//NTP同步检测
			Element ntp_config = Constant.prop_report.getElement("topic", "NTP");
			answer.append(ntp_config.elementText("name")).append("\n");
			List<String[]> maps_ntp = new ArrayList<String[]>();
			String ntp_command = ntp_config.elementText("command");
			String ntp_result = this.session.executeDis(ntp_command);
			String[] ntp_lines = ntp_result.split("\n");
			for(String ntp_line : ntp_lines) {
				String[] items = ntp_line.trim().split("\\s+");
				if(items.length > 1) {
					maps_ntp.add(items);
				}
			}
			answer.append(UtilTool.retract(PrintToTableUtil.printToTable(maps_ntp, 10), "  ")).append("\n");

			//其他环境检测项
			String[] env_params = env_config.elementText("property").split(";");
			for(String property : env_params) {
				Element config = Constant.prop_report.getElement("topic", property);
				answer.append(config.elementText("name")).append("\n");
				String command = config.elementText("command");
				String result = this.session.executeDis(command);
				answer.append(UtilTool.retract(result, "  ")).append("\n");
			}
			
			//网络配置检测
			Element network_config = Constant.prop_report.getElement("topic", "network");
			String[] network_params = network_config.elementText("property").split(";");
			for(String param : network_params) {
				Element config = Constant.prop_report.getElement("topic", param);
				answer.append(config.elementText("name")).append("\n");
				String command = config.elementText("command");
				String result = this.session.executeDis(command);
				answer.append(UtilTool.retract(result, "  ")).append("\n");
			}
			
			//磁盘检测
			Element disk_config = Constant.prop_report.getElement("topic", "disk");
			String[] disk_params = disk_config.elementText("property").split(";");
			for(String param : disk_params) {
				Element config = Constant.prop_report.getElement("topic", param);
				answer.append(config.elementText("name")).append("\n");
				String command = config.elementText("command");
				String result = this.session.executeDis(command);
				List<String[]> maps_disk = new ArrayList<String[]>();
				String[] lines = result.split("\n");
				for(String line : lines) {
					if(line.startsWith("#") || line.trim().equals("")) continue;
					maps_disk.add(line.trim().split("\\s+"));
				}
				answer.append(UtilTool.retract(PrintToTableUtil.printToTable(maps_disk, 30), "  "));
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return answer.toString();
	}
	
	public String getPortCheck() {
		StringBuffer answer = new StringBuffer("端口连接数检测 :\n");
		List<Element> configs = Constant.prop_portCheck.getAll();
		List<String[]> maps = new ArrayList<String[]>();
		maps.add(new String[]{"服务角色", "端口号", "连接数"});
		for(Element config : configs) {
			String roleType = config.elementText("roleType");
			String port = config.elementText("port");
			String command = config.elementText("command");
			try {
				String exec_result = this.session.executeDis(command).trim();
				maps.add(new String[]{roleType, port, exec_result});
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		try {
			answer.append(PrintToTableUtil.printToTable(maps, 30));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return answer.toString();
	}
}
