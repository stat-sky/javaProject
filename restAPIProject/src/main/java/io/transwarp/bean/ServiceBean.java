package io.transwarp.bean;

import io.transwarp.util.CommonUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ServiceBean {

	private String id;
	private String name;
	private List<String> dependencies;
	private String status;
	private String installed;
	private String health;
	private String configStatus;
	private List<HealthCheckBean> healthChecks;
	private String enableKerberos;
	private String type;
	private String clusterId;
	private String clusterName;
	private List<RoleBean> roles;
	
	public ServiceBean() {};
	public ServiceBean(String json) {
		this(JSONObject.fromObject(json));
	}
	public ServiceBean(JSONObject json) {
//		System.out.println(json);
		Class clazz = this.getClass();
		Field[] fields = clazz.getDeclaredFields();
		for(Field field : fields) {
			String fieldName = field.getName();
			try {
				Method setMethod = (Method)clazz.getMethod("set" + CommonUtil.changeFirstCharToCapital(fieldName), String.class);
				Object value = json.get(fieldName);
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
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getDependencies() {
		return dependencies;
	}
	public void setDependencies(String param) {
		this.dependencies = new ArrayList<String>();
		if(param == null || param.equals("")) return;
		if(param.length() > 2) {
			param = param.substring(1, param.length() - 1);
			String[] serviceIds = param.split("\\,");
			for(int i = 0; i < serviceIds.length; i++) {
				this.dependencies.add(serviceIds[i]);
			}
		}
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
				Method getMethod = (Method)clazz.getMethod("get" + CommonUtil.changeFirstCharToCapital(fieldName));
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
