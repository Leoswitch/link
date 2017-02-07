package cn.richcloud.engine.realtime;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.richcloud.common.tool.AnalysisOptions;
import cn.richcloud.engine.realtime.loader.BrasanlayInfoFileLoader;
import cn.richcloud.engine.realtime.loader.Loader;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.richcloud.common.tool.AnalysisResult;
import cn.richcloud.common.tool.AnalysisTool;
import cn.richcloud.engine.realtime.common.utils.RealResult;
import cn.richcloud.engine.realtime.loader.OnuDelinfoInfoLoader;


public class LoadTool extends AnalysisTool {
	private final static Log LOG = LogFactory.getLog(LoadTool.class.getClass());
	private static final Map<String, String> cmdMap = new ConcurrentHashMap<String, String>();

	static {
		register("brasAnlaysInfoFileLoader", BrasanlayInfoFileLoader.class.getName());
		register("onuInfoLoader", OnuDelinfoInfoLoader.class.getName());
	}
	
	private static void register(String cmd, String clazz) {
		cmdMap.put(cmd, clazz);
	}

	@Override
	public AnalysisResult runTool(AnalysisOptions options) {
		String clazz = cmdMap.get(options.getLoadCMD().trim());
		if (StringUtils.isEmpty(clazz)) {
			LOG.error("can not find the cmd " + options.getLoadCMD().trim());
			throw new RuntimeException("can not find the cmd "
					+ options.getLoadCMD().trim());
		}
		try {

			Class cl = Thread.currentThread().getContextClassLoader()
					.loadClass(clazz);
			Loader tool = (Loader) cl.newInstance();
			tool.load(options);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return new AnalysisResult(RealResult.SUCESS, "执行成功");
	}

	public static void main(String[] args) {
		String a = "aa|b|b|d|sdf|";
		System.out.println(a.split("\\|").length);
	}

//	public static void main(String[] args) {
//		/*AnalysisOptions options = new AnalysisOptions(new String[]{"load_cmd:onuInfoLoader",
//				"quorumServers:192.168.2.36:2181"
//		,"interval:1000000","zNode:/onu","zValue:hello","zookeeperTimeOut:100","filePath:/root/ONU_ACCESSNUM_20160817-UTF8.txt"});
//
//		LoadTool loadTool = new LoadTool();
//		loadTool.runTool(options);*/
//
//		try{
//			ArrayList<String> fieldNames = Lists.newArrayList("areaid", "useraccount", "portname", "provicename", "districtname", "buildingname", "unitname", "onuname");
//			File file = new File("/root/ONU_ACCESSNUM_20160817-UTF8.txt");
//			FileReader fr = new FileReader(file) ;
//			BufferedReader br = new BufferedReader(fr);
//			String line = br.readLine();
//			while (StringUtils.isNotEmpty(line)) {
//				Map<String, Object> h = new HashMap<String, Object>();
//				String[] split = line.split("\\|");
//
//				if(split.length!=8){
//					System.out.println("ignore record : "+line);
//					continue;
//				}
//				for(int i=0;i<split.length;i++){
//					h.put(fieldNames.get(i),split[i]);
//				}
//				String useraccount = h.get("useraccount").toString();
//				if(!isPhone(useraccount)){
////					return;
//					System.out.println("ignore---------->"+ line);
//				}
//
//				line = br.readLine();
//			}
//		}catch (Exception e){
//
//		}
//
//	}
//
//	private static boolean isPhone(String phone){
//		boolean result = false;
//		if(StringUtils.isNotEmpty(phone)&&
//				phone.length()>5){
//			try {
//				Long.parseLong(phone);
//				result = true;
//			}catch (Exception e){
//				result = false;
//			}
//
//		}
//		if(!result){
//			System.out.println(">>>>>>not phoneNum "+phone);
//		}
//
//		return result;
//	}

}
