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

    Predicate<Path> pathTypePredicate = path -> !path.getFileName().toString().startsWith(".") && !path.getFileName().toString().endsWith(".zip");

    Predicate<Path> isFile = Files::isRegularFile;

    Predicate<Path> allPredicate = isFile.and(pathTypePredicate);
}
