package io.transwarp.report;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.transwarp.bean.ServiceBean;
import io.transwarp.thread.ConfigCallable;
import io.transwarp.util.Constant;
import io.transwarp.util.HttpMethodTool;
import io.transwarp.util.PrintToTableUtil;
import io.transwarp.util.UtilTool;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.log4j.Logger;

public class ServiceConfigReport {

	private static Logger logger = Logger.getLogger(ServiceConfigReport.class);
	private HttpMethodTool method;
	private Map<String, ServiceBean> services;
	
	public ServiceConfigReport(HttpMethodTool method, Map<String, ServiceBean> services) {
		this.method = method;
		this.services = services;
	}
	
	/**
	 * 获取各个节点的平台服务配置信息，以map返回，其中key为节点hostname， value为表格形式的配置信息
	 * @return
	 */
	public Map<String, String> getConfigReprot() throws Exception {
		Map<String, String> answer = new HashMap<String, String>();
		String[] serviceTypes = Constant.prop_report.getProperty("services").split(";");
		for(String serviceType : serviceTypes) {
			for(Iterator<String> keys = this.services.keySet().iterator(); keys.hasNext(); ) {
				String key = keys.next();
				ServiceBean service = this.services.get(key);
				if(service.getType().equals(serviceType)) {
					logger.info("get config of service is : " + service.getName());
					ConfigCallable configCallable = new ConfigCallable(this.method, service.getId(), serviceType);
					Map<String, byte[]> configs = configCallable.call();
					this.analysisConfig(answer, configs, service.getName());					
				}
			}

		}
		return answer;
	}
	
	private void analysisConfig(Map<String, String> answer, Map<String, byte[]> configs, String serviceName) throws Exception {
		for(Iterator<String> keys = configs.keySet().iterator(); keys.hasNext(); ) {
			String key = keys.next();
			if(!key.endsWith(".sh") || !key.endsWith("-env") || !key.endsWith(".xml")) continue;
			//获取文件名称
			String fileName = UtilTool.getFileName(key);
			//获取配置文件所属节点的hostname
			String[] dirs = key.split("/");
			if(dirs.length < 2) {
				logger.error(".sh config file name is error");
				throw new RuntimeException(".sh config file name is error");
			}
			String hostname = dirs[1];
			//获取已解析出的配置信息
			String configValues = answer.get(hostname);
			//设置写入接下来解析内容的buffer
			StringBuffer buffer = null;
			if(configValues == null) {
				buffer = new StringBuffer(serviceName).append(" :\n");
			}else {
				buffer = new StringBuffer(configValues);
			}
			buffer.append("  ").append(fileName).append("\n");
			
			if(key.endsWith(".sh") || key.endsWith("-env")) {
				//用于缓存解析结果
				Map<String, String> analysisValues = new HashMap<String, String>();
				//.sh配置文件内容分析
				byte[] fileValus = configs.get(key);
				String[] lines = new String(fileValus, "utf-8").split("\n");
				for(String line : lines) {
					line = line.trim();
					if(line.startsWith("#") || line.equals("")) continue;  //去除注释和空白行
					//确定变量名称位置
					int beginIndex = line.indexOf(' ', 1);
					int endIndex = line.indexOf('=', 1);
					if(beginIndex > endIndex || beginIndex == -1) {
						beginIndex = 0;
					}
					//截取变量名
					String paramName = line.substring(beginIndex, endIndex);
					//截取变量值
					String paramValue = line.substring(endIndex + 1);
					//若变量值中存在引号则去掉
					if(paramValue.indexOf("\"") != -1) paramValue.replaceAll("\"", "");
					
					//判断是否已存在该变量值，若存在则进行取舍，并将结果放入缓存
					String oldValue = analysisValues.get(paramName);
					if(oldValue != null) {
						//若已存在该值，则取-Xmx和-Xms的值
						if(paramValue.indexOf("-Xms") != -1 || paramValue.indexOf("-Xmx") != -1) analysisValues.put(paramName, paramValue);
						else analysisValues.put(paramName, oldValue);
					}else {
						analysisValues.put(paramName, oldValue);
					}
				}
				
				//从配置中获取需要选择的变量，并从缓存的配置参数中获取，然后组成list作为打印成表格的参数
				List<String[]> maps = new ArrayList<String[]>();
				String params = Constant.prop_report.getProperty(fileName);
				if(params != null) {
					String[] items = params.split(";");
					for(String item : items) {
						String itemValues = analysisValues.get(item);
						if(itemValues == null) continue;
						String[] paramValues = itemValues.replaceAll(",", "\n").replaceAll(" ", "\n").split("\n");
						int length = paramValues.length;
						for(int i = 0; i < length; i++) {
							if(i == 0) maps.add(new String[]{item, paramValues[i]});
							else maps.add(new String[]{null, paramValues[i]});
						}
					}
				}
				buffer.append(PrintToTableUtil.printToTable(maps, 60));
			}else if(key.endsWith(".xml")) {
				Configuration analysisValues = HBaseConfiguration.create();
				byte[] fileValues = configs.get(key);
				InputStream inputStream = new ByteArrayInputStream(fileValues);
				analysisValues.addResource(inputStream);
				
				//从配置中获取需要选择的变量，并从缓存的配置参数中获取，然后组成list作为打印成表格的参数
				List<String[]> maps = new ArrayList<String[]>();
				String params = Constant.prop_report.getProperty(fileName);
				if(params != null) {
					String[] items = params.split(";");
					for(String item : items) {
						String itemValues = analysisValues.get(item);
						if(itemValues == null) continue;
						String[] paramValues = itemValues.replaceAll(",", "\n").replaceAll(" ", "\n").split("\n");
						int length = paramValues.length;
						for(int i = 0; i < length; i++) {
							if(i == 0) maps.add(new String[]{item, paramValues[i]});
							else maps.add(new String[]{null, paramValues[i]});
						}
					}
				}
				buffer.append(PrintToTableUtil.printToTable(maps, 60));
			}
			answer.put(hostname, buffer.toString());
		}
	}
}
