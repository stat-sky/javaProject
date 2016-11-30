package io.transwarp.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dom4j.Element;

public class HttpMethod {

	private static Logger logger = Logger.getLogger(HttpMethod.class);
	private static HttpMethod method = null;
	private CloseableHttpClient httpClient = HttpClients.createDefault();
	private CloseableHttpResponse response = null;
	//登录信息
	private static String manager = null;
	private static String username = null;
	private static String password = null;
	
	private HttpMethod() {
		super();
	}
	
	public static HttpMethod getMethod() {
		String manager = CommonString.prop_env.getProperty("manager");
		String username = CommonString.prop_env.getProperty("username");
		String password = CommonString.prop_env.getProperty("password");
		if(manager == null || username == null || password == null) {
			System.err.println("There are not parameter in the file env.properties, exit system");
			System.exit(1);
		}
		return HttpMethod.getMethod(manager, username, password);
	}
	
	public static HttpMethod getMethod(String manager, String username, String password) {
		if(method == null || !HttpMethod.manager.equals(manager) || !HttpMethod.username.equals(username) || !HttpMethod.password.equals(password)) {
			HttpMethod.manager = manager;
			HttpMethod.username = username;
			HttpMethod.password = password;			
			method = new HttpMethod();
			boolean ok = method.login(username, password);
			if(ok) {
				return method;
			}
			else {
				System.err.println("login error");
				return null;
			}
		}else {
			return method;
		}
	}
	
	/**
	 * 执行rest api获取信息
	 * @param url
	 * @param httpMethod
	 * @return
	 */
	public String execute(String url, String httpMethod) {
		return this.execute(url, httpMethod, null);
	}
	/**
	 * 执行rest api获取信息
	 * @param url
	 * @param httpMethod
	 * @param paramJson
	 * @return
	 */
	public String execute(String url, String httpMethod, String paramJson) {
		HttpEntity entity = null;
		if(url.indexOf("http") == -1) {
			url = HttpMethod.manager + "/api" + url;
		}
		logger.info("execute url is : " + url + "    http method is : " + httpMethod);
		switch(httpMethod) {
		case "get" : entity = this.getHttpMethod(url); break;
		case "put" : entity = this.putHttpMethod(url, paramJson); break;
		case "post" : entity = this.postHttpMethod(url, paramJson); break;
		case "delete" : entity = this.deleteHttpMethod(url, paramJson); break;
		default : ;break;
		}
		if(entity == null) {
			logger.error("result is null");
		}else {
			String result;
			try {
				result = EntityUtils.toString(entity);
				this.close();
				return result;
			} catch (ParseException | IOException e) {
				// TODO Auto-generated catch block
				logger.error(e.getCause());
//				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * 获取配置
	 * @param url
	 * @return
	 */
	public Map<String, byte[]> getConfig(String url, String serviceType) {
		url = HttpMethod.manager + "/api" + url;
		HttpEntity entity = this.getHttpMethod(url);
		logger.info("execute url is : " + url);
		try {
			InputStream inputStream = entity.getContent();
			Map<String, byte[]> answer =  CommonUtil.readInputStreamOfTarGz(inputStream, serviceType);
			inputStream.close();
			this.close();
			return answer;
		} catch (Exception e) {
			logger.error("get config error : " + e.getMessage());
//			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 下载用户的keytab
	 * @param url
	 */
	public void downloadKeytab(String fileName) {
		try {	
			//获取配置
			Element config = CommonString.config_restAPI.getElementByChild("purpose", CommonString.DOWNLOAD_KEYTAB);
			//构建url
			Map<String, Object> urlParam = new HashMap<String, Object>();
			urlParam.put("userType", "KRB5LDAP");
			urlParam.put("username", this.username);
			urlParam.put("filename", fileName);
			String url = CommonUtil.buildURL(config.elementText("url"), urlParam);
			url = HttpMethod.manager + "/api" + url;
			logger.info("url is : " + url);
			HttpEntity entity = this.getHttpMethod(url);
			logger.info("execute url is : " + url);
			InputStream input = entity.getContent();
			FileUtil.writeToFile(input, CommonString.prop_env.getProperty("bufferPath") + fileName + ".keytab");
		} catch(Exception e) {
			logger.error("download keytab error : " + e.getMessage());
		}
	}
	
	private HttpEntity getHttpMethod(String url) {
		HttpGet getRequest = new HttpGet(url);
		try {
			response = httpClient.execute(getRequest);
			return response.getEntity();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	private HttpEntity putHttpMethod(String url, String json) {
		HttpPut putRequest = new HttpPut(url);
		try {
			if(json != null) {
				StringEntity stringEntity = new StringEntity(json);
				putRequest.setEntity(stringEntity);
			}			
			response = httpClient.execute(putRequest);
			return response.getEntity();
		}catch(IOException e) {
			logger.error(e.getMessage());
		}
		return null;
	}
	
	private HttpEntity postHttpMethod(String url, String json) {
		HttpPost postRequest = new HttpPost(url);
		try {
			if(json != null) {
				StringEntity stringEntity = new StringEntity(json);
				postRequest.setEntity(stringEntity);
			}
			response = httpClient.execute(postRequest);
			return response.getEntity();
		}catch(IOException e) {
			logger.error(e.getMessage());
		}
		return null;
	}
	
	private HttpEntity deleteHttpMethod(String url, String json) {
		HttpDeleteWithBody deleteRequest = new HttpDeleteWithBody(url);
		try {
			if(json != null) {
				StringEntity stringEntity = new StringEntity(json);
				deleteRequest.setEntity(stringEntity);
			}
			response = httpClient.execute(deleteRequest);
			return response.getEntity();
		}catch(IOException e) {
			logger.error(e.getMessage());
		}
		return null;
	}
	
	private boolean login(String username, String password) {
		try {
			Element loginConfig = CommonString.config_restAPI.getElementByChild("purpose", CommonString.USER_LOGIN);
			//build parameter
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("userName", username);
			param.put("userPassword", password);
			String paramJson = CommonUtil.changeMapToJson(param);
			String jsonString = this.execute(loginConfig.elementText("url"), loginConfig.elementText("http-method"), paramJson);
			Map<String, Object> answer = CommonUtil.changeJsonToMap(jsonString);
			for(Iterator<String> keys = answer.keySet().iterator(); keys.hasNext(); ) {
				String key = keys.next();
				if(key.equals("messageKey")) {
					logger.error(jsonString);
					throw new RuntimeException(answer.get("message").toString());
				}
			}
			return true;
		}catch(Exception e) {
			logger.error(e.getMessage());
		}
		return false;
	}
	
	private void close() {
		try {
			this.response.close();
		} catch (IOException e) {
			logger.error(e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public boolean closeMethod() {
		try {
			Element logoutConfig = CommonString.config_restAPI.getElementByChild("purpose", CommonString.USER_LOGOUT);
			String json = this.execute(logoutConfig.elementText("url"), logoutConfig.elementText("http-method"));
			if(json.equals("success")) {
				return true;
			}
		}catch(Exception e) {
			logger.error(e.getMessage());
		}
		return false;
	}
	
	private class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
		public static final String METHOD_NAME = "DELETE";
		public String getMethod() {
			return METHOD_NAME;
		}
		@SuppressWarnings("unused")
		public HttpDeleteWithBody(final String uri) {
			super();
			setURI(URI.create(uri));
		}
		@SuppressWarnings("unused")
		public HttpDeleteWithBody(final URI uri) {
			super();
			setURI(uri);
		}
		@SuppressWarnings("unused")
		public HttpDeleteWithBody() {
			super();
		}
		
	}
}
