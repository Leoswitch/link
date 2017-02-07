package cn.richcloud.common.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AnalysisOptions {

	private String args[] = new String[]{};
	Map<String,String> properties = null;
	public   AnalysisOptions(){
		
	}
	public AnalysisOptions(String args[] ){
		properties = new HashMap<String,String>();
		String[] ars = new String[]{};
		this.args=args;
		for(String arg : args) {
			ars = arg.split(":",2);
			if(ars.length == 2 ) {
				properties.put(ars[0], ars[1]);
			}
		}
	}
	
	public Map<String,String> getProperties(){
		
		return properties;
	}
	
	public String[] getArgs(){
		List result_list=new ArrayList();
		Iterator iter = properties.entrySet().iterator(); 
		int i=0;
		while (iter.hasNext()) { 
		    Map.Entry entry = (Map.Entry) iter.next(); 
		    String key = entry.getKey()+""; 
		    String val = entry.getValue()+"";
		    result_list.add(key+":"+val);
		} 
		
		return (String[]) result_list.toArray(new String[0]);
	}
	
	public String getToolCMD(){
		return properties.get("tool_cmd");
	}
	
	public String getBusicCMD(){
		return properties.get("busic_cmd");
	}
	
	public String getLoadCMD(){
		return properties.get("load_cmd");
	}
	
}
