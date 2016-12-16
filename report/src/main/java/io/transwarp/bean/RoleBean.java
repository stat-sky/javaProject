package io.transwarp.bean;

import io.transwarp.util.UtilTool;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class RoleBean {
	
	private String id;				//角色编号
	private String name;			//角色名称
	private String roleType;		//角色类型
	private String status;			//角色状态
	private String health;			//健康状态
	private List<HealthCheckBean> healthChecks;		//健康检测
	private String node;			//所在节点的hostname
	private ServiceBean service;	//所属服务
	
	public RoleBean() {
		super();
	}
	public RoleBean(String json) {
		this(JSONObject.fromObject(json));
	}
	public RoleBean(JSONObject json) {
		Class clazz = this.getClass();
		Field[] fields = clazz.getDeclaredFields();
		for(Field field : fields) {
			String fieldName = field.getName();
			try {
				Method setMethod = (Method)clazz.getMethod("set" + UtilTool.changeFirstCharToCapital(fieldName), String.class);
				Object value = json.get(fieldName);
//				if(fieldName.equals("healthCheck") ) System.out.println("fielName : " + fieldName + " value : " + value);
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
	public String getRoleType() {
		return roleType;
	}
	public void setRoleType(String roleType) {
		this.roleType = roleType;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getHealth() {
		return health;
	}
	public void setHealth(String health) {
		this.health = health;
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
	public String getHostName() {
		return node;
	}
	public void setNode(String jsonObject) {
		NodeBean node = new NodeBean(jsonObject);
		this.node = node.getHostName();
	}
	public void setNode(NodeBean node) {
		this.node = node.getHostName();
	}
	public ServiceBean getService() {
		return service;
	}
	public void setService(String json) {
		this.service = new ServiceBean(json);
	}
	public void setService(ServiceBean service) {
		this.service = service;
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
//				System.out.println("fieldName : " + fieldName + " ; type : " + value.getClass());
				if(value == null || value.equals("")) continue;
				if(value.getClass().equals(ArrayList.class)) {
					buffer.append(fieldName).append(" : ").append("\n");
					List list = (ArrayList)value;
					for(Object obj : list) {
						String[] values = obj.toString().split("\n");
						for(String va : values) {
							buffer.append("\t").append(va).append("\n");
						}
						buffer.append("\n");
					}

				}else if(value.getClass().equals(NodeBean.class)) {
					String[] tmp = value.toString().split("\n");
					buffer.append(fieldName).append(" : \n");
					for(String t : tmp) {
						buffer.append("\t").append(t).append("\n");
					}
					buffer.append("\n");
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
