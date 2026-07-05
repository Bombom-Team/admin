package me.bombom.api.v1.common.config.datasource;

public class DataSourceContextHolder {
    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();

    public static final String PROD = "PROD";
    public static final String DEV = "DEV";

    public static void setContext(String contextType) {
        contextHolder.set(contextType);
    }

    public static String getContext() {
        return contextHolder.get();
    }

    public static void clear() {
        contextHolder.remove();
    }
}
