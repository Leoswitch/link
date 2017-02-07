package cn.richcloud.engine.realtime.timer;

import cn.richcloud.engine.realtime.loader.ImportLifeCycle;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class FileImportTimer<T> extends IntervalTimer<T>{

	protected File file = null;
	public FileImportTimer(){}

	protected List<String> fieldName ;

	protected String spliterRegex = "";
	/**
 *
 * @param firstTimer
 * @param period 以毫秒为单位
 */
	public FileImportTimer(Date firstTimer, long period){
		super(firstTimer, period);
	}

	public FileImportTimer(Timer myTimer, Date firstTimer, long period){
		super(myTimer, firstTimer, period);
	}



	public FileImportTimer(String absoluteFilePath,List<String> fieldName,String spliterRegex,Date firstTimer, long period){
		super(firstTimer,period);
		file = new File(absoluteFilePath);
		if(!file.exists()||file.isDirectory()){
			throw new RuntimeException("file is not exists or file is directory~"+absoluteFilePath);
		}
		this.fieldName = fieldName;
		this.spliterRegex = spliterRegex;
	}

	@Override
	protected void loadData(ImportLifeCycle<T> dbOptor) {
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

				if(split.length!=8&&split.length!=5){
					System.out.println("ignore record : "+line);
					line = br.readLine();
					continue;
				}

				if(split.length==5){
					h.put(fieldName.get(1),0);
					h.put(fieldName.get(0),split[0]);
					h.put(fieldName.get(2),split[2]);
					h.put(fieldName.get(3),split[1]);
				}else{
					for(int i=0;i<fieldName.size();i++){
						h.put(fieldName.get(i),split[i]);
					}
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
