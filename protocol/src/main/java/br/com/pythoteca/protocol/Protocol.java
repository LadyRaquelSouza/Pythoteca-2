package br.com.pythoteca.protocol;

public final class Protocol {
    private Protocol() {}
    public static final String READER = "br.com.pythoteca.experimental.reader";
    public static final String EXECUTOR = "br.com.pythoteca.experimental.executor";
    public static final String PERMISSION = READER + ".permission.BIND_EXECUTOR";
    public static final String SERVICE = EXECUTOR + ".ProbeService";
    public static final int VERSION = 1;
    public static final int PING = 1;
    public static final int PONG = 2;
    public static final int PYTHON_PROBE = 3;
    public static final int PYTHON_RESULT = 4;
    public static final int PYTHON_ERROR = 5;
    public static final int PYTHON_FAIL_PROBE = 6;
    public static final int PYTHON_NETWORK_PROBE = 7;
    public static final int PYTHON_ENDLESS_PROBE = 8;
    public static final int PYTHON_BUSY = 9;
    public static final int PYTHON_STARTED = 10;
    public static final int JS_PROBE = 11;
    public static final int JS_RESULT = 12;
    public static final int JS_ERROR = 13;
}
