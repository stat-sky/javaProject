若报错: no filesystem for scheme hdfs
则在core-site.xml中添加如下配置：
<property>
	<name>fs.hdfs.impl</name>
	<value>org.apache.hadoop.hdfs.DistributedFileSystem</value>
</property>