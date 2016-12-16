package io.transwarp.report;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import io.transwarp.bean.TableBean;
import io.transwarp.thread.SqlQueryCallable;
import io.transwarp.util.Constant;
import io.transwarp.util.JDBCConnectionTool;
import io.transwarp.util.PrintToTableUtil;
import io.transwarp.util.SessionTool;
import io.transwarp.util.UtilTool;

import org.apache.log4j.Logger;
import org.dom4j.Element;

public class TDHDataReport {

	private static Logger logger = Logger.getLogger(TDHDataReport.class);
	private String security;
	//连接节点信息
	private String ipAddress;
	private String nodeUser;
	private String nodePwd;
	//连接jdbc信息
	private Connection conn;
	//hdfs用户密码-用于kerberos认证
	private String hdfsPwd;
	
	private SessionTool session;
	
	/**
	 * 集群数据检测类
	 * @param security 集群安全模式，取simple、kerberos、ldap、all
	 * @param ipAddress 节点ip
	 * @param nodeUser 节点登录用户名
	 * @param nodePwd 节点登录密码
	 * @param inceptorURL jdbc连接串
	 * @param jdbcUser Jdbc登录用户（当安全为simple、kerberos时可以为空）
	 * @param jdbcPwd jdbc登录密码（当安全为simple、kerberos时可以为空）
	 * @param hdfsPwd hdfs用户密码（当安全为simple、ldap时可以为空）
	 */
	public TDHDataReport(String security, String ipAddress, String nodeUser, String nodePwd, String inceptorURL, String jdbcUser, String jdbcPwd, String hdfsPwd) {
		this.security = security;
		this.ipAddress = ipAddress;
		this.nodePwd = nodePwd;
		this.nodeUser = nodeUser;
		this.hdfsPwd = hdfsPwd;
		try {
			if(security.equals("kerberos") || security.equals("simple")) {
				this.conn = JDBCConnectionTool.getConnection(inceptorURL);
			}else {
				this.conn = JDBCConnectionTool.getConnection(inceptorURL, jdbcUser, jdbcPwd);
			}
			this.session = SessionTool.getSession(ipAddress, nodeUser, nodePwd);
			if(security.equals("kerberos") || security.equals("all")) {
				this.sendShellScript();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取数据检测报告
	 * @return
	 */
	public String getDataReport() {
		StringBuffer answer = new StringBuffer();
/*		//获取集群版本号
		answer.append("集群版本号为：").append(this.getVersion()).append("\n\n");		*/
		//获取集群整体检测结果
		answer.append(this.getHDFSReport()).append("\n\n");
		//获取集群中表空间检测结果
		answer.append(this.getTableInfoReport()).append("\n\n");
		this.deleteScript();
		try {
			this.conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return answer.toString();	
	}
	
	public String getVersion() {
		try {
			Statement stat = this.conn.createStatement();
			ResultSet rs = stat.executeQuery(Constant.SELECT_VERSION);
			while(rs.next()) {
				String version = rs.getString("tdh_version");
				return version;
			}
		}catch(Exception e) {
			logger.error("get tdh version error, error message is : " + e.getMessage());
		}
		return null;
	}
	
	public String getHDFSReport() {
		StringBuffer answer = new StringBuffer("HDFS集群数据检测 :\n");
		Element totalConfig = Constant.prop_report.getElement("topic", "HDFSCheck");
		String[] cmdTypes = totalConfig.elementText("property").split(";");
		for(String cmdType : cmdTypes) {
			Element config = Constant.prop_report.getElement("topic", cmdType);
			String cmd = config.elementText("command");
			answer.append("  ").append(config.elementText("name")).append("\n");
			String command = this.getExecCommand(this.security, cmd);
			logger.info("check HDFS's command is : " + command);
			try {
				String cmd_result = this.session.executeDis(command);
				
				List<String[]> maps = new ArrayList<String[]>();
				String[] lines = cmd_result.split("\n");
				for(String line : lines) {
					if(line.trim().equals("")) {
						if(maps.size() > 0) {
							answer.append(UtilTool.retract(PrintToTableUtil.printToTable(maps, 60), "  "));
							maps.clear();
						}
						answer.append("\n");
					}
					String[] items = line.split(":");
					if(items.length == 2) {
						String key = items[0].trim();
						String value = items[1].trim();
						maps.add(new String[]{key,value});
					}
				}
				if(maps.size() > 0) {
					answer.append(UtilTool.retract(PrintToTableUtil.printToTable(maps, 60), "  "));
					maps.clear();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			answer.append("\n\n");
		}
		
		return answer.toString();
	}
	
	public String getTableInfoReport() {
		StringBuffer answer = new StringBuffer("数据表检测 :\n");
		//用来建立表格的缓存数据
		List<String[]> maps = new ArrayList<String[]>();
//		maps.add(new String[]{"database", "owner", "table", "type", "maxDir", "minDir", "countDir", "avgDir", "maxFile", "minFile", "countFile", "avgFile"});
//		maps.add(new String[]{"数据库名","所有者","表名","表类型","最大文件夹大小","最小文件夹大小","总文件夹数","平均文件夹大小","最大文件大小","最小文件大小","总文件数","平均文件大小"});
		maps.add(new String[]{"数据库名","所有者","表名","表类型","文件夹:最大|最小|平均(b)","文件:最大|最小|平均(b)"});
		
		//从数据库中查询表的数据字典，获取相关信息
		SqlQueryCallable sqlQuery = new SqlQueryCallable(this.conn, Constant.SELECT_TABLES, TableBean.class);
		List<Object> tables = sqlQuery.call();
		for(Object item : tables) {
			TableBean table = (TableBean)item;
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
					String cmd_query = this.getExecCommand(security, "hdfs dfs -ls " + path);
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
			maps.add(new String[]{table.getDatabase_name(), table.getOwner_name(), table.getTable_name(), table.checkTableType(), 
					table.getMaxDir() + "|" + table.getMinDir() + "|" + table.getAvgDir(), 
					table.getMaxFile() + "|" + table.getMinFile() + "|" + table.getAvgFile()});
		}
		try {
			answer.append(UtilTool.retract(PrintToTableUtil.printToTable(maps, 30), "  "));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return answer.toString();
	}
	
	private void sendShellScript() {
		//将执行脚本发送到目标节点的/tmp目录下
		StringBuffer scpCmd = new StringBuffer("sh ");
		scpCmd.append(Constant.prop_env.getProperty("scp_script"));
		//设置执行参数
		scpCmd.append(" ").append(this.nodePwd);
		scpCmd.append(" ").append(Constant.prop_env.getProperty("kerberos_script"));
		scpCmd.append(" ").append(this.nodeUser).append("@").append(this.ipAddress).append(":").append("/tmp/");
		try {
			logger.info(scpCmd.toString());
			SessionTool.executeLocal(scpCmd.toString());
		} catch (Exception e) {
			logger.error("scp file of execute shell error : " + e.getMessage());
			e.printStackTrace();
		}	
	}
	
	private String getExecCommand(String security, String command) {
		if(security.equals("simple") || security.equals("ldap")) {
			return "sudo -u hdfs " + command;
		}else {
			StringBuffer shellCmd = new StringBuffer("sh ");
			//设置执行脚本
			shellCmd.append("/tmp/execAtKRB.sh");
			//设置脚本参数
			shellCmd.append(" ").append(hdfsPwd);
			shellCmd.append(" \"").append(command).append("\"");
			return shellCmd.toString();
		}		
	}
	
	private void deleteScript() {
		try {
			if(security.equals("all") || security.equals("kerberos")) {
				this.session.executeDis("rm /tmp/execAtKRB.sh -f");
			}
			this.session.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}
