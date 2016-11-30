package io.transwarp.thread;

import io.transwarp.util.CommonParam;
import io.transwarp.util.CommonUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.util.ReflectionUtils;
import org.apache.log4j.Logger;



public class PutDataRunnable implements Runnable{

	private File file = null;
	private static Logger logger = Logger.getLogger(PutDataRunnable.class);
	
	public PutDataRunnable(File file) {
		this.file = file;
	}
	
	@Override
	public void run() {
		try {
			FileInputStream inputStream = new FileInputStream(file);
			FileSystem fileSystem = FileSystem.get(CommonParam.config);
			OutputStream outputStream = null;
			
			CompressionCodec codec = null;
			String suffix = "";
			//选择压缩格式
			if(CommonParam.prop.getProperty("isCompress").equals("true")) {
				String compress = CommonParam.prop.getProperty("codec.name").toLowerCase().trim();
				String className = null;
				if(compress.equals("snappy")) {
					className = CommonParam.SNAPPY;
				}else if(compress.equals("gzip")) {
					className = CommonParam.GZIP;
				}else if(compress.equals("deflate")) {
					className = CommonParam.DEFLATE;
				}else if(compress.equals("lz4")) {
					className = CommonParam.LZ4;
				}else if(compress.equals("bzip2")) {
					className = CommonParam.BZIP2;
				}else {
					className = CommonParam.SNAPPY;
				}
				try {
					codec = (CompressionCodec) ReflectionUtils.newInstance(Class.forName(className), CommonParam.config);
				}catch(ClassNotFoundException classNotFound) {
					logger.error(classNotFound.getMessage());
				}
				suffix = codec.getDefaultExtension();
				
			}
			
			Path fpath = new Path(CommonUtil.getDistFileName(this.file) + suffix);
			FSDataOutputStream fsOutput = fileSystem.create(fpath, true, 1024 * 1024);
			
			if(CommonParam.prop.getProperty("isCompress").equals("true")) {
				outputStream = codec.createOutputStream(fsOutput);
			}else {
				outputStream = fsOutput;
			}
			
			byte[] buffer = new byte[1024 * 1024];
			for(int readLine = inputStream.read(buffer); readLine >= 0; readLine = inputStream.read(buffer)) {
				outputStream.write(buffer);
			}
			outputStream.flush();
			outputStream.close();
			
			inputStream.close();
			fileSystem.close();
			CommonParam.successTasks.incrementAndGet();
		}catch(Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			synchronized(CommonParam.errLock) {
				
				failedFileWrite(file);
			}
		}
	}
	
	private void failedFileWrite(File file) {
		BufferedOutputStream bufferedOutput = null;
		try {
			bufferedOutput = new BufferedOutputStream(new FileOutputStream(CommonParam.prop.getProperty("failedLog"), true));
			bufferedOutput.write((file.getAbsolutePath() + "\n").getBytes(CommonParam.ENCODING));
		} catch(Exception e) {
			logger.error(e.getMessage());
		} finally {
			try {
				bufferedOutput.close();
			} catch(Exception e) {
				logger.error(e.getMessage());
			}
		}
	}
}
