package io.transwarp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.management.RuntimeErrorException;

import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.log4j.Logger;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

public class CommonUtil {
	
	private static Logger logger = Logger.getLogger(CommonUtil.class);
	
	/**
	 * 
	 * @param fieldName
	 * @return
	 */
	public static String changeFirstCharToCapital(String fieldName) {
		byte[] items = fieldName.getBytes();
		int ch = items[0];
		if(ch > 'a' && ch < 'z') {
			ch = ch - 'a' + 'A';
			items[0] = (byte)ch;
		}
		return new String(items);
	}
	
	/**
	 * 通过原始待参数url串和url中使用的参数列表构建可用的url串
	 * @param original
	 * @param urlParameter
	 * @return
	 * @throws Exception
	 */
	public static String buildURL(String original, Map<String, Object> urlParameter) throws Exception{
		if(urlParameter == null) urlParameter = new HashMap<String, Object>();
		StringBuffer url = new StringBuffer();
		if(original.indexOf("{") == -1) {   //不存在参数
			logger.debug("type : without parameter");
			url.append(original);
		}else if(original.indexOf("[") == -1) {   //不存在可选参数
			logger.debug("type : with required parameter");
			url.append(buildURLWithRequired(original, urlParameter));
		}else {    //存在可选参数
			logger.debug("type : with optional parameter");
			url.append(buildURLWithOptional(original, urlParameter));
		}
		return url.toString();
	}
	
	//存在且仅存在必选参数
	private static String buildURLWithRequired(String original, Map<String, Object> urlParameter) throws Exception{
		StringBuffer url = new StringBuffer();
		String[] urlSplits = original.split("\\{");  //对原始的url按照"{"进行分片
		url.append(urlSplits[0]);
		for(int i = 1; i < urlSplits.length; i++) {
			String[] temp = urlSplits[i].split("\\}");
			Object value = urlParameter.get(temp[0]);  //获取参数
			if(value == null || value.equals(""))      //若参数不存在则抛出异常
				throw new RuntimeErrorException(null, "It must be had parameter : " + temp[0]);
			url.append(value);
			if(temp.length > 1) url.append(temp[1]);			
		}
		
		return url.toString();
	}
	
	//存在可选参数
	private static String buildURLWithOptional(String original, Map<String, Object> urlParameter) throws Exception {
		StringBuffer url = new StringBuffer();
		String[] urlSplitByOptional = original.split("\\[");  //对原始的url按照"["进行分片，则第一片为无参或仅有必选参数
		url.append(buildURL(urlSplitByOptional[0], urlParameter));
		boolean hasParam = (url.toString().indexOf("?") == -1) ? false : true;  //判断已连接的url中是否存在"?"
		for(int i = 1; i < urlSplitByOptional.length; i++) {
			urlSplitByOptional[i] = urlSplitByOptional[i].substring(1, urlSplitByOptional[i].length() - 1);
			logger.debug("urlSplitByOptional : " + urlSplitByOptional[i]);
			String[] items = urlSplitByOptional[i].split("\\&");
			for(int j = 0; j < items.length; j++) {
				try {
					String urlSplit = buildURLWithRequired(items[j], urlParameter);
					logger.debug("read : " + items[j] + "    " + urlSplit);
					if(hasParam) {
						url.append("&").append(urlSplit);
					}else {
						hasParam = true;
						url.append("?").append(urlSplit);
					}					
				}catch(RuntimeErrorException e) {
					
				}
					
			}
		}
		return url.toString();
	}
	
	
	/**
	 * change json string to map<String, Object>
	 * @param jsonString
	 * @return
	 * @throws JSONException
	 */
	public static Map<String, Object> changeJsonToMap(String jsonString) throws JSONException{
		Map<String, Object> answer = new HashMap<String, Object>();
		JSONObject jsonObject = JSONObject.fromObject(jsonString);
		@SuppressWarnings("unchecked")
		Iterator<String> keys = jsonObject.keys();
		while(keys.hasNext()) {
			String key = keys.next();
			Object value = jsonObject.get(key);
			//judge the type of value
			if(value.getClass().equals(JSONObject.class)) {
				String json = value.toString();
				answer.put(key, changeJsonToMap(json));
			}else if(value.getClass().equals(JSONArray.class)) {
				List<Object> list = new ArrayList<Object>();
				JSONArray array = (JSONArray)value;
				int length = array.size();
				for(int i = 0; i < length; i++) {
					if(array.get(i).getClass().equals(JSONObject.class)) {
						String json = array.get(i).toString();
						list.add(changeJsonToMap(json));
					}else {
						list.add(array.get(i));
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
	 * change Map<String, Object> to json String
	 * @param parameter
	 * @return
	 */
	public static String changeMapToJson(Map<String, Object> parameter) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.putAll(parameter);
		return jsonObject.toString();
	}
	
	/**
	 * read file that tye is .tar.gz by inputStream
	 * @param inputStream
	 * @return
	 * @throws Exception
	 */
	public static Map<String, byte[]> readInputStreamOfTarGz(InputStream inputStream, String serviceType) throws Exception{
		Map<String, byte[]> answer = new HashMap<String, byte[]>();
		GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
		File outputFile = new File(CommonString.prop_env.getProperty("bufferPath") + serviceType + "-" + CommonString.prop_env.getProperty("fileName") + ".tar");
		FileOutputStream outputStream = new FileOutputStream(outputFile);
		IOUtils.copy(gzipInputStream, outputStream);
		gzipInputStream.close();
		outputStream.close();
		//unTar
		InputStream input = new FileInputStream(outputFile);
		TarArchiveInputStream tarInputStream = (TarArchiveInputStream)new ArchiveStreamFactory()
			.createArchiveInputStream("tar", input);
		TarArchiveEntry entry = null;
		while((entry = (TarArchiveEntry)tarInputStream.getNextEntry()) != null) {
			if(entry.isDirectory()) {
//				System.out.println("this is directory");
			}else {
				byte[] buffer = new byte[(int)entry.getSize()];
				tarInputStream.read(buffer);
				answer.put(entry.getName(), buffer);
			}
		}
		return answer;
	}
	
	/**
	 * change inputStream into String
	 * @param inputStream
	 * @return
	 * @throws Exception
	 */
	public static String changeInputStreamToString(InputStream inputStream) throws Exception {
		StringBuffer stringBuffer = new StringBuffer();
		byte[] buffer = new byte[1024];
		int len = -1;
		while((len = inputStream.read(buffer)) != -1) {
			String value = new String(buffer, 0, len);
			stringBuffer.append(value);
		}
		inputStream.close();
		return stringBuffer.toString();

	}
	
	/**
	 * 获取文件名称(不含扩展名)
	 * @param file
	 * @return
	 */
    public static String fileFormat(String file) {
        String formatted;
        String temp=file;
        int prefix=temp.lastIndexOf("/")+1;
        int postfix=temp.lastIndexOf(".");
        if ((prefix==-1)||(postfix==-1)) {
            prefix = 0;
            postfix = 0;
        }
        formatted=temp.substring(prefix,postfix);
        return formatted;
    }
    
    public static String getFileName(String path) {
    	String[] paths = path.split("/");
    	if(paths.length < 1) return null;
    	return paths[paths.length - 1];
    }

    /**
     * 获取文件路径
     * @param path
     * @return
     */
    public static String getDirectory (String path) {
        int point=path.lastIndexOf("/");
        if (point==-1) {
            point=0;
        }
        String dname=path.substring(0,point);
        return dname+"/";
    }
    
    /**
     * 将字符串info用字符ch补齐至长度length
     * @param info
     * @param length
     * @param ch
     * @return
     */
	public static String paddingString(String info, int length, char ch) {
		if(info.length() < length) {
			StringBuffer buffer = new StringBuffer(info);
			length -= info.length();
			for(int i = 0; i < length; i++) {
				buffer.append(ch);
			}
			return buffer.toString();
		}else {
			return info;
		}	
	}
	
	/**
	 * 给string字符串每行开头添加字符串value
	 * @param string
	 * @param value
	 * @return
	 */
	public static String paddingBegin(String string, String value) {
		StringBuffer answer = new StringBuffer();
		String[] lines = string.split("\n");
		for(String line : lines) {
			answer.append(value).append(line).append("\n");
		}
		return answer.toString();
	}
	
	/**
	 * 将信息使用表格形式输出
	 * @param columnName 表格列名
	 * @param lines 表格中一行的值，每列间用，隔开；
	 * @param length 单元格长度
	 * @return
	 */
	public static String printByTable(String[] columnNames, List<String> lines, int length) {
		StringBuffer answer = new StringBuffer();
		//确定列数
		int number = columnNames.length;
		//建立表头
		//边框
		answer.append("+");
		for(int i = 0; i < number; i++) {
			answer.append(CommonUtil.paddingString("", length, '-')).append("+");
		}
		answer.append("\n|");
		for(String columnName : columnNames) {
			answer.append("  ").append(CommonUtil.paddingString(columnName, length - 2, ' ')).append("|");
		}
		answer.append("\n+");
		for(int i = 0; i < number; i++) {
			answer.append(CommonUtil.paddingString("", length, '-')).append("+");
		}
		answer.append("\n");
		
		//表格内容
		for(String line : lines) {
			String[] values = line.replaceAll("\n", " ").split(",");
			answer.append("|");
			for(String value : values) {
				answer.append("  ").append(CommonUtil.paddingString(value, length - 2, ' ')).append("|");
			}
			answer.append("\n");
		}
		answer.append("+");
		for(int i = 0; i < number; i++) {
			answer.append(CommonUtil.paddingString("", length, '-')).append("+");
		}
		answer.append("\n");
		return answer.toString();
	}
	
	public static String printByTable(Map<String, String> values) {
		StringBuffer answer = new StringBuffer();
		
		int firstLen = 50;
		int secondLen = 70;

		answer.append("+").append(CommonUtil.paddingString("", firstLen, '-')).append("+")
			.append(CommonUtil.paddingString("", secondLen, '-')).append("+\n");
		//内容
		for(Iterator<String> keys = values.keySet().iterator(); keys.hasNext(); ) {
			String key = keys.next();
			String[] lines = values.get(key).split("\n");
			boolean first = false;
			for(String line : lines) {
				if(line.equals("$" + key)) continue;
				if(!first) {
					answer.append("|  ").append(CommonUtil.paddingString(key, firstLen - 2, ' ')).append("|");
					first = true;
				}else {
					answer.append("|").append(CommonUtil.paddingString("", firstLen, ' ')).append("|");
				}
				line = "  " + line;
				int row = line.length() / secondLen;
				int begin = 0;
				int end = secondLen;
				for(int i = 0; i <= row; i++) {
						//第一列为空格
					if(i > 0) answer.append("|").append(CommonUtil.paddingString("", firstLen, ' ')).append("|");
					if(i < row) {
						answer.append(line.substring(begin, end)).append("|\n");
						begin = end;
						end += secondLen;
					}else {
						answer.append(CommonUtil.paddingString(line.substring(begin), secondLen, ' ')).append("|\n");
					}
						
				}
			}
			answer.append("+").append(CommonUtil.paddingString("", firstLen, '-')).append("+")
				.append(CommonUtil.paddingString("", secondLen, '-')).append("+\n");
		}
		return answer.toString();
	}
	
	public static boolean checkDateTime(String logTime, String endTime, int rangeHouse) {
		try {
			long end = CommonString.dateFormat.parse(endTime).getTime();
			return checkDateTime(logTime, end, rangeHouse);
		}catch(Exception e) {
			logger.error("change string to dateTime error : " + e.getMessage());
		}
		return false;
	}
	public static boolean checkDateTime(String logTime, long endTime, int rangeHouse) {
		double answer = 0;
		
		try {
			long temp = CommonString.dateFormat.parse(logTime).getTime();
			answer = (endTime - temp) / 1000 / 60 / 60 - rangeHouse;
		}catch(Exception e) {
			logger.error("change string to dateTime error : " + e.getMessage());
		}
		if(answer > 0) {
			return false;
		}else {
			return true;
		}		
	}
	
}
