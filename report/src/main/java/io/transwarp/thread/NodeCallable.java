package io.transwarp.thread;

import io.transwarp.bean.NodeBean;
import io.transwarp.util.Constant;
import io.transwarp.util.HttpMethodTool;
import io.transwarp.util.UtilTool;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.Callable;

import net.sf.json.JSONArray;

import org.apache.log4j.Logger;
import org.dom4j.Element;

public class NodeCallable implements Callable<Map<String, NodeBean>>{

	private static Logger logger = Logger.getLogger(NodeCallable.class);
	private HttpMethodTool method;
	private String nodeId;
	private String viewtype;
	
	public NodeCallable(HttpMethodTool method, String viewtype, String nodeId) {
		this.method = method;
		this.viewtype = viewtype;
		this.nodeId = nodeId;
	}
	
	@Override
	public Map<String, NodeBean> call() {
		Map<String, NodeBean> nodes = new HashMap<String, NodeBean>();
		try {
			Element config = null;
			if(this.nodeId == null) {  //根据节点ID的有无判断是查询多个节点还是一个节点
				config = Constant.prop_restapi.getElement("purpose", Constant.FIND_MORE_NODE);
			}else {
				config = Constant.prop_restapi.getElement("purpose", Constant.FIND_NODE);
			}
			//构建url
			Map<String, Object> urlParam = new HashMap<String, Object>();
			urlParam.put("nodeId", this.nodeId);
			urlParam.put("viewtype", this.viewtype);
			String url = UtilTool.buildURL(config.elementText("url"), urlParam);
			//执行http方法获取结果
			String exec_result = this.method.execute(url, config.elementText("http-method"), null);
			if(nodeId == null) {		//一个节点和多个节点的结果解析不同
				JSONArray array = JSONArray.fromObject(exec_result);
				int length = array.size();
				logger.info("there are " + length + " nodes");
				for(int i = 0; i < length; i++) {
					NodeBean node = new NodeBean(array.getJSONObject(i));
					nodes.put(node.getHostName(), node);
				}
			}else {
				NodeBean node = new NodeBean(exec_result);
				nodes.put(node.getHostName(), node);
			}
		}catch(Exception e) {
			logger.error("find node information error : " + e.getMessage());
		}
		return nodes;
	}
}
