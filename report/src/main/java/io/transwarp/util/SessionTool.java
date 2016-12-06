package io.transwarp.util;

import java.io.InputStream;

import org.apache.log4j.Logger;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;

public class SessionTool {

	private static Logger logger = Logger.getLogger(SessionTool.class);
	private static SessionTool sessionTool = null;
	private Connection connection = null;
	
	private static String ipAddress = "";
	private static String nodeUser = "";
	private static String nodePwd = "";
	
	private SessionTool(String ipAddress1, String nodeUser1, String nodePwd1) throws Exception {
		ipAddress = ipAddress1;
		nodeUser = nodeUser1;
		nodePwd = nodePwd1;
		
		this.connection = new Connection(ipAddress);
		this.connection.connect();
		this.connection.authenticateWithPassword(nodeUser, nodePwd);
	}
	
	public static SessionTool getSession(String ipAddress, String nodeUser, String nodePwd) throws Exception{
		if(sessionTool == null || checkInfo(ipAddress, nodeUser, nodePwd)) {
			sessionTool = new SessionTool(ipAddress, nodeUser, nodePwd);
		}
		return sessionTool;
	}
	
	public static boolean checkInfo(String ipAddress1, String nodeUser1, String nodePwd1) {
		boolean ok = true;
		if(ipAddress1 == null || ipAddress.equals("") || nodeUser1 == null || nodeUser1.equals("") || nodePwd1 == null || nodePwd1.equals("")) {
			ok = false;
			logger.error("ipAddress, nodeUser, nodePwd must not be null");
		}
		if(ipAddress.equals(ipAddress1) || nodeUser.equals(nodeUser1) || nodePwd.equals(nodePwd1)) {
			ok = false;
		}
		return ok;
	}
	
	/**
	 * 在指定节点远程执行shell语句
	 * @param command 需要执行的shell语句
	 * @return shell语句的执行结果
	 * @throws Exception
	 */
	public String executeDis(String command) throws Exception {
		Session session = connection.openSession();
		session.execCommand(command);
		InputStream inputStream = session.getStdout();
		String result = UtilTool.changeInputStreamToString(inputStream);
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
		String result = UtilTool.changeInputStreamToString(inputStream);
		inputStream.close();
		logger.debug("command : \"" + command + "\" , result is \"" + result);
		return result;
	}
	
	public static void close() {
		if(sessionTool != null) {
			sessionTool.connection.close();
			sessionTool = null;
		}
	}
}
