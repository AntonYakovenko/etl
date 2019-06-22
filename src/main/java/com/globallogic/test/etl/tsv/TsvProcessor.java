package com.globallogic.test.etl.tsv;

import java.net.URI;
import java.util.function.Predicate;

public interface TsvProcessor {

    void migrate(String path);

    void migrate(String path, Predicate<? super TsvItem> predicate);

    void migrate(URI uri);

    void migrate(URI uri, Predicate<? super TsvItem> predicate);
}
