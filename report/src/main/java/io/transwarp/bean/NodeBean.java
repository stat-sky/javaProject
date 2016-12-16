package io.transwarp.bean;

import io.transwarp.util.UtilTool;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class NodeBean {
	
	private String id;						//节点ID
	private String hostName;				//节点hostname
	private String ipAddress;				//节点IP
	private String clusterId;				//所属集群编号
	private String clusterName;				//所属集群名称
	private String sshConfigId;				//ssh配置编号
	private String rackId;					//所属机柜编号
	private String rackName;				//所属机柜名称
	private String isManaged;				//是否为manager节点
	private String expectedConfigVersion;	//最近一次配置修改的时间戳
	private String lastHeartbeat;			//最近一次心跳的时间戳
	private String numCores;				//core的数量
	private String totalPhysMemBytes;		//总的物理空间大小
	private String mounts;					//硬盘挂载点
	private String status;					//节点状态
	private String cpu;						//cpu信息
	private String disk;					//磁盘信息
	private String osType;					//操作系统
	private String serverKey;				//机器码
	private List<RoleBean> roles;				//包含的服务角色

	public NodeBean() {
		super();
	}
	public NodeBean(String jsonObject) {
		this(JSONObject.fromObject(jsonObject));
	}
	public NodeBean(JSONObject json) {
		Class clazz = this.getClass();
		Field[] fields = clazz.getDeclaredFields();
		for(Field field : fields) {
			String fieldName = field.getName();
			try {
				Method setMethod = (Method)clazz.getMethod("set" + UtilTool.changeFirstCharToCapital(fieldName), String.class);
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
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
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
	public String getSshConfigId() {
		return sshConfigId;
	}
	public void setSshConfigId(String sshConfigId) {
		this.sshConfigId = sshConfigId;
	}
	public String getRackId() {
		return rackId;
	}
	public void setRackId(String rackId) {
		this.rackId = rackId;
	}
	public String getRackName() {
		return rackName;
	}
	public void setRackName(String rackName) {
		this.rackName = rackName;
	}
	public String getIsManaged() {
		return isManaged;
	}
	public void setIsManaged(String isManaged) {
		this.isManaged = isManaged;
	}
	public String getExpectedConfigVersion() {
		return expectedConfigVersion;
	}
	public void setExpectedConfigVersion(String expectedConfigVersion) {
		this.expectedConfigVersion = expectedConfigVersion;
	}
	public String getLastHeartbeat() {
		return lastHeartbeat;
	}
	public void setLastHeartbeat(String lastHeartbeat) {
		this.lastHeartbeat = lastHeartbeat;
	}
	public String getNumCores() {
		return numCores;
	}
	public void setNumCores(String numCores) {
		this.numCores = numCores;
	}
	public String getTotalPhysMemBytes() {
		return totalPhysMemBytes;
	}
	public void setTotalPhysMemBytes(String totalPhysMemBytes) {
		this.totalPhysMemBytes = totalPhysMemBytes;
	}
	public String getMounts() {
		return mounts;
	}
	public void setMounts(String mounts) {
		this.mounts = mounts;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getCpu() {
		return cpu;
	}
	public void setCpu(String cpu) {
		this.cpu = cpu;
	}
	public String getDisk() {
		return disk;
	}
	public void setDisk(String disk) {
		this.disk = disk;
	}
	public String getOsType() {
		return osType;
	}
	public void setOsType(String osType) {
		this.osType = osType;
	}
	public String getServerKey() {
		return serverKey;
	}
	public void setServerKey(String serverKey) {
		this.serverKey = serverKey;
	}
	public List<RoleBean> getRoles() {
		return roles;
	}
	public void setRoles(String json) {
		this.roles = new ArrayList<RoleBean>();
//		System.out.println(json);
		if(json == null || json.equals("")) return;
		JSONArray array = JSONArray.fromObject(json);
		for(int i = 0; i < array.size(); i++) {
			RoleBean role = new RoleBean(array.getJSONObject(i));
			this.roles.add(role);
		}
	}
	public void addRole(String roleType) {
		if(this.roles == null) this.roles = new ArrayList<RoleBean>();
		RoleBean role = new RoleBean();
		role.setRoleType(roleType);
		roles.add(role);
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
