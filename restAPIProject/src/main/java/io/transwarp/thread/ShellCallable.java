package io.transwarp.thread;

import io.transwarp.util.SessionTool;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

public class ShellCallable implements Callable<String> {

	private static Logger logger = Logger.getLogger(ShellCallable.class);
	private String cmd;
	private SessionTool session;

	public ShellCallable(String ipAddress, String username, String password, String cmd) throws Exception{
		this(SessionTool.getSession(ipAddress, username, password), cmd);
	}
	public ShellCallable(SessionTool session, String cmd) {
		this.cmd = cmd;
		this.session = session;
	}
	
	@Override
	public String call() throws Exception{
		String result = this.session.executeDis(cmd);
		return result;
	}
}
