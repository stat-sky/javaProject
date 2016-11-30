package io.transwarp.thread;

import io.transwarp.bean.MetricBean;
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
 * 		1、指定查询类型、查询ID、指标名称、起始时间戳、终止时间戳来获取指标
 * @author 30453
 *
 */
public class MetricCallable implements Callable<Map<String, MetricBean>> {

	private static Logger logger = Logger.getLogger(MetricCallable.class);
	private HttpMethod method;
	private String type;
	private String id;
	private String metricName;
	private long startTimeStamp = -1;
	private long endTimeStamp = -1;
	
	public MetricCallable(String type) {
		this(HttpMethod.getMethod(), type);
	}
	public MetricCallable(HttpMethod method, String type) {
		this.method = method;
		this.type = type;
	}
	
	public void setGoal(String id, String metricName, long startTimeStamp, long endTimeStamp) {
		this.id = id;
		this.metricName = metricName;
		this.startTimeStamp = startTimeStamp;
		this.endTimeStamp = endTimeStamp;
	}
	
	@Override
	public Map<String, MetricBean> call() {
		if(this.type == null || this.id == null || this.metricName == null || this.startTimeStamp == -1 || this.endTimeStamp == -1) {
			System.err.println("you need parameter : type, id, metricName, startTimeStamp, endTimeStamp");
			System.exit(1);
		}
		Map<String, MetricBean> metrics = new HashMap<String, MetricBean>();
		Element config = null;
		try {
			Map<String, Object> urlParam = new HashMap<String, Object>();
			//选择配置
			if(this.type.equalsIgnoreCase("service")) {
				urlParam.put("serviceId", this.id);
				config = CommonString.config_restAPI.getElementByChild("purpose", CommonString.INQUIRE_SERVICE_METRIC);
			}else if(this.type.equalsIgnoreCase("role")) {
				urlParam.put("roleId", this.id);
				config = CommonString.config_restAPI.getElementByChild("purpose", CommonString.INQURIE_SERVICE_ROLE_METRIC);
			}else if(this.type.equalsIgnoreCase("node")) {
				urlParam.put("nodeId", this.id);
				config = CommonString.config_restAPI.getElementByChild("purpose", CommonString.INQUIRE_NODE_METRIC);
			}else if(this.type.equalsIgnoreCase("cluster")) {
				urlParam.put("clusterId", this.id);
				config = CommonString.config_restAPI.getElementByChild("purpose", CommonString.INQUIRE_CLUSTER_METRIC);
			}else {
				System.err.println("the parameter only is service, role, node, cluster");
				System.exit(1);
			}
			urlParam.put("metricsName", this.metricName);
			urlParam.put("startTimeStamp", this.startTimeStamp);
			urlParam.put("endTimeStamp", this.endTimeStamp);
			String url = CommonUtil.buildURL(config.elementText("url"), urlParam);
			String httpMethod = config.elementText("http-method");
			String result = this.method.execute(url, httpMethod);
			JSONArray array = JSONArray.fromObject(result);
			for(int i = 0; i < array.size(); i++) {
				MetricBean metric = new MetricBean(array.getJSONObject(i));
				metrics.put(metric.getMetricName() + "-" + metric.getTimestamp(), metric);
			}
			
		}catch(Exception e) {
			logger.error(e.getMessage());
		}
		return metrics;
	}
}
