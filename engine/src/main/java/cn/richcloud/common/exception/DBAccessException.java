package cn.richcloud.common.exception;

public class DBAccessException extends RuntimeException {

	public DBAccessException() {
		super();
	}

	public DBAccessException(String message, Throwable cause) {
		super(message+"\n  nest exception:"+cause.getMessage(), cause);
	}

	public DBAccessException(String message) {
		super(message);
	}

	public DBAccessException(Throwable cause) {
		super(cause);
	}
	
}
