package com.agoda.test.app;

import java.nio.file.Path;

/**
 * FileNameUtils
 *
 * @author yousheng
 * @since 2018/6/1
 */
public class FileNameUtils {
    public static Integer getFileId(Path path) {
        String name = path.getFileName().toString();
        String suffix = name.substring(name.lastIndexOf(".") + 1);
        try {
            return Integer.parseInt(suffix) + 1;
        } catch (Exception ex) {
            return 0;
        }
    }

    public static Integer getFileId(String name) {
        String suffix = name.substring(name.lastIndexOf(".") + 1);
        try {
            return Integer.parseInt(suffix) + 1;
        } catch (Exception ex) {
            return 0;
        }
    }

}
