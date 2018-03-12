package com.github.charlesvhe.springcloud.practice.core.vo;

import java.io.Serializable;

public class Response<T> implements Serializable {
    protected Boolean status = Boolean.TRUE;
    protected String code = "200";
    protected String msg = "success";
    protected String errorMsg = "";
    protected T data;

    public Response(Boolean status, String code, String msg, String errorMsg) {
        super();
        this.status = status;
        this.code = code;
        this.msg = msg;
        this.errorMsg = errorMsg;
    }

    public Response(T data) {
        super();
        this.data = data;
    }

    public Response(Boolean status, String code, String msg, String errorMsg, T data) {
        super();
        this.status = status;
        this.code = code;
        this.msg = msg;
        this.errorMsg = errorMsg;
        this.data = data;
    }

    public Response() {
        super();
    }

    public Boolean success() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public T getData() {
        return data;
    }

    /**
     * toString
     *
     * @return
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "Response [status=" + status + ", code=" + code + ", msg=" + msg + ", errorMsg=" + errorMsg + ", data="
                + data + "]";
    }

    /**
     * hashCode
     *
     * @return
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((code == null) ? 0 : code.hashCode());
        result = prime * result + ((data == null) ? 0 : data.hashCode());
        result = prime * result + ((errorMsg == null) ? 0 : errorMsg.hashCode());
        result = prime * result + ((msg == null) ? 0 : msg.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        return result;
    }

    /**
     * equals
     *
     * @param obj
     * @return
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Response<?> other = (Response<?>) obj;
        if (code == null) {
            if (other.code != null) {
                return false;
            }
        } else if (!code.equals(other.code)) {
            return false;
        }

        if (data == null) {
            if (other.data != null) {
                return false;
            }
        } else if (!data.equals(other.data)) {
            return false;
        }
        if (errorMsg == null) {
            if (other.errorMsg != null) {
                return false;
            }
        } else if (!errorMsg.equals(other.errorMsg)) {
            return false;
        }
        if (msg == null) {
            if (other.msg != null) {
                return false;
            }
        } else if (!msg.equals(other.msg)) {
            return false;
        }
        if (status == null) {
            if (other.status != null) {
                return false;
            }
        } else if (!status.equals(other.status)) {
            return false;
        }
        return true;
    }

}
