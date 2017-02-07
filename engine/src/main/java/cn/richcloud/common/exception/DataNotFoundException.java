package cn.richcloud.common.exception;

public class DataNotFoundException extends RuntimeException {
	public DataNotFoundException() {
		super();
	}

	public DataNotFoundException(String message) {
		super(message);
	}
}
