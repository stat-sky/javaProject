package io.transwarp.util;

import java.util.List;

import org.apache.log4j.Logger;

import io.transwarp.exception.PrintTableException;

public class PrintToTableUtil {

	public static String printToTable(List<String[]> listMap, int centLength) throws Exception {
		int rowCount = listMap.size();
		String[][] maps = new String[rowCount][];
		int columnCount = 0;
		for(int i = 0; i < rowCount; i++) {
			maps[i] = listMap.get(i);
			int length = maps[i].length;
			if(i == 0) {
				columnCount = length;
			}else {
				if(length < columnCount) {
					String[] newMap = new String[length];
					for(int j = 0; j < columnCount; j++) {
						if(j < length) newMap[j] = maps[0][j];
						else newMap[length - 1] += " " + maps[0][j];
					}
					maps[0] = newMap;
					columnCount = length;
				}
			}
		}
		return printToTable(maps, centLength);
	}
	
	public static String printToTable(String[][] maps, int centLength) throws Exception{
		if(maps == null) {
			throw new PrintTableException("maps must not be null");
		}
		int rowCount = maps.length;
		int colCount = maps[0].length;
		//init
		//单元格内容，添加一行为下边框
		StringBuffer[] results = new StringBuffer[rowCount + 1];
		//单元格上边框，与每行单元格对应，不包含下边框
		StringBuffer[] lines = new StringBuffer[rowCount];
		for(int i = 0; i < rowCount + 1; i++) {
			if(i == rowCount) {
				results[i] = new StringBuffer("+");
			}else {
				results[i] = new StringBuffer("| ");
			}
		}
		boolean[][] hasValues = new boolean[rowCount][colCount];
		for(int i = 0; i < rowCount; i++) {
			for(int j = 0; j < colCount; j++) {
				hasValues[i][j] = false;
			}
		}
		for(int j = 0; j < colCount; j++) {
			for(int i = 0; i < rowCount; i++) {
				//已标记的点不需要填写
				if(hasValues[i][j] == true) continue;
				//横向查询合并的单元格
				int colNum = 1;  //横向合并的单元格个数
				for(int x = 1; j + x < colCount && maps[i][j + x] == null && hasValues[i][j + x] == false; x++) {
					colNum = x + 1;
				}
				int centWidth = (centLength + 1) * colNum - 1;  //两个或以上单元格合并时会添加中间的竖线
				//纵向查询合并的单元格
				int rowNum = 1;  //纵向合并单元格个数
				for(int x = 1; i + x < rowCount && maps[i + x][j] == null && hasValues[i + x][j] == false; x++) {
					rowNum = x + 1;
				}
				int centHigh = rowNum;
				
				//标记合并的单元格，默认单元格合并后仍为矩形
				for(int x = 0; x < rowNum; x++) {
					for(int y = 0; y < colNum; y++) {
						hasValues[i + x][j + y] = true;
					}
				}
			
				//填写单元格
				if(i == rowCount - 1 || (centHigh > 1 && i + centHigh == rowCount)) {  //若为最后一行则绘制下边框
					results[rowCount].append(UtilTool.paddingString("", centWidth, '-')).append("+");				
				}
				for(int row = 0; row < centHigh; row++) {
					if(row == 0) {
						results[i + row].append(UtilTool.paddingString(maps[i][j], centWidth - 1, ' ')).append("| ");
						if(lines[i + row] == null) lines[i + row] = new StringBuffer("+");
						lines[i + row].append(UtilTool.paddingString("", centWidth, '-')).append("+");
					}else {
						results[i + row].append(UtilTool.paddingString("", centWidth - 1, ' ')).append("| ");
						if(lines[i + row] == null) lines[i + row] = new StringBuffer("|");
						lines[i + row].append(UtilTool.paddingString("", centWidth, ' ')).append("+");
					}
				}
			}
		}
		StringBuffer answer = new StringBuffer();
		for(int i = 0; i < rowCount; i ++) {
			answer.append(lines[i].toString()).append("\n");
			answer.append(results[i].toString()).append("\n");
		}
		answer.append(results[rowCount].toString()).append("\n");
		return answer.toString();
	}
	
}
