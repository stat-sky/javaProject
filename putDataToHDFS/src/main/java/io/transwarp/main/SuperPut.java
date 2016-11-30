package io.transwarp.main;

import io.transwarp.thread.PutDataRunnable;
import io.transwarp.util.CommonParam;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

public class SuperPut {

	private LinkedList<File> taskQueue = new LinkedList<File>();
	private static Logger logger = Logger.getLogger(SuperPut.class);
	
	public static void main(String[] args) {
		SuperPut superPut = new SuperPut();
		superPut.run();
	}
	
	public void run() {
		boolean check = this.checkProp();
		if(!check) {
			System.exit(1);
		}
		this.generateTasks();
		//获取线程数
		int threadNum = Integer.valueOf(CommonParam.prop.getProperty("thread.num"));
		ExecutorService threadPool = Executors.newFixedThreadPool(threadNum);
		
		long startTime = System.currentTimeMillis();
		
		//执行线程进行存储
		while(!this.taskQueue.isEmpty()) {
			threadPool.execute(new PutDataRunnable(this.taskQueue.poll()));
		}
		//判断是否完成
		for(boolean ok = false; ok != true; ) {
			if((CommonParam.successTasks.intValue() + CommonParam.faildedTasks.intValue() == CommonParam.totalTasks) || threadPool.isTerminated()) {
				String result = String.format("---- Summary ----\nTotal : %d Success : %d Failed : %d", 
						CommonParam.totalTasks, CommonParam.successTasks.intValue(), CommonParam.faildedTasks.intValue());
				System.out.println(result);
				threadPool.shutdown();
				ok = true;
			}else {
				try {
					Thread.sleep(1000);
				}catch(Exception e) {
					logger.error(e.getMessage());
					threadPool.shutdown();
					ok = true;
				}
			}
		}
		
		long endTime = System.currentTimeMillis();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		System.out.println("Start time is : " + dateFormat.format(startTime));
		System.out.println("end time is   : " + dateFormat.format(endTime));
		System.out.println("Time cost is  : " + (endTime - startTime) / 1000 + "s");
	}
	
	//检查配置
	public boolean checkProp() {
		boolean ok = true;
		//检查数据路径
		String dataDir = CommonParam.prop.getProperty("data.dir");
		if(dataDir == null || dataDir.equals("")) {
			ok = false;
			logger.error("Please set property \"data.dir\" at least. ");
		}
		//检查目标路径
		String dataDst = CommonParam.prop.getProperty("data.dst");
		if(dataDst == null || dataDst.equals("")) {
			ok = false;
			logger.error("Please set property \"data.dst\" at least. ");
		}
		
		//检查其他配置信息，若无则设置默认值
		//是否压缩
		String isCompress = CommonParam.prop.getProperty("isCompress");
		if(isCompress == null || isCompress.equals("")) {
			CommonParam.prop.setProperty("isCompress", "true");
		}
		//压缩格式
		String codecName = CommonParam.prop.getProperty("codec.name");
		if(codecName == null || codecName.equals("")) {
			CommonParam.prop.setProperty("codec.name", "snappy");
		}
		//线程数
		String threadNum = CommonParam.prop.getProperty("thread.num");
		if(threadNum == null || threadNum.equals("")) {
			CommonParam.prop.setProperty("thread.num", "3");
		}
		return ok;
	}
	
	//将文件放入写队列
	public void deepSearchFiles(File file) {
		if(file.isDirectory()) {
			File[] children = file.listFiles();
			for(File child : children) {
				deepSearchFiles(child);
			}
		}else {
			this.taskQueue.add(file);
		}
	}
	
	//构建文件队列
	public void generateTasks() {
		String[] paths = CommonParam.prop.getProperty("data.dir").split(";");
		for(String path : paths) {
			File file = new File(path);
			deepSearchFiles(file);
		}
		Collections.shuffle(taskQueue);
		CommonParam.totalTasks = taskQueue.size();
		logger.info("Found " + CommonParam.totalTasks + " tasks");
	}
}
