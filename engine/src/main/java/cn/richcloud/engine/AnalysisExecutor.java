package cn.richcloud.engine;

import cn.richcloud.common.DAOUtils;
import cn.richcloud.common.IContext;
import cn.richcloud.common.tool.AnalysisOptions;
import cn.richcloud.common.tool.AnalysisResult;
import cn.richcloud.common.SystemContext;
import cn.richcloud.common.tool.AnalysisTool;

public class AnalysisExecutor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		AnalysisResult result  = null ;
		try{
			AnalysisOptions options = new AnalysisOptions(args);
			System.out.println("startup");
			AnalysisTool tool = AnalysisTool.getTool(options.getToolCMD());
			if(tool == null ) {
				result = new AnalysisResult(AnalysisResult.FAIL,"执行失败，找不到执行命令 args :" + args.toString()  );
			} else {
				String startData =  DAOUtils.getCurrentDateTime();
				long start  = System.currentTimeMillis();
				result = tool.runTool(options);
				long end  = System.currentTimeMillis();
				System.out.println("end - start"  +( end - start));
				System.out.println("startData = " + startData +" end data " + DAOUtils.getCurrentDateTime());
			}

			}catch(Throwable e) {
				e.printStackTrace();
				SystemContext.getContext().rollBakcAll();
				result = new AnalysisResult(AnalysisResult.FAIL,"执行失败" + e.getMessage() + " args : " + args.toString());
			}finally{
	 			SystemContext.getContext().releaseAll();
			}
			System.out.println(IContext.RET_CODE + ":"
					+ result.getRetCode());
			System.out.println(IContext.RET_MES + ":"
					+ result.getRetMes());
//		Thread.currentThread().
			System.exit(0);
	}


}



