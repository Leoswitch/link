package cn.richcloud.engine.realtime.timer;

import cn.richcloud.engine.realtime.busi.BrasanlaysInfoSpec;
import cn.richcloud.engine.realtime.loader.ImportLifeCycle;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class BrasanlayInfoFileImportTimer extends FileImportTimer<BrasanlaysInfoSpec>{

    public BrasanlayInfoFileImportTimer(String filePath, ArrayList<String> fieldNames, String spliterRegex, Date date, int interval) {
        super(filePath,fieldNames,spliterRegex,date,interval);
    }
    @Override
    protected void loadData(ImportLifeCycle<BrasanlaysInfoSpec> dbOptor) {
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

                if(split.length<=5){
                    System.out.println("ignore record : "+line);
                    line = br.readLine();
                    continue;
                }

                if(split.length>=5){
                    h.put(fieldName.get(1),0);
                    h.put(fieldName.get(0),split[0]);
                    h.put(fieldName.get(2),split[2]);
                    h.put(fieldName.get(3),split[1]);
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
