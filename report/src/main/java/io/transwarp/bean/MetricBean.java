package io.transwarp.bean;

import net.sf.json.JSONObject;

public class MetricBean {

	private String metricName;
	private MetricValue metricValue;
	private String unit;
	private String timestamp;
	
	public MetricBean(String json) {
		this(JSONObject.fromObject(json));
	}
	public MetricBean(JSONObject json) {
		this.setMetricName(json.getString("metricName"));
		this.setMetricValue(new MetricValue(json.getString("metricValue")));
		this.setUnit(json.getString("unit"));
		this.setTimestamp(json.getString("timestamp"));
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("metricName : ").append(this.metricName).append("\n");
		buffer.append("metricValue : ").append("\n").append(this.metricValue.toString()).append("\n");
		buffer.append("unit : ").append(this.unit).append("\n");
		buffer.append("timestamp : ").append(this.timestamp).append("\n");
		return buffer.toString();
	}
	
	
	public String getMetricName() {
		return metricName;
	}
	public void setMetricName(String metricName) {
		this.metricName = metricName;
	}
	public MetricValue getMetricValue() {
		return metricValue;
	}
	public void setMetricValue(MetricValue metricValue) {
		this.metricValue = metricValue;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	class MetricValue {

		private String value;
		private String type;
		public MetricValue(String json) {
			this(JSONObject.fromObject(json));
		}
		public MetricValue(JSONObject json) {
			this.setValue(json.getString("value"));
			this.setType(json.getString("type"));
		}
		
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}		
		
		@Override
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append("\t").append("value : ").append(this.value).append("\n");
			buffer.append("\t").append("type : ").append(this.type).append("\n");
			return buffer.toString();
		}
	}
}
