package io.transwarp.api;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import io.transwarp.bean.MetricBean;
import io.transwarp.bean.ServiceBean;
import io.transwarp.thread.MetricCallable;
import io.transwarp.thread.ServiceCallable;
import io.transwarp.util.CommonString;
import io.transwarp.util.HttpMethod;


public class ServiceAPI {

	private static Logger logger = Logger.getLogger(ServiceAPI.class);
	private HttpMethod method;
	
	public ServiceAPI() {
		this(HttpMethod.getMethod());
	}
	public ServiceAPI(HttpMethod method) {
		this.method = method;
	}
	
	public Map<String, ServiceBean> getAllServices(String viewtype) {
		ServiceCallable serviceCallable = new ServiceCallable(null, viewtype);
		return serviceCallable.call();
	}
	
	public ServiceBean getService(String serviceName, String viewtype) {
		Map<String, ServiceBean> services = this.getAllServices(viewtype);
		ServiceBean service = services.get(serviceName);
		return service;
	}

	public Map<String, MetricBean> getMetrics(String serviceName, long startTimeStamp, long endTimeStamp) {
		serviceName = serviceName.toUpperCase();
		String[] metricNames = CommonString.prop_metric.getProperty(serviceName).split(",");
		return this.getMetrics(serviceName, startTimeStamp, endTimeStamp, metricNames);
	}
	
	public Map<String, MetricBean> getMetrics(String serviceName, long startTimeStamp, long endTimeStamp, String metricName) {
		return this.getMetrics(serviceName, startTimeStamp, endTimeStamp, new String[]{metricName});
	}
	
	public Map<String, MetricBean> getMetrics(String serviceName, long startTimeStamp, long endTimeStamp, String[] metricNames) {
		Map<String, MetricBean> metrics = new HashMap<String, MetricBean>();
		
		serviceName = serviceName.toUpperCase();
		String serviceId = this.getService(serviceName, "idname").getId();
		
		ExecutorService threadPool = Executors.newFixedThreadPool(metricNames.length);
		CompletionService<Map<String, MetricBean>> completionService = new ExecutorCompletionService<Map<String, MetricBean>>(threadPool);
		
		for(String metricName : metricNames) {
			MetricCallable metricCallable = new MetricCallable(this.method, "service");
			metricCallable.setGoal(serviceId, metricName, startTimeStamp, endTimeStamp);
			completionService.submit(metricCallable);
		}
		
		for(int i = 0; i < metricNames.length; i++) {
			try {
				Map<String, MetricBean> buffer = completionService.take().get();
				for(Iterator<String> keys = buffer.keySet().iterator(); keys.hasNext();) {
					String key = keys.next();
					metrics.put(key, buffer.get(key));
				}
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
			}
		}
		return metrics;
	}
}
