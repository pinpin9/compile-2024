package tools;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.min;

public class StrTool {
    public static String getFormat(String string) {
        return string.replace("\\n","\\0A");
    }

    public static List<String> getStrings(String string){
        List<String> formatStrings = new ArrayList<>();
        // 将字符串中的\n替换为\0A
        string = getFormat(string);
        // 去掉首位的引号
        string = string.substring(1, string.length()-1);
        int index_c = string.indexOf("%c");
        int index_d = string.indexOf("%d");
        int index = 0;
        while(index_d != -1 || index_c != -1){
            if(index_d!=-1&&index_c!=-1){
                index = min(index_c, index_d);
            } else if (index_d!=-1) {
                index = index_d;
            } else if (index_c!=-1) {
                index = index_c;
            }
            if(index!=0){
                formatStrings.add(string.substring(0,index));
            }
            formatStrings.add(string.substring(index,index+2));
            if(index+2 < string.length()){
                string = string.substring(index+2);
                index_d = string.indexOf("%d");
                index_c = string.indexOf("%c");
            }else{
                string = null;
                index_d = index_c = -1;
            }
        }
        if(string!=null){
            formatStrings.add(string);
        }
        return formatStrings;
    }

    public static int getLen(String string){
        string = string.replace("\\0A","\\n");
        int cnt = 0;
        for(int i = 0;i<string.length();i++){
            cnt ++;
            if(string.charAt(i)=='\\'){
                i++;
            }
        }
        System.out.println(cnt);
        return cnt;
    }
}