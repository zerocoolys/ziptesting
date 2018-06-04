package com.agoda.test.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;

/**
 * Predicates
 *
 * @author yousheng
 * @since 2018/6/4
 */
public interface Predicates {

    Predicate<Path> FILE_TYPE_PREDICATE = path -> !path.getFileName().toString().startsWith(".") && !path.getFileName().toString().endsWith(".zip");

    Predicate<Path> IS_FILE = Files::isRegularFile;

    Predicate<Path> DIR_TYPE_PREDICATE = path -> !path.getFileName().toString().startsWith(".");
    Predicate<Path> IS_DIR = Files::isDirectory;

    Predicate<Path> ALL_DIR_PREDICATE = IS_DIR.and(DIR_TYPE_PREDICATE);

    Predicate<Path> ALL_FILE_PREDICATE = IS_FILE.and(FILE_TYPE_PREDICATE);
}
