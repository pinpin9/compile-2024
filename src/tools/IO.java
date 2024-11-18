package tools;

import settings.Settings;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;



public class IO {
    String outputFile;
    PrintWriter writer;
    public IO(String outputFile){
        this.outputFile = outputFile;
        try {
            writer = new PrintWriter(new FileWriter(outputFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    // 从testfile中读入sourceCode
    public static String getInput(){
        byte[] bytes = null;
        try(FileInputStream input = new FileInputStream(Settings.inputFile)){
            // 读取所有字节
            bytes = new byte[input.available()];
            input.read(bytes);
        }catch (IOException e){
            System.out.println("读入文件错误");
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     *
     * @param list：待输出的list列表
     * @throws IOException
     */
    public void output(List<?> list){
        for (Object item : list) {
            writer.println(item.toString());
        }
        writer.flush();
    }


    public void output(String s){
        writer.println(s);
        writer.flush();
    }
}
