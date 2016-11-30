package io.transwarp.bean;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.sf.json.JSONObject;

public class HealthCheckBean {

	private String type;
	private String result;
	private String lastCheck;
	private String detail;
	public HealthCheckBean() {}
	public HealthCheckBean(String json) {
		this(JSONObject.fromObject(json));
	}
	public HealthCheckBean(JSONObject json) {
		Class clazz = this.getClass();
		Field[] fields = clazz.getDeclaredFields();
		for(Field field : fields) {
			String fieldName = field.getName();
			try {
				PropertyDescriptor pd = new PropertyDescriptor(fieldName, clazz);
				Method setMethod = pd.getWriteMethod();
				Object value = json.get(fieldName);
				if(value == null) value = new String();
				setMethod.invoke(this, new Object[]{value.toString()});
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getLastCheck() {
		return lastCheck;
	}
	public void setLastCheck(String lastCheck) {
		this.lastCheck = lastCheck;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		Class clazz = this.getClass();
		Field[] fields = clazz.getDeclaredFields();
		for(Field field : fields) {
			String fieldName = field.getName();
			try {
				PropertyDescriptor pd = new PropertyDescriptor(fieldName, clazz);
				Method getMethod = pd.getReadMethod();
				Object value = getMethod.invoke(this);
				buffer.append(fieldName).append(" : ").append(value).append("\n");
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		return buffer.toString();
	}
}
