package io.transwarp.util;

import io.transwarp.exception.ExecuteRestAPIException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

public class HttpMethodTool {

	private static Logger logger = Logger.getLogger(HttpMethodTool.class);
	private static HttpMethodTool method = null;
	//登录信息
	private static String manager = null;
	private static String username = null;
	private static String password = null;
	
	private CloseableHttpClient httpClient = HttpClients.createDefault();
	private CloseableHttpResponse response = null;
	
	private HttpMethodTool() {
		super();
	}
	
	/**
	 * 从配置获取信息登录
	 * @return 登录后的rest api的执行函数
	 * @throws Exception
	 */
	public static HttpMethodTool getMethod() throws Exception{
		String manager = Constant.prop_env.getProperty("manager");
		String username = Constant.prop_env.getProperty("username");
		String password = Constant.prop_env.getProperty("password");
		if(manager == null || username == null || password == null) {
			logger.error("there are need manager, username, password at file env.properties");
			System.exit(1);
		}
		return HttpMethodTool.getMethod(manager, username, password);
	}
	
	/**
	 * 根据给定的用户名、密码、manager节点连接串获取rest api的执行函数
	 * @param manager1 连接的manager节点连接串
	 * @param username1 用户名
	 * @param password1 密码
	 * @return rest api的执行函数
	 * @throws Exception
	 */
	public static HttpMethodTool getMethod(String manager1, String username1, String password1) throws Exception{
		if(method == null || !manager.equals(manager1) || !username.equals(username1) || !password.equals(password1)) {
			manager = manager1;
			username = username1;
			password = password1;
			method = new HttpMethodTool();
			try {
				boolean ok = method.login(username, password);
				if(!ok) {
					throw new ExecuteRestAPIException("login failer");
				}
			}catch(ExecuteRestAPIException e) {
				logger.error(e.getMessage());
				System.exit(1);				
			}

		}
		return method;
	}
	/**
	 * 关闭该rest api的执行函数
	 * @return 成功关闭返回true，否则返回false
	 */
	public boolean close() {
		try {
			boolean userLogout = logout();
			this.method = null;
			this.httpClient = null;
			return userLogout;
		}catch(Exception e) {
			logger.error("error at close method");
		}
		return false;
	}
	
	/**
	 * 执行指定的rest api获取结果的json字符串
	 * @param url 执行的rest api的url连接串
	 * @param httpMethod 执行的http方法
	 * @param paramJson 执行使用参数的json字符串
	 * @return 执行结果的json字符串
	 */
	public String execute(String url, String httpMethod, String paramJson) {
		String result = null;
		HttpEntity entity = null;
		if(url.indexOf("http") == -1) {
			url = manager + "/api" + url;
		}
		logger.info("execute url is : \"" + url + "\", httpMethod is " + httpMethod);
		switch(httpMethod) {
		case "get" : entity = this.getHttpMethod(url); break;
		case "put" : entity = this.putHttpMethod(url, paramJson); break;
		case "post" : entity = this.postHttpMethod(url, paramJson); break;
		case "delete" : entity = this.deleteHttpMethod(url, paramJson); break;
		default : logger.error("execute httpMethod \"" + httpMethod + "\" is error"); break;
		}
		if(entity == null) {
			logger.error("result is null");
		}else {
			try {
				result = EntityUtils.toString(entity);
				response.close();
			} catch(Exception e) {
				logger.error("error at get result of execute : " + e.getMessage());
			}
		}
		return result;
	}
	
	/**
	 * 获取指定服务的配置
	 * @param url 执行rest api的连接串
	 * @param serviceType 要查询配置的服务类型
	 * @return 解析成map的服务配置，其中key为配置文件名，value为配置文件内容的byte[]
	 */
	public Map<String, byte[]> getConfig(String url, String serviceType) {
		Map<String, byte[]> result = new HashMap<String, byte[]>();
		InputStream inputStream = null;
		url = manager + "/api" + url;
		logger.info("execute url is : \"" + url + "\", httpMethod is get");
		HttpEntity entity = getHttpMethod(url);
		try {
			inputStream = entity.getContent();
			result = UtilTool.readInputStreamOfTarGz(inputStream, serviceType);
		}catch(Exception e) {
			logger.error("error at get config of " + serviceType + " : " + e.getMessage());
		}finally {
			try {
				if(inputStream != null) {
					inputStream.close();
				}
				response.close();
			}catch(Exception e) {
				logger.error("error at close response and inputStream :" + e.getMessage());
			}
		}
		return result;
	}
	
	/**
	 * 下载用户的keytab
	 * @param fileName 下载的keytab文件名称
	 * @return
	 */
	public boolean downLoadKeytab(String fileName) {
		boolean downloadSuccess = true;
		try {
			Element config = Constant.prop_restapi.getElement("purpose", Constant.DOWNLOAD_KEYTAB);
			//构建url
			Map<String, Object> urlParam = new HashMap<String, Object>();
			urlParam.put("userType", "KRB5LDAP");
			urlParam.put("username", username);
			urlParam.put("filename", fileName);
			String url = UtilTool.buildURL(config.elementText("url"), urlParam);
			url = manager + "/api" + url;
			HttpEntity entity = this.getHttpMethod(url);
			logger.info("execute url is : \"" + url + "\", httpMethod is get");
			InputStream inputStream = entity.getContent();
			String outputPath = Constant.bufferPath + fileName + ".keytab";
			FileOutputStream outputStream = new FileOutputStream(outputPath);
			IOUtils.copy(inputStream, outputStream);
			inputStream.close();
			outputStream.flush();
			outputStream.close();
		}catch(Exception e) {
			logger.error("error at download keytab : " + e.getMessage());
			downloadSuccess = false;
		}
		return downloadSuccess;
	}
	
	//rest api执行函数进行登录
	private boolean login(String username, String password) throws Exception {
		boolean loginSuccess = true;
		Element loginConfig = Constant.prop_restapi.getElement("purpose", Constant.USER_LOGIN);
		String url = loginConfig.elementText("url");
		//构建参数
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("userName", username);
		param.put("userPassword", password);
		String paramJson = UtilTool.changeMapToString(param);
		String resultOfLogin = execute(url, loginConfig.elementText("http-method"), paramJson);
		Map<String, Object> resultMap = UtilTool.changeJsonToMap(resultOfLogin);
		for(Iterator<String> keys = resultMap.keySet().iterator(); keys.hasNext(); ) {
			String key = keys.next();
			if(key.equals("messageKey")) {
				loginSuccess = false;
				logger.error(resultOfLogin);
				throw new ExecuteRestAPIException(resultMap.get("message").toString());
			}
		}
		return loginSuccess;
	}
	
	//登出
	private boolean logout() throws Exception{
		Element logoutConfig = Constant.prop_restapi.getElement("purpose", Constant.USER_LOGOUT);
		String url = logoutConfig.elementText("url");
		String httpMethod = logoutConfig.elementText("http-method");
		String logoutResult = execute(url, httpMethod, null);
		if(logoutResult.equals("success")) {
			return true;
		}
		return false;
	}
	

	//执行get 的http方法
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
	//执行put的http方法
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
	//执行post的http方法
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
	//执行delete的http方法
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
	//自定义的delete的http方法
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
