package io.transwarp.util;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class CommonUtilTest {

	@Test
	public void test_printByTable() {
		Map<String, String> lines = new HashMap<String, String>();
		lines.put("io.compression.codecs", "org.apache.hadoop.io.compress.DefaultCodec\norg.apache.hadoop.io.compress.GzipCodec\norg.apache.hadoop.io.compress.BZip2Codec\norg.apache.hadoop.io.compress.DeflateCodec\norg.apache.hadoop.io.compress.SnappyCodec");
		lines.put("fs.defaultFS", "hdfs://nameservice1");
		System.out.println(CommonUtil.printByTable(lines));
	}
}

/*
io.compression.codecs : org.apache.hadoop.io.compress.DefaultCodec,org.apache.hadoop.io.compress.GzipCodec,org.apache.hadoop.io.compress.BZip2Codec,org.apache.hadoop.io.compress.DeflateCodec,org.apache.hadoop.io.compress.SnappyCodec
fs.defaultFS : hdfs://nameservice1

*/