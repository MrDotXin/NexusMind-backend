package com.mrdotxin.nexusmind.utils;

public class PathUtils {

    public static String wipeSuffix(String path) {
        int index = path.lastIndexOf('.');
        return path.substring(0, index);
    }

    public static String changeSuffix(String path, String suffix) {
        return wipeSuffix(path) + "." + suffix;
    }
}
