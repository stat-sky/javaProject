package io.transwarp.bean;

import io.transwarp.util.UtilTool;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ServiceBean {

	private String id;				//服务编号
	private String name;			//服务名称
	private String dependencies;	//该服务依赖的服务的编号
	private String status;			//服务状态
	private String installed;		//是否安装
	private String health;			//健康状况
	private String configStatus;	//配置状况
	private List<HealthCheckBean> healthChecks;	//健康检测
	private String enableKerberos;	//是否开启kerberos
	private String type;			//服务类型
	private String clusterId;		//集群编号
	private String clusterName;		//集群名称
	private List<RoleBean> roles;	//属于该服务的角色
	
	public ServiceBean() {
		super();
	}
	public ServiceBean(String json) {
		this(JSONObject.fromObject(json));
	}
	public ServiceBean(JSONObject jsonObject) {
		Class clazz = this.getClass();
		Field[] fields = clazz.getDeclaredFields();
		for(Field field : fields) {
			String fieldName = field.getName();
			try {
				Method setMethod = (Method)clazz.getMethod("set" + UtilTool.changeFirstCharToCapital(fieldName), String.class);
				Object value = jsonObject.get(fieldName);
				if(value == null) value = new String();
				setMethod.invoke(this, new Object[]{value.toString()});
			}catch(Exception e) {
				e.printStackTrace();
			}
		}		
	}
	public String getId() {
		return id;
	}
	public void setServiceId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDependencies() {
		return dependencies;
	}
	public void setDependencies(String dependencies) {
		this.dependencies = dependencies;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getInstalled() {
		return installed;
	}
	public void setInstalled(String installed) {
		this.installed = installed;
	}
	public String getHealth() {
		return health;
	}
	public void setHealth(String health) {
		this.health = health;
	}
	public String getConfigStatus() {
		return configStatus;
	}
	public void setConfigStatus(String configStatus) {
		this.configStatus = configStatus;
	}
	public List<HealthCheckBean> getHealthChecks() {
		return healthChecks;
	}
	public void setHealthChecks(String json) {
		this.healthChecks = new ArrayList<HealthCheckBean>();
		if(json == null || json.equals("")) return;
		JSONArray array = JSONArray.fromObject(json);
		for(int i = 0; i < array.size(); i++) {
			this.healthChecks.add(new HealthCheckBean(array.getJSONObject(i)));
		}
	}
	public String getEnableKerberos() {
		return enableKerberos;
	}
	public void setEnableKerberos(String enableKerberos) {
		this.enableKerberos = enableKerberos;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getClusterId() {
		return clusterId;
	}
	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}
	public String getClusterName() {
		return clusterName;
	}
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}
	public List<RoleBean> getRoles() {
		return roles;
	}
	public void setRoles(String json) {
		this.roles = new ArrayList<RoleBean>();
		if(json == null || json.equals("")) return;
		JSONArray array = JSONArray.fromObject(json);
		for(int i = 0; i < array.size(); i++) {
			this.roles.add(new RoleBean(array.getJSONObject(i)));
		}
	}
	public void addRole(RoleBean role) {
		this.roles.add(role);
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		Class clazz = this.getClass();
		Field[] fields = clazz.getDeclaredFields();
		for(Field field : fields) {
			String fieldName = field.getName();
			try {
				Method getMethod = (Method)clazz.getMethod("get" + UtilTool.changeFirstCharToCapital(fieldName));
				Object value = getMethod.invoke(this);
				if(value == null || value.equals("")) continue;
				if(value.getClass().equals(ArrayList.class)){
					buffer.append(fieldName).append(" : ").append("\n");
					List list = (ArrayList)value;
					for(Object obj : list) {
						String[] values = obj.toString().split("\n");
						for(String val : values) {
							buffer.append("\t").append(val).append("\n");
						}
						buffer.append("\n");
					}
				}else {
					buffer.append(fieldName).append(" : ").append(value).append("\n");
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		return buffer.toString();
	}
	
}
