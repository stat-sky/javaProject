package io.transwarp.thread;

import io.transwarp.bean.RoleBean;
import io.transwarp.util.CommonUtil;
import io.transwarp.util.CommonString;
import io.transwarp.util.HttpMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import net.sf.json.JSONArray;

import org.apache.log4j.Logger;
import org.dom4j.Element;

/**
 * 实现功能：
 * 		1、传入参数serviceId则查找指定服务下的所有角色；
 * 		2、不传入参数serviceId则查找所有服务角色；
 * @author 30453
 *
 */
public class RoleCallable implements Callable<Map<String, RoleBean>>{

	private static Logger logger = Logger.getLogger(RoleCallable.class);
	private HttpMethod method;
	private String serviceId;
	
	public RoleCallable(String serviceId) {
		this(serviceId, HttpMethod.getMethod());
	}
	public RoleCallable(String serviceId, HttpMethod method) {
		this.method = method;
		this.serviceId = serviceId;
	}
	
	@Override
	public Map<String, RoleBean> call() {
		Map<String, RoleBean> roles = new HashMap<String, RoleBean>();
		try {
			//构建调用restapi的url
			Map<String, Object> urlParam = new HashMap<String, Object>();
			urlParam.put("serviceId", serviceId);
			Element config = CommonString.config_restAPI.getElementByChild("purpose", CommonString.FIND_MORE_SERVICE_ROLE);
			String url = CommonUtil.buildURL(config.elementText("url"), urlParam);
			String httpMethod = config.elementText("http-method");
			
			//调用rest api
			String result = this.method.execute(url, httpMethod);
			//解析结果
			JSONArray array = JSONArray.fromObject(result);
			int length = array.size();
			logger.info("there are " + length + " service's role have been found");
			for(int i = 0; i < length; i++) {
				RoleBean role = new RoleBean(array.getJSONObject(i));
				roles.put(role.getName(), role);
			}
		}catch(Exception e) {
			logger.error("find service's role is error : " + e.getMessage());
		}
		return roles;
	}
}
