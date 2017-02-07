package cn.richcloud.engine.realtime.common.utils;

public class RealResult {
	
	public static final String SUCESS = "0";
	public static final String FAIL = "-1";
	
	private String retCode;
	private String retMes;
	
	public RealResult(String retCode , String retMes){
		this.retCode = retCode;
		this.retMes = retMes;
	}

	public String getRetCode() {
		return retCode;
	}

	public void setRetCode(String retCode) {
		this.retCode = retCode;
	}

	public String getRetMes() {
		return retMes;
	}

	public void setRetMes(String retMes) {
		this.retMes = retMes;
	}
	
	public boolean isSucess(){
		if( SUCESS.equals(this.retCode)) {
			return true;
		} else {
			return false;
		}
	}

}
