package io.transwarp.util;

import java.io.InputStream;

import org.apache.log4j.Logger;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;


/**
 * 实现功能：
 * 		1、在指定的linux节点执行sh命令
 * 		2、在指定的linux节点执行sh脚本（包括带参和不带参）
 * @author 30453
 *
 */
public class SessionTool {

	private static Logger logger = Logger.getLogger(SessionTool.class);
	private static SessionTool sessionTool = null;
	private Connection connection = null;
	
	private static String ipAddress = "";
	private static String nodeUser = "";
	private static String nodePwd = "";
	
	private SessionTool(String ipAddress, String nodeUser, String nodePwd) throws Exception{
		SessionTool.ipAddress = ipAddress;
		SessionTool.nodeUser = nodeUser;
		SessionTool.nodePwd = nodePwd;
		
		this.connection = new Connection(ipAddress);
		this.connection.connect();
		this.connection.authenticateWithPassword(nodeUser, nodePwd);
	}
	
	public static SessionTool getSession(String ipAddress, String nodeUser, String nodePwd) throws Exception {
		if(SessionTool.sessionTool == null || checkInfo(ipAddress, nodeUser, nodePwd)) {
			SessionTool.sessionTool = new SessionTool(ipAddress, nodeUser, nodePwd);
		}
		return sessionTool;
	}
	
	private static boolean checkInfo(String ipAddress, String nodeUser, String nodePwd) {
		boolean ok = true;
		//检查登录信息是否为空
		if(ipAddress == null || ipAddress.equals("") || nodeUser == null || nodeUser.equals("") || nodePwd == null || nodePwd.equals("")) {
			ok = false;
			logger.error("ipAddress, nodeUser, nodePwd can not be null");
		}
		//检查登录用户是否已经创建连接
		if(ipAddress.equals(SessionTool.ipAddress) && nodeUser.equals(SessionTool.nodeUser) && nodePwd.equals(SessionTool.nodePwd)) {
			ok = false;
		}
		return ok;
	}
	
	public String executeDis(String command) throws Exception{
		Session session = this.connection.openSession();
		session.execCommand(command);
		InputStream inputStream = session.getStdout();
		String result = CommonUtil.changeInputStreamToString(inputStream);
		session.close();
		return result;
	}
	
	public static String executeLocal(String command) throws Exception {
		Process process = Runtime.getRuntime().exec(command);
		int exitValue = process.waitFor();
		if(exitValue != 0) {
			logger.error("execute command : \"" + command + "\" is error, error code is " + exitValue);
		}
		InputStream inputStream = process.getInputStream();
		String result = CommonUtil.changeInputStreamToString(inputStream);
		inputStream.close();
		logger.debug("command : \"" + command + "\" , result is \"" + result);
		return result;
	}
	
	public static void close() {
		if(SessionTool.sessionTool != null) {
			sessionTool.connection.close();
			sessionTool = null;
		}
		
	}
}
