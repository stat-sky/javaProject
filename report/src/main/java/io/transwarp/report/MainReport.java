package io.transwarp.report;

import io.transwarp.bean.NodeBean;
import io.transwarp.bean.RoleBean;
import io.transwarp.bean.ServiceBean;
import io.transwarp.thread.NodeCallable;
import io.transwarp.thread.RoleCallable;
import io.transwarp.thread.ServiceCallable;
import io.transwarp.util.Constant;
import io.transwarp.util.HttpMethodTool;

import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

public class MainReport {

	private static Logger logger = Logger.getLogger(MainReport.class);
	private Map<String, ServiceBean> services;
	private Map<String, NodeBean> nodes;
	private Map<String, String> configs;
	private String security;
	private HttpMethodTool method;
	
	public MainReport() throws Exception {
		this(HttpMethodTool.getMethod(), Constant.prop_env.getProperty("security"));
	}
	public MainReport(HttpMethodTool method, String security) {
		this.security = security;
		this.method = method;
		init();
	}
	
	private void init() {
		//查询所有服务信息
		ServiceCallable serviceCallable = new ServiceCallable(this.method, "summary", null);
		this.services = serviceCallable.call();
		//查询所有节点信息
		NodeCallable nodeCallable = new NodeCallable(this.method, "summary", null);
		this.nodes = nodeCallable.call();
		//查询所有服务角色信息
		RoleCallable roleCallable = new RoleCallable(this.method, null);
		Map<String, RoleBean> roles = roleCallable.call();
		for(Iterator<String> keys = roles.keySet().iterator(); keys.hasNext(); ) {
			String key = keys.next();
			RoleBean role = roles.get(key);
			String serviceName = role.getService().getName();
			this.services.get(serviceName).addRole(role);
		}
		//获取平台服务配置信息
		
	}
}
