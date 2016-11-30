package io.transwarp.util;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.management.RuntimeErrorException;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class ReadXmlConfig {

	/*		root element	 */
	private Element rootElement = null;
	
	public ReadXmlConfig(String filePath) throws Exception {
		this.rootElement = this.getRootElementByPath(filePath);
	}
	
	private Element getRootElementByPath(String filePath) throws Exception {
		File file = new File(filePath);
		Document document = new SAXReader().read(file);
		return document.getRootElement();
	}
	
	/**
	 * find node by key-value of child nodes
	 * @param key child key
	 * @param value child value
	 * @return
	 * @throws Exception
	 */
	public Element getElementByChild(String key, Object value) throws Exception{
		Queue<Element> queue = new LinkedList<Element>();
		queue.offer(this.rootElement);
		while(!queue.isEmpty()) {
			Element element = queue.poll();
			List<Element> children = element.elements();
			for(Element child : children) {
				if(child.getName().equals(key)) {
					if(child.getText().equals(value)) {
						return element;
					}
				}else {
					queue.offer(child);
				}
			}
		}
		throw new RuntimeErrorException(null, "node no found");
	}
	
	public List<Element> getElements(String key) {
		List<Element> answer = new ArrayList<Element>();
		Queue<Element> queue = new LinkedList<Element>();
		queue.offer(this.rootElement);
		while(!queue.isEmpty()) {
			Element element = queue.poll();
			List<Element> children = element.elements();
			for(Element child : children) {
				if(child.getName().equals(key)) {
					answer.add(child);
				}else {
					queue.offer(child);
				}
			}
		}
		return answer;
	}
	
}
