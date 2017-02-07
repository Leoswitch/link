package cn.richcloud.common.exception;

public class ConfigNotFoundException extends RuntimeException {

	public ConfigNotFoundException(){
		super();
	}
	
	public ConfigNotFoundException(String message){
		super(message);
	}
	
	public ConfigNotFoundException(String rule_id, String message){
		super("规则["+rule_id+"]"+ message);
	}
	
}
