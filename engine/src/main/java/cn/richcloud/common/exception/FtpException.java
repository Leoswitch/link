package cn.richcloud.common.exception;

public class FtpException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FtpException() {

    }

    public FtpException(String message, Throwable cause) {
        super(message, cause);
    }

    public FtpException(String message) {
        super(message);
    }
}
