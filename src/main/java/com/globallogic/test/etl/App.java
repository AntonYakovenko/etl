package com.globallogic.test.etl;

import com.globallogic.test.etl.db.DatabaseInitializer;
import com.globallogic.test.etl.db.TsvItemRepository;
import com.globallogic.test.etl.db.TsvItemRepositoryImpl;
import com.globallogic.test.etl.tsv.TsvProcessor;
import com.globallogic.test.etl.tsv.TsvProcessorImpl;
import com.mongodb.client.MongoDatabase;

public class App {
    private final static String PATH = "src\\main\\resources\\etl_0.tsv";

    public static void main(String[] args) {
        MongoDatabase db = DatabaseInitializer.initDb();
        TsvItemRepository repository = new TsvItemRepositoryImpl(db);
        TsvProcessor tsvProcessor = new TsvProcessorImpl(repository);
        tsvProcessor.migrate(PATH);
    }
}
