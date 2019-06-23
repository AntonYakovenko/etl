package com.globallogic.test.etl;

import com.globallogic.test.etl.db.DatabaseInitializer;
import com.globallogic.test.etl.db.TsvItemRepository;
import com.globallogic.test.etl.db.MongoTsvItemRepository;
import com.globallogic.test.etl.tsv.TsvProcessor;
import com.globallogic.test.etl.tsv.TsvProcessorImpl;
import com.mongodb.client.MongoDatabase;

public class App {
    private final static String SOURCE_FILE_PATH = "src\\main\\resources\\etl_0.tsv";

    public static void main(String[] args) {
        final String mongoDbUri = System.getenv("MONGODB_URI");
        final String sourceFilePath = System.getenv("SOURCE_FILE_PATH");

        MongoDatabase db = mongoDbUri != null
                ? DatabaseInitializer.initDb(mongoDbUri)
                : DatabaseInitializer.initDb();

        TsvItemRepository repository = new MongoTsvItemRepository(db);
        TsvProcessor tsvProcessor = new TsvProcessorImpl(repository);
        tsvProcessor.migrate(sourceFilePath != null ? sourceFilePath : SOURCE_FILE_PATH);
    }
}
