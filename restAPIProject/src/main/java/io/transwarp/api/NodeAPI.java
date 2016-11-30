package io.transwarp.api;

import io.transwarp.bean.MetricBean;
import io.transwarp.bean.NodeBean;
import io.transwarp.thread.MetricCallable;
import io.transwarp.thread.NodeCallable;
import io.transwarp.util.CommonString;
import io.transwarp.util.HttpMethod;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

/**
 * 实现功能：
 * 		1、获取指定节点的详细/简要信息；
 * 		2、获取所有节点的详细/简要信息；
 * 		3、获取指定节点的指定配置信息；
 * 		4、获取指定节点的所有配置信息；
 * @author 30453
 *
 */
public class NodeAPI {

	private static Logger logger = Logger.getLogger(NodeAPI.class);
	private HttpMethod method;
	
	public NodeAPI() {
		this(HttpMethod.getMethod());
	}
	public NodeAPI(HttpMethod method) {
		this.method = method;
	}
	
	public Map<String, NodeBean> getAllNodes(String viewtype) {
		NodeCallable nodeCallable = new NodeCallable(null, viewtype);
		Map<String, NodeBean> nodes = nodeCallable.call();
		return nodes;
	}
	
	public NodeBean getNode(String hostname, String viewtype) {
		NodeCallable nodeCallable = new NodeCallable(null, viewtype);
		Map<String, NodeBean> nodes = nodeCallable.call();
		return nodes.get(hostname);
	}
	
	public Map<String, MetricBean> getNodeMetrics(String hostName, long startTimeStamp, long endTimeStamp) {
		String[] metricNames = CommonString.prop_metric.getProperty("NODE").split(",");
		return this.getNodeMetrics(hostName, startTimeStamp, endTimeStamp, metricNames);
	}
	public Map<String, MetricBean> getNodeMetrics(String hostName, long startTimeStamp, long endTimeStamp, String metricName) {
		
		return this.getNodeMetrics(hostName, startTimeStamp, endTimeStamp, new String[]{metricName});
	}
	
	public Map<String, MetricBean> getNodeMetrics(String hostName, long startTimeStamp, long endTimeStamp, String[] metricNames) {
		Map<String, MetricBean> metrics = new HashMap<String, MetricBean>();
		String nodeId = this.getNode(hostName, "idname").getId();
		int number = metricNames.length;
		
		ExecutorService threadPool = Executors.newFixedThreadPool(number);
		CompletionService<Map<String, MetricBean>> completion = new ExecutorCompletionService<Map<String, MetricBean>>(threadPool);
		
		for(String metricName : metricNames) {
			MetricCallable metricCallable = new MetricCallable(this.method, "node");
			metricCallable.setGoal(nodeId, metricName, startTimeStamp, endTimeStamp);
			completion.submit(metricCallable);
		}
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			logger.error("error at get metric of node " + hostName + " : " + e1.getMessage());
		}
		
		for(int i = 0; i < number; i++) {
			try {
				Map<String, MetricBean> buffer = completion.take().get();
				for(Iterator<String> keys = buffer.keySet().iterator(); keys.hasNext(); ) {
					String key = keys.next();
					metrics.put(key, buffer.get(key));
				}
			} catch (InterruptedException | ExecutionException e) {
				logger.error("error at count metric : " + e.getMessage());
			}
		}
		return metrics;
	}
}
