package io.transwarp.api;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import io.transwarp.bean.MetricBean;
import io.transwarp.bean.NodeBean;
import io.transwarp.bean.RoleBean;
import io.transwarp.bean.ServiceBean;
import io.transwarp.thread.MetricCallable;
import io.transwarp.util.CommonString;
import io.transwarp.util.HttpMethod;

import org.apache.log4j.Logger;


public class RoleAPI {

	private HttpMethod method = null;
	private Map<String, ServiceBean> services = null;
	private List<String> nodeInfo = new ArrayList<String>();
	
	public RoleAPI() {
		this.method = HttpMethod.getMethod();
		this.services = new ServiceAPI().getAllServices("idname");

	}
	
	public RoleAPI(String manager, String username, String password) {
		this(HttpMethod.getMethod(manager, username, password));
	}
	public RoleAPI(HttpMethod method) {
		this.method = method;
		this.services = new ServiceAPI().getAllServices("idname");

	}

	public String getRoleIdByType(String roleType) {
		for(Iterator<String> keys = this.services.keySet().iterator(); keys.hasNext(); ) {
			String key = keys.next();
			ServiceBean service = services.get(key);
			List<RoleBean> roles = service.getRoles();
			for(RoleBean role : roles) {
				if(role.getRoleType().equalsIgnoreCase(roleType)) {
					return role.getId();
				}
			}
		}
		return null;
	}
	
	/**
	 * 获取指定服务角色类型的所有配置
	 * @param roleType
	 * @param startTimeStamp
	 * @param endTimeStamp
	 * @return
	 */
	public Map<String, MetricBean> getMetrics(String roleType, long startTimeStamp, long endTimeStamp) {
		roleType = roleType.toUpperCase();
		String[] metricNames = CommonString.prop_metric.getProperty(roleType).split(",");
		return this.getMetrics(roleType, startTimeStamp, endTimeStamp, metricNames);
	}
	
	public Map<String, MetricBean> getMetrics(String roleType, long startTimeStamp, long endTimeStamp, String metricName) {
		return this.getMetrics(roleType, startTimeStamp, endTimeStamp, new String[]{metricName});
	}
	public Map<String, MetricBean> getMetrics(String roleType, long startTimeStamp, long endTimeStamp, String[] metricNames) {
		roleType = roleType.toUpperCase();
		Map<String, MetricBean> metrics = new HashMap<String, MetricBean>();
		String roleId = this.getRoleIdByType(roleType);
		if(roleId == null) {
			System.err.println("this role type is not exist");
			System.exit(1);
		}
		
		ExecutorService threadPool = Executors.newFixedThreadPool(metricNames.length);
		CompletionService<Map<String, MetricBean>> completionService = new ExecutorCompletionService<Map<String, MetricBean>>(threadPool);
		
		for(String metricName : metricNames) {
			MetricCallable metricCallable = new MetricCallable(this.method, "role");
			metricCallable.setGoal(roleId, metricName, startTimeStamp, endTimeStamp);
			completionService.submit(metricCallable);
		}
		
		for(int i = 0; i < metricNames.length; i++) {
			try {
				Map<String, MetricBean> buffer = completionService.take().get();
				for(Iterator<String> keys = buffer.keySet().iterator(); keys.hasNext(); ) {
					String key = keys.next();
					metrics.put(key, buffer.get(key));
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		return metrics;
	}
	
	/**
	 * 将所有服务角色在节点的分布输出到xlsx或xls文件
	 * @param path xlsx或xls文件路径
	 */
	public void writeRoleMapToExecl(String path) {
		int column = 2;
		for(Iterator<String> keys = services.keySet().iterator(); keys.hasNext(); ) {
			String key = keys.next();
			ServiceBean service = services.get(key);
			column += this.writeRoleMapToExecl(path, service, column);
//			System.out.println("column : " + column);
		}
		
	}
	
	/**
	 * 将一个服务的所有角色写入execl文件，返回写入了几列
	 * @param service 写入的服务
	 * @param column 从第几列开始写
	 */
	private int writeRoleMapToExecl(String path, ServiceBean service, int column) {
		List<String> roleType = new ArrayList<String>();
		List<RoleBean> roles = service.getRoles();
		WritableWorkbook workbook = null;
		try {
			//创建工作表
			File file = new File(path);
			if(!file.exists()) {
				workbook = Workbook.createWorkbook(file);
				workbook.createSheet("roleMap", 0);
				workbook.write();
				workbook.close();
			}
			//为实现覆盖写入，即在已有数据上叠加数据，需要如下方式建立
			workbook = Workbook.createWorkbook(file, Workbook.getWorkbook(file));
			if(workbook == null) {
				System.err.println("no file");
				System.exit(1);
			}
			WritableSheet sheet = workbook.getSheet("roleMap");
			if(sheet == null) {
				sheet = workbook.createSheet("roleMap", 0);
			}
			
			for(RoleBean role : roles) {
				int x = -1, y = -1;
				//判断x坐标
				NodeBean node = role.getNode();
				String hostname = node.getHostName();
				if(this.nodeInfo.contains(hostname)) {
					x = this.nodeInfo.indexOf(hostname) + 2;  //前两行为服务和服务角色的标题
				}else {
					x = this.nodeInfo.size() + 2;
					this.nodeInfo.add(hostname);
					//将节点信息写入
					Label cell_ip = new Label(0, x, node.getIpAddress());
					Label cell_host = new Label(1, x, node.getHostName());
					sheet.addCell(cell_host);
					sheet.addCell(cell_ip);
				}
				//判断y坐标
				if(roleType.contains(role.getRoleType())) {
					y = roleType.indexOf(role.getRoleType()) + column;
				}else {
					y = roleType.size() + column;
					roleType.add(role.getRoleType());
					//将角色类型写入
					Label cell_role = new Label(y, 1, role.getRoleType());
					sheet.addCell(cell_role);
				}
				
				//将角色状态写入表格
//				System.out.println("xy : " + x + "  " + y);
				sheet.addCell(new Label(y, x, role.getHealth()));
			}
//			System.out.println(service.getType() + " : " + roleType.size());
			if(roleType.size() > 0) {
				sheet.mergeCells(column, 0, column + roleType.size() - 1, 0);
				sheet.addCell(new Label(column, 0, service.getType()));				
			}

			workbook.write();
			
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				workbook.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return roleType.size();
	}
	
	
	
}
