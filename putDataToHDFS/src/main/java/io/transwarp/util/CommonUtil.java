package io.transwarp.util;

import java.io.File;

public class CommonUtil {

	public static String compressFileName(File file) {
		return file.getName();
	}
	
	public static String getDistFileName(File file) {
		return CommonParam.prop.getProperty("data.dst") + "/" + compressFileName(file);
	}
	
}
