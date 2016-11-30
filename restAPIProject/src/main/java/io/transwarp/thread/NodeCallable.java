package io.transwarp.thread;

import io.transwarp.bean.NodeBean;
import io.transwarp.util.CommonUtil;
import io.transwarp.util.CommonString;
import io.transwarp.util.HttpMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import net.sf.json.JSONArray;

import org.apache.log4j.Logger;
import org.dom4j.Element;

public class NodeCallable implements Callable<Map<String, NodeBean>>{

	private static Logger logger = Logger.getLogger(NodeCallable.class);
	private HttpMethod method;
	private String nodeId;
	private String viewtype;
	
	public NodeCallable(String nodeId, String viewtype) {
		this(nodeId, viewtype, HttpMethod.getMethod());
	}
	public NodeCallable(String nodeId, String viewtype, HttpMethod method) {
		this.nodeId = nodeId;
		this.viewtype = viewtype;
		this.method = method;
	}
	
	@Override
	public Map<String, NodeBean> call() {
		Map<String, NodeBean> nodes = new HashMap<String, NodeBean>();
		try {
			Map<String, Object> urlParam = new HashMap<String, Object>();
			urlParam.put("nodeId", nodeId);
			urlParam.put("viewtype", viewtype);
			Element config = null;
			if(nodeId == null) {
				config = CommonString.config_restAPI.getElementByChild("purpose", CommonString.FIND_MORE_NODE);
			}else {
				config = CommonString.config_restAPI.getElementByChild("purpose", CommonString.FIND_NODE);
			}
			
			String url = CommonUtil.buildURL(config.elementText("url"), urlParam);
			String httpMethod = config.elementText("http-method");
			String result = this.method.execute(url, httpMethod);
			if(nodeId == null) {
				JSONArray array = JSONArray.fromObject(result);
				int length = array.size();
				logger.info("there are " + length + " nodes");
				for(int i = 0; i < length; i++) {
					NodeBean node = new NodeBean(array.getJSONObject(i));
					nodes.put(node.getHostName(), node);
				}
			}else {
				NodeBean node = new NodeBean(result);
				nodes.put(node.getHostName(), node);
			}
		}catch(Exception e) {
			logger.error("find node info is error : " + e.getMessage());
		}
		return nodes;
	}
}
