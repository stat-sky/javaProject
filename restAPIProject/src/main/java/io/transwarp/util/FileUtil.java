package io.transwarp.util;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class FileUtil {

	public static void writeToFile(String value, String path) {
		try {
			FileWriter out = new FileWriter(path);
			out.write(value);
			out.flush();
			out.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void writeToFile(List<String> values, String path) {
		try {
			FileWriter out = new FileWriter(path);
			for(String value : values) {
				out.write(value);
			}
			out.flush();
			out.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void writeToFile(InputStream inputStream, String path) {
		FileOutputStream output = null;
		try {
			output = new FileOutputStream(path);
			byte[] buffer = new byte[1024];
			while((inputStream.read(buffer)) != -1) {
				output.write(buffer);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			if(output != null) {
				try {
					output.flush();
					output.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
}
