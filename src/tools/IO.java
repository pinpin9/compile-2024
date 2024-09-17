package tools;

import settings.Settings;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

// 从testfile中读入sourceCode

public class IO {

    public static String getInput() throws IOException {
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
     * @param list：待输出的输出
     * @param outputFile：指定输出文件的路径
     * @throws IOException
     */
    public static void output(List<?> list, String outputFile) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, false))) {
            for (Object item : list) {
                outputLine(item.toString(), writer);
            }
        } catch (IOException e) {
            throw new IOException("写入文件时发生错误：" + e.getMessage(), e);
        }
    }

    public static void outputLine(String s, BufferedWriter writer) throws IOException {
        writer.write(s);
        writer.flush(); // 刷新缓冲区，确保数据立即写入文件
    }
}
