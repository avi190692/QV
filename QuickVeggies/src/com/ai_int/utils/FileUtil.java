package com.ai_int.utils;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class FileUtil {
	
	private final static Map<String, String[]> excelExtMap = new LinkedHashMap<>();
	private final static Map<String, String[]> pdfExtMap = new LinkedHashMap<>();
	static {
		excelExtMap.put("Excel Files", new String[]{"*.xls"});
	}
	static {
		pdfExtMap.put("PDF Files", new String[]{"*.pdf"});
	}


	public static String getSaveToFileName(Scene scene, String title, Map<String, String[]> extensionMap ) {
		FileChooser fc = new FileChooser();
		fc.setTitle(title);
		for (String ext : extensionMap.keySet()) {
			fc.getExtensionFilters().add(new ExtensionFilter(ext, extensionMap.get(ext)));
		}
		File file = fc.showSaveDialog(scene.getWindow()); 
		return (file != null ? file.toString() : null);
	}
	
	public static File getFileToOpen(Scene scene, String title, Map<String, String[]> extensionMap ) {
		FileChooser fc = new FileChooser();
		fc.setTitle(title);
		for (String ext : extensionMap.keySet()) {
			fc.getExtensionFilters().add(new ExtensionFilter(ext, extensionMap.get(ext)));
		}
		File file = fc.showOpenDialog(scene.getWindow()); 
		return file;
	}

	
	public static Map<String, String[]> getExcelExtMap() {
		return excelExtMap;
	}

	public static Map<String, String[]> getPdfExtMap() {
		return pdfExtMap;
	}
}
