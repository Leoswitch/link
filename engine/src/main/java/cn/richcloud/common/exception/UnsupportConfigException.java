package cn.richcloud.common.exception;

public class UnsupportConfigException extends RuntimeException {

	public UnsupportConfigException() {
		super();
	}

	public UnsupportConfigException(String message, Throwable cause) {
		super(message + "\n  nest exception:" + cause.getMessage(), cause);
	}

	public UnsupportConfigException(String message) {
		super(message);
	}

	public UnsupportConfigException(Throwable cause) {
		super(cause);
	}

	public UnsupportConfigException(String rule_id, String message) {
		super("规则["+rule_id+"]" + message);
	}
}
