package com.globallogic.test.etl.db;

import com.globallogic.test.etl.tsv.TsvItem;

import java.util.Optional;

public interface TsvItemRepository {

    Optional<TsvItem> find(String id);

    void save(TsvItem item);

    void delete(String id);
}
