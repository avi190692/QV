package com.ai_int.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileData {
    public byte[] content;
    
    public String contentType;

    public String fileName;
    
    public void ReadFrom(String pathWithFileName) throws Exception
    {
        Path path = Paths.get(pathWithFileName);
        content = Files.readAllBytes(path);
        fileName = path.getFileName().toString(); 
        contentType = null;
    }

    public static FileData CreateFromFile(String pathWithFileName) throws Exception
    {
        FileData fileData = new FileData();
        fileData.ReadFrom(pathWithFileName);
        return fileData;
    }
}