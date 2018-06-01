package com.agoda.test.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;

/**
 * OutputUtils
 *
 * @author yousheng
 * @since 2018/5/30
 */
public class OutputUtils {

    public String getNextOutputFileName(String output, String name) {
        try {
            long idx = Files.list(Paths.get(output)).filter(path -> path.getFileName().startsWith(name)).count();
            return MessageFormat.format(name + ".{0}", ++idx);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
