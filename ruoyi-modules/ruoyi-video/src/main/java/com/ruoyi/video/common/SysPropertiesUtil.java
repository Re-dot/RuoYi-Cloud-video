package com.ruoyi.video.common;

public class SysPropertiesUtil {
    private static SysConfig sysConfig = (SysConfig)SpringUtil.getBean(SysConfig.class);

    public SysPropertiesUtil() {
    }

    public static String getString(String key) {
        String value = "";

        try {
            value = sysConfig.getVal(key);
        } catch (Exception var3) {
            var3.printStackTrace();
        }

        return value;
    }

    public static Integer getInt(String key) {
        Integer value = 0;

        try {
            value = Integer.valueOf(sysConfig.getVal(key));
        } catch (Exception var3) {
            var3.printStackTrace();
        }

        return value;
    }

    public static Boolean getBoolean(String key) {
        Boolean value = false;

        try {
            value = Boolean.valueOf(sysConfig.getVal(key));
        } catch (Exception var3) {
            var3.printStackTrace();
        }

        return value;
    }

    public static Long getLong(String key) {
        Long value = 0L;

        try {
            value = Long.valueOf(sysConfig.getVal(key));
        } catch (Exception var3) {
            var3.printStackTrace();
        }

        return value;
    }
}
