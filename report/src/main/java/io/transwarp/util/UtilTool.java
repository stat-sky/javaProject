package io.transwarp.util;

import io.transwarp.exception.BuildURLException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public final class UtilTool {

	private static Logger logger = Logger.getLogger(UtilTool.class);
	
	/**
	 * 将字符串首字母改为大写字母
	 * @param oldString 需要进行修改的字符串
	 * @return 修改后的字符串
	 */
	public static String changeFirstCharToCapital(String oldString) {
		byte[] items = oldString.getBytes();
		int ch = items[0];
		if(ch > 'a' && ch < 'z') {
			ch = ch - 'a' + 'A';
			items[0] = (byte)ch;
		}
		return new String(items);
	}
	
	/**
	 * 根据带占位符的url和url参数来构建可用的url字符串
	 * @param original 带占位符的url
	 * @param urlParam url中使用的参数
	 * @return 可用的url
	 * @throws Exception 构建失败
	 */
	public static String buildURL(String original, Map<String, Object> urlParam) throws Exception {
		if(urlParam == null) urlParam = new HashMap<String, Object>();
		String url = null;
		if(original.indexOf("{") == -1) {
			logger.debug("this url has not parameter");
			url = original;
		}else if(original.indexOf("[") == -1) {
			logger.debug("this url has required parameter but not optional parameter");
			url = buildURLWithRequired(original, urlParam);
		}else {
			logger.debug("this url has optional parameter");
			url = buildURLWithOptional(original, urlParam);
		}
		return url;
	}
	//存在且仅存在必选参数的url构建
	private static String buildURLWithRequired(String original, Map<String, Object> urlParam) throws Exception{
		StringBuffer urlBuild = new StringBuffer();
		String[] urlSplits = original.split("\\{");
		int numberOfSplit = urlSplits.length;
		if(numberOfSplit < 1) {
			throw new BuildURLException("原始url切分错误");
		}
		urlBuild.append(urlSplits[0]);
		for(int i = 1; i < numberOfSplit; i++) {
			String[] items = urlSplits[i].split("\\}");
			Object value = urlParam.get(items[0]);
			if(value == null || value.equals("")) {
				throw new BuildURLException("there is not this param : " + items[0]);
			}
			urlBuild.append(value);
			if(items.length == 2) urlBuild.append(items[1]);  
			
		}
		return urlBuild.toString();
	}
	//存在可选参数的url构建
	private static String buildURLWithOptional(String original, Map<String, Object> urlParam) throws Exception {
		StringBuffer urlBuild = new StringBuffer();
		String[] urlSplitByOptionals = original.split("\\[");
		int numberOfSplit = urlSplitByOptionals.length;
		if(numberOfSplit < 1) {
			throw new BuildURLException("原始url切分错误");
		}		
		urlBuild.append(buildURL(urlSplitByOptionals[0], urlParam));
		boolean hasParam = (urlBuild.toString().indexOf("?") == -1) ? false : true;
		for(int i = 0; i < numberOfSplit; i++) {
			urlSplitByOptionals[i] = urlSplitByOptionals[i].substring(1, urlSplitByOptionals[i].length() - 1);
			logger.debug("urlSplitByOptional is : " + urlSplitByOptionals[i]);
			String[] items = urlSplitByOptionals[i].split("\\&");
			for(int j = 0; j < items.length; j++) {
				try {
					String urlSplit = buildURLWithRequired(items[j], urlParam);
					logger.debug("read : " + items[j] + "  " + urlSplit);
					if(hasParam) {
						urlBuild.append("&").append(urlSplit);
					}else {
						urlBuild.append("?").append(urlSplit);
						hasParam = true;
					}
				}catch(BuildURLException e) {}
			}
		}
		return urlBuild.toString();
	}
	
	/**
	 * 将json字符串转换为map类型返回
	 * @param jsonString 要进行转换的json字符串
	 * @return 转换后返回的map类型参数
	 * @throws Exception
	 */
	public static Map<String, Object> changeJsonToMap(String jsonString) throws Exception {
		Map<String, Object> answer = new HashMap<String, Object>();
		JSONObject jsonObject = JSONObject.fromObject(jsonString);
		
		for(Iterator<String> keys = jsonObject.keys(); keys.hasNext(); ) {
			String key = keys.next();
			Object value = jsonObject.get(key);
			if(value.getClass().equals(JSONObject.class)) {
				String json = value.toString();
				answer.put(key, changeJsonToMap(json));
			}else if(value.getClass().equals(JSONArray.class)) {
				List<Object> list = new ArrayList<Object>();
				JSONArray array = JSONArray.fromObject(value.toString());
				int length = array.size();
				for(int i = 0; i < length; i++) {
					Object item = array.get(i);
					if(item.getClass().equals(JSONObject.class)) {
						String json = item.toString();
						list.add(changeJsonToMap(json));
					}else {
						list.add(item);
					}
				}
				answer.put(key, list);
			}else {
				answer.put(key, value);
			}
		}
		return answer;
	}
	
	/**
	 * 将map类型参数转换成json字符串
	 * @param param 要进行转换的map类型参数
	 * @return 转换后的json字符串
	 */
	public static String changeMapToString(Map<String, Object> param) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.putAll(param);
		return jsonObject.toString();
	}
	
	/**
	 * 读取tar.gz格式的压缩文件，结果以map返回，其中key为文件名，value为文件内容的byte数组
	 * @param inputStream tar.gz的inputStream输入
	 * @param serviceType tar.gz文件所属服务类型
	 * @return map存储的压缩文件内容
	 * @throws Exception 解析失败
	 */
	public static Map<String, byte[]> readInputStreamOfTarGz(InputStream inputStream, String serviceType) throws Exception{
		Map<String, byte[]> answer = new HashMap<String, byte[]>();
		GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
		//中间以tar文件输出
		StringBuffer outputPath = new StringBuffer(Constant.goalPath);
		outputPath.append(serviceType).append("-").append("config.tar");
		File outputFile = new File(outputPath.toString());
		FileOutputStream outputStream = new FileOutputStream(outputFile);
		IOUtils.copy(gzipInputStream, outputStream);
		gzipInputStream.close();
		outputStream.close();
		
		//读取输出的tar文件
		InputStream input = new FileInputStream(outputFile);
		TarArchiveInputStream tarInputStream = (TarArchiveInputStream)new ArchiveStreamFactory().createArchiveInputStream("tar", input);
		TarArchiveEntry entry = null;
		while((entry = (TarArchiveEntry)tarInputStream.getNextEntry()) != null) {
			if(!entry.isDirectory()) {
				byte[] buffer = new byte[(int)entry.getSize()];
				tarInputStream.read(buffer);
				answer.put(entry.getName(), buffer);
			}
		}
		input.close();
		return answer;
	}
	
	/**
	 * 将inputStream类型数据转换成String类型返回
	 * @param inputStream 要进行转换的inputStream类型数据
	 * @return 转换后的String类型数据
	 * @throws Exception
	 */
	public static String changeInputStreamToString(InputStream inputStream) throws Exception{
		StringBuffer answer = new StringBuffer();
		byte[] buffer = new byte[1024];
		int len = -1;
		while((len = inputStream.read(buffer)) != -1) {
			String value = new String(buffer, 0, len);
			answer.append(value);
		}
		inputStream.close();
		return answer.toString();
	}
	
	/**
	 * 获取文件名称（包含有文件的扩展名）
	 * @param filePath 文件路径
	 * @return 包含扩展名的文件名称
	 */
	public static String getFileName(String filePath) {
		String[] items = filePath.split("/");
		if(items.length < 1) return null;
		return items[items.length - 1];
	}
	
	/**
	 * 获取文件所在目录路径
	 * @param filePath 文件路径
	 * @return 文件所在目录路径
	 */
	public static String getDirectory(String filePath) {
		int point = filePath.lastIndexOf("/");
		if(point == -1) {
			point = 0;
		}
		String dname = filePath.substring(0, point);
		return dname + "/";
	}
	
	/**
	 * 检测时间是否在给出的时间范围内
	 * @param checkTime 要进行检测的时间字符串
	 * @param nowTime 当前时间字符串
	 * @param rangeHouse 时间范围（多少小时内）
	 * @return 是否在改范围内，在则返回true，不在返回false
	 * @throws Exception 时间格式转换错误
	 */
	public static boolean checkDateTime(String checkTime, String nowTime, int rangeHouse) throws Exception{
		long checkTime1 = Constant.dateFormat.parse(checkTime).getTime();
		long nowTime1 = Constant.dateFormat.parse(nowTime).getTime();
		double result = (nowTime1 - checkTime1) * 1.0 / 1000 / 60 / 60 - rangeHouse;
		if(result > 0) return false;
		else return true;
	}
	
	/**
	 * 使用给定的字符将原始字符串填充至指定长度
	 * @param oldString 需要进行填充的原始字符串
//	 * @param goalLength 需要填充至的长度
	 * @param ch 用来填充的字符
	 * @return 填充后的字符串
	 */
	public static String paddingString(String oldString, int goalLength, char ch) {
		int length = goalLength - oldString.length();
		if(length <= 0) return oldString;
		StringBuffer newString = new StringBuffer(oldString);
		for(int i = 0; i < length; i++) {
			newString.append(ch);
		}
		return newString.toString();
	}
	
	/**
	 * 将list改为数组
	 * @param list
	 * @return
	 */
	public static String[] changeListToStrings(List<String> list) {
		int length = list.size();
		String[] items = new String[length];
		for(int i = 0; i < length; i++) {
			items[i] = list.get(i);
		}
		return items;
	}
	
	/**
	 * 在oldString字符串的每行前添加value字符串
	 * @param oldString 要进行处理的字符串
	 * @param value 添加在每行开头的字符串
	 * @return 处理结果
	 */
	public static String retract(String oldString, String value) {
		StringBuffer result = new StringBuffer();
		String[] lines = oldString.split("\n");
		for(String line : lines) {
			result.append(value).append(line).append("\n");
		}
		return result.toString();
	}
}
