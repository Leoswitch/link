package cn.richcloud.common.tool;

import java.util.Map;
import java.util.TreeMap;

import cn.richcloud.engine.realtime.LoadTool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public abstract class AnalysisTool {
	 private static final Map<String, Class<? extends AnalysisTool>> TOOLS;
	 public static final Log LOG = LogFactory.getLog(AnalysisTool.class.getName());
	public abstract AnalysisResult runTool(AnalysisOptions options);
	
	  static {
		    TOOLS = new TreeMap<String, Class<? extends AnalysisTool>>();

		
		    registerTool("LoadTool", LoadTool.class, "Hbase表数据到Hive表");
		    
		    }
	  
	  private static void registerTool(String toolName,
		      Class<? extends AnalysisTool> cls, String description) {

		    TOOLS.put(toolName, cls);
		  }
	  
	  public static AnalysisTool getTool(String toolName) {
		    Class<? extends AnalysisTool> cls = TOOLS.get(toolName);
		    try {
		      if (null != cls) {
		    	  AnalysisTool tool = cls.newInstance();
		        return tool;
		      }
		    } catch (Exception e) {
		      LOG.error(e);
		      throw new RuntimeException(e.getMessage(),e);
		    }

		    return null;
		  }
}
