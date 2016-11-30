package io.transwarp.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

public class CommonParam {

	//压缩格式的className
	public static final String SNAPPY = "org.apache.hadoop.io.compress.SnappyCodec";
	public static final String GZIP = "org.apache.hadoop.io.compress.GzipCodec";
	public static final String DEFLATE = "org.apache.hadoop.io.compress.DeflateCodec";
	public static final String LZ4 = "org.apache.hadoop.io.compress.Lz4Codec";
	public static final String BZIP2 = "org.apache.hadoop.io.compress.BZip2Codec"; 
	public static final String CONFIGPATH = "./config/env.properties";
	public static final String ENCODING = "UTF-8";
	
	//错误日志写锁
	public static byte[] errLock = new byte[0];
	//建立文件系统配置
	public static Configuration config = new Configuration();
	//环境配置
	public static Properties prop = new Properties();
	
	//统计任务数
	public static AtomicInteger faildedTasks = new AtomicInteger(0);
	public static AtomicInteger successTasks = new AtomicInteger(0);
	public static int totalTasks = 0;
	
	static {
		try {
			prop.load(new FileInputStream(new File(CommonParam.CONFIGPATH)));
			String[] resources = prop.getProperty("hdfsConfigPath").split(";");
			for(String resource : resources) {
				File file = new File(resource);
				if(!file.exists()) {
					System.out.println(file);
				}else {
					System.out.println(file.getAbsolutePath());
				}
				config.addResource(new Path(resource));
			}
			config.reloadConfiguration();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
