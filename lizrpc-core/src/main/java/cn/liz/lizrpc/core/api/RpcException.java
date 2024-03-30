package cn.liz.lizrpc.core.api;

import lombok.Data;

@Data
public class RpcException extends RuntimeException {
    private String errCode;

    public RpcException() {
    }

    public RpcException(String message) {
        super(message);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(Throwable cause) {
        super(cause);
    }

    public RpcException(Throwable cause, String errCode) {
        super(cause);
        this.errCode = errCode;
    }

    public enum ErrCodeEnum {
        // X 技术类异常
        // Y 业务类异常
        // Z unknown异常
        SocketTimeout("X001", "http_timeout"),
        NoSuchMethod("X002", "no_such_method"),
        Unknown("Z001", "unknown");

        String code;

        String message;

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        ErrCodeEnum(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }

}
