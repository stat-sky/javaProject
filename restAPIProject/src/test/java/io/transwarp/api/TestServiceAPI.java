package io.transwarp.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.transwarp.bean.RoleBean;
import io.transwarp.bean.ServiceBean;
import io.transwarp.util.FileUtil;

import org.junit.Test;

public class TestServiceAPI {

	@Test
	public void test() {
		ServiceAPI serviceAPI = new ServiceAPI();
		Map<String, ServiceBean> services = serviceAPI.getAllServices("idname");
		for(Iterator<String> keys = services.keySet().iterator(); keys.hasNext(); ) {
			String key = keys.next();
			ServiceBean service = services.get(key);
			System.out.println(service);
		}
	}
	
	
	
	
}
