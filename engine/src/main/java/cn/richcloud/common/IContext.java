package cn.richcloud.common;

import java.util.Map;

/**
 * 这是一个规则的上下文，最终会给ETL返回执行是否是成功的信息
 * @author zhou.peiyaun
 *
 */
public abstract class IContext {
	
	public static final String RET_CODE_ERROR = "-1";
	public static final String RET_CODE_SUSS = "0";
	public static final String RET_CODE_FAIL = "-1";
	
	public static final String  RET_CODE = "retCode";
	public static final String  RET_MES = "retMes";

	private String retCode = ""; // -1： 异常  0 成功（满足） 1 不满足
	private String retMes= "";
	
	public String getRetMes() {
		return retMes;
	}
	public String getRetCode() {
		return retCode;
	}

	public void setRetCode(String retCode) {
		this.retCode = retCode;
	}


	public void setRetMes(String retMes) {
		this.retMes = retMes;
	}
	
	public abstract Map getContextData() ;
}
