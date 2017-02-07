package cn.richcloud.engine.realtime.timer;

import cn.richcloud.engine.realtime.busi.OnuDelinfoSpec;
import cn.richcloud.engine.realtime.loader.ImportLifeCycle;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class OnuInfoFileImportTimer extends FileImportTimer<OnuDelinfoSpec>{

    public OnuInfoFileImportTimer(String filePath, ArrayList<String> fieldNames, String spliterRegex, Date date, int interval) {
        super(filePath,fieldNames,spliterRegex,date,interval);
    }

    @Override
    protected void loadData(ImportLifeCycle<OnuDelinfoSpec> dbOptor) {
        System.out.println("loadData=======================");
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(file) ;
            br = new BufferedReader(fr);
            String line = br.readLine();
            dbOptor.importBefore();
            while (StringUtils.isNotEmpty(line)) {
                Map<String, Object> h = new LinkedHashMap<String, Object>();
                String[] split = line.split(spliterRegex);
                if(split.length<8){
                    System.out.println("ignore record : "+line);
                    line = br.readLine();
                    continue;
                }
                for(int i=0;i<fieldName.size();i++){
                    h.put(fieldName.get(i),split[i]);
                }

                dbOptor.imports(h);
                line = br.readLine();
            }
        } catch (Exception se) {
            se.printStackTrace();
            throw new RuntimeException("systemError"+se.getMessage());
        } finally {
            dbOptor.importCompete();
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
