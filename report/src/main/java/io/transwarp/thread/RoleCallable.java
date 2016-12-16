package io.transwarp.thread;

import io.transwarp.bean.RoleBean;
import io.transwarp.util.Constant;
import io.transwarp.util.HttpMethodTool;
import io.transwarp.util.UtilTool;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import net.sf.json.JSONArray;

import org.apache.log4j.Logger;
import org.dom4j.Element;

public class RoleCallable implements Callable<Map<String, RoleBean>>{

	private static Logger logger = Logger.getLogger(RoleCallable.class);
	private HttpMethodTool method;
	private String serviceId;
	
	public RoleCallable(HttpMethodTool method, String serviceId) {
		this.method = method;
		this.serviceId = serviceId;
	}
	
	@Override
	public Map<String, RoleBean> call() {
		Map<String, RoleBean> roles = new HashMap<String, RoleBean>();
		try {
			//获取配置
			Element config = Constant.prop_restapi.getElement("purpose", Constant.FIND_MORE_SERVICE_ROLE);
			//构建url
			Map<String, Object> urlParam = new HashMap<String, Object>();
			urlParam.put("serviceId", this.serviceId);
			String url = UtilTool.buildURL(config.elementText("url"), urlParam);
			//执行http方法获取结果
			String exec_result = this.method.execute(url, config.elementText("http-method"), null);
/*			
			FileWriter writer = new FileWriter("/home/xhy/temp/serviceRole.json");
			writer.write(exec_result);
			writer.close();*/
			JSONArray array = JSONArray.fromObject(exec_result);
			int number = array.size();
			logger.info("found service role number is " + number);
			for(int i = 0; i < number; i++) {
				RoleBean role = new RoleBean(array.getJSONObject(i));
				String serviceName = role.getService().getName();
				roles.put(serviceName + role.getName(), role);
			}
		}catch(Exception e) {
			logger.error("find service role error : " + e.getMessage());
		}
		return roles;
	}
	
}
