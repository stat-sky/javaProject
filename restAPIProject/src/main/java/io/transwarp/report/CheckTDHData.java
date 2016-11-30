package io.transwarp.report;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import io.transwarp.api.TableAPI;
import io.transwarp.bean.TableBean;
import io.transwarp.util.CommonString;
import io.transwarp.util.CommonUtil;
import io.transwarp.util.SessionTool;

import org.apache.log4j.Logger;

public class CheckTDHData {

	private static Logger logger = Logger.getLogger(CheckTDHData.class);
	private String securityType = "simple";
	private SessionTool session;
	private String ipAddress;
	private String nodeUser;
	private String nodePwd;
	
	public CheckTDHData(String securityType, String ipAddress, String nodeUser, String nodePwd) {
		this.securityType = securityType;
		this.ipAddress = ipAddress;
		this.nodeUser = nodeUser;
		this.nodePwd = nodePwd;
		try {
			this.session = SessionTool.getSession(ipAddress, nodeUser, nodePwd);
			if(this.securityType.equals("kerberos") || this.securityType.equals("all")) {
				this.sendShellOfKerberos();
			}
		} catch (Exception e) {
			logger.error("error at sessionTool : " + e.getMessage());
		}
	}
	@Override
	public String toString() {
		StringBuffer answer = new StringBuffer();
		answer.append(this.getHDFSReport()).append("\n\n");
		answer.append(this.checkTables());
		
		this.close();
		return answer.toString();
	}
	
	public String getHDFSReport() {
		StringBuffer answer = new StringBuffer("HDFS集群数据检测 :\n");
		String[] cmdTypes = CommonString.prop_report.getProperty("HDFSCheck").split(";");
		for(String cmdType : cmdTypes) {
			String cmd = CommonString.prop_report.getProperty(cmdType);
			answer.append("  ").append(cmdType).append(" :\n");
			String command = this.getExecCommand(this.securityType, cmd);
			logger.info("check HDFS's command is : " + command);
			try {
				String cmd_result = this.session.executeDis(command);
//				
				String[] lines = cmd_result.split("\n");
				int length = lines.length;
				for(int i = 0; i < length; i++) {
					if(i < 2) continue;
					answer.append("    ").append(lines[i]).append("\n");
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return answer.toString();
	}
	
	public String checkTables() {
		StringBuffer answer = new StringBuffer();
		answer.append("数据表检测 :\n");
		TableAPI tableAPI = new TableAPI();
		List<TableBean> tables = tableAPI.getTableInfos();
		List<String> tableInfos = new ArrayList<String>();
		logger.info("number of table is : " + tables.size());
		for(TableBean table : tables) {
			//表数据路径
			String dataPath = table.getTable_location();
			dataPath = dataPath.substring(19);
			logger.info("table path is : " + dataPath);
			//查询表空间中文件和文件夹大小
			Queue<String> queue = new LinkedList<String>();
			queue.offer(dataPath);
			while(!queue.isEmpty()) {
				try {
					//查询指定目录下文件列表，由此判断为文件还是文件夹
					String path = queue.poll();
					long sizeDir = 0;
					String cmd_query = this.getExecCommand(securityType, "hdfs dfs -ls " + path);
					logger.debug("cmd_query is " + cmd_query);
					String cmd_result = this.session.executeDis(cmd_query);
					String[] lines = cmd_result.split("\n");
					for(String line : lines) {
						String[] items = line.replaceAll("\\s+", ",").split(",");
						if(items.length < 8 || !items[0].matches("[-dwxr]+")) continue;
						if(items[0].indexOf("d") != -1) {
							queue.offer(items[7]);
						}else {
							long sizeFile = Long.valueOf(items[4]);
							logger.debug("file size is : " + sizeFile);
							table.addFile(sizeFile);
							sizeDir += sizeFile;
						}
					}
					if(sizeDir > 0 && !path.equals(dataPath)) table.addDir(sizeDir);
					logger.debug("directory size is : " + sizeDir);
				} catch(Exception e) {
					logger.error("query dir or file error : " + e.getMessage());
				}				
			}
			StringBuffer tableInfo = new StringBuffer();
			tableInfo.append(table.getDatabase_name()).append(",");
			tableInfo.append(table.getOwner_name()).append(",");
			tableInfo.append(table.getTable_name()).append(",");
			tableInfo.append(table.checkTableType()).append(",");
			tableInfo.append(table.getMaxDir()).append(",");
			tableInfo.append(table.getMinDir()).append(",");
			tableInfo.append(table.getCountDir()).append(",");
			tableInfo.append(table.getAvgDir()).append(",");
			tableInfo.append(table.getMaxFile()).append(",");
			tableInfo.append(table.getMinFile()).append(",");
			tableInfo.append(table.getCountFile()).append(",");
			tableInfo.append(table.getAvgFile());
			
			tableInfos.add(tableInfo.toString());
		}
		String tableListStr = CommonUtil.printByTable(new String[]{"database", "owner", "table", "type", "maxDir", "minDir", "countDir", "avgDir", "maxFile", "minFile", "countFile", "avgFile"}, tableInfos, 15);
		answer.append(CommonUtil.paddingBegin(tableListStr, "  "));
		return answer.toString();
	}
	
	private String getExecCommand(String securityType, String command) {
		if(securityType.equals("simple") || securityType.equals("ldap")) {
			return "sudo -u hdfs " + command;
		}else {
			StringBuffer shellCmd = new StringBuffer("sh ");
			//设置执行脚本
			shellCmd.append("/tmp/execAtKRB.sh");
			//设置脚本参数
			shellCmd.append(" ").append(CommonString.prop_env.getProperty("hdfsPwd"));
			shellCmd.append(" \"").append(command).append("\"");
			return shellCmd.toString();
		}
	}
	
	private void sendShellOfKerberos() {
		//将脚本传到目标节点
		StringBuffer scpCmd = new StringBuffer("sh ");
		scpCmd.append(CommonString.prop_report.getProperty("scp_shell"));
		//设置执行参数
		scpCmd.append(" ").append(this.nodePwd);
		scpCmd.append(" ").append(CommonString.prop_report.getProperty("shellOfKerberos"));
		scpCmd.append(" ").append(this.nodeUser).append("@").append(this.ipAddress).append(":").append("/tmp/");
		try {
			logger.info(scpCmd.toString());
			SessionTool.executeLocal(scpCmd.toString());
		} catch (Exception e) {
			logger.error("scp file of execute shell error : " + e.getMessage());
			e.printStackTrace();
		}		
	}
	
	private void close() {
		try {
			this.session.executeDis("rm /tmp/execAtKRB.sh -f");
			this.session.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
