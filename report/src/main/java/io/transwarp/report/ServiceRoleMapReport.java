package io.transwarp.report;

import io.transwarp.bean.NodeBean;
import io.transwarp.bean.RoleBean;
import io.transwarp.bean.ServiceBean;
import io.transwarp.util.PrintToTableUtil;
import io.transwarp.util.UtilTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ServiceRoleMapReport {

	private Map<String, NodeBean> nodes;
	private Map<String, ServiceBean> services;
	
	public ServiceRoleMapReport(Map<String, NodeBean> nodes, Map<String, ServiceBean> services) {
		this.nodes = nodes;
		this.services = services;
	}
	
	public String getRoleMap() {
		StringBuffer answer = new StringBuffer("服务角色分布 : \n");
		List<String[]> maps = new ArrayList<String[]>();
		//将节点按照所属机柜划分 —— key为rack name，value 为hostname
		Map<String, List<String>> racks = new HashMap<String, List<String>>();
		for(Iterator<String> keys = this.nodes.keySet().iterator(); keys.hasNext(); ) {
			String key = keys.next();
			NodeBean node = this.nodes.get(key);
			String rackName = node.getRackName();
			List<String> rackNodes = racks.get(rackName);
			if(rackNodes == null) {
				rackNodes = new ArrayList<String>();
			}
			rackNodes.add(node.getHostName());
			racks.put(rackName, rackNodes);
		}
		//列数 ——- 多的两列为服务列和服务角色列
		int columnCount = this.nodes.size() + 2;
		//构建表格前两行 ———— 为节点分布
		String[] firstLine = new String[columnCount];
		firstLine[0] = "Rack"; firstLine[1] = null;
		String[] secondLine = new String[columnCount];
		secondLine[0] = "Node"; secondLine[1] = null;
		int nowColumn = 2;  //目前在写的为第几列
		for(Iterator<String> rackNames = racks.keySet().iterator(); rackNames.hasNext(); ) {
			String rackName = rackNames.next();
			List<String> hostnames = racks.get(rackName);
			int nodeNum = hostnames.size();
			for(int i = 0; i < nodeNum; i++) {
				if(i == 0) {
					firstLine[nowColumn] = rackName;
				}
				secondLine[nowColumn] = hostnames.get(i);
				nowColumn++;
			}
		}
		maps.add(firstLine);
		maps.add(secondLine);
		//按照所属服务添加服务角色分布
		for(Iterator<String> keys = this.services.keySet().iterator(); keys.hasNext(); ) {
			String key = keys.next();
			ServiceBean service = this.services.get(key);
			List<RoleBean> roles = service.getRoles();
			//存储每种服务角色分布，key为服务角色类型,value为角色分布
			Map<String, String[]> roleMaps = new HashMap<String, String[]>();
			for(RoleBean role : roles) {
				String roleType = role.getRoleType();
				String[] roleMap = roleMaps.get(roleType);
				if(roleMap == null) {
					roleMap = new String[columnCount];
				}
				roleMap[1] = roleType;
				//查询服务角色所在节点所在的列
				String hostname = role.getHostName();
				for(int i = 2; i < columnCount; i++) {
					if(secondLine[i] != null && hostname.equals(secondLine[i])) {
						roleMap[i] = role.getHealth();
						break;
					}
				}
				roleMaps.put(roleType, roleMap);
			}
			
			//将信息放入缓存maps，准备用来生成表格
			boolean firstRole = true;
			for(Iterator<String> roleTypes = roleMaps.keySet().iterator(); roleTypes.hasNext(); ) {
				String roleType = roleTypes.next();
				String[] roleMap = roleMaps.get(roleType);
				if(firstRole) {  //第一个服务角色前加服务名称
					roleMap[0] = key;
					firstRole = false;
				}
				for(int i = 2; i < columnCount; i++) {
					if(roleMap[i] == null) roleMap[i] = "";
				}
				maps.add(roleMap);
			}
		}
		try {
			answer.append(UtilTool.retract(PrintToTableUtil.printToTable(maps, 30), "  "));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return answer.toString();
	}
}
