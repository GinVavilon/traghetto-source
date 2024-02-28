package com.github.ginvavilon.traghentto;

public class PathUtils {

    public static final char PATH_SEPARATOR_CHAR = '/';
    public static final String PATH_SEPARATOR = String.valueOf(PATH_SEPARATOR_CHAR);

    public static String extractName(String path) {
        int last = path.length() - 1;
        if (PATH_SEPARATOR_CHAR == (path.charAt(last))) {
            last--;
        }
        int index = path.lastIndexOf(PATH_SEPARATOR, last);
        return path.substring(index + 1, last + 1);
    }

    public static String concat(String path, String childPath) {
        String pathSuffix;
        if (path.endsWith(PathUtils.PATH_SEPARATOR)) {
            pathSuffix = "";
        } else {
            pathSuffix = PathUtils.PATH_SEPARATOR;
        }
        return path + pathSuffix + childPath;
    }

}
