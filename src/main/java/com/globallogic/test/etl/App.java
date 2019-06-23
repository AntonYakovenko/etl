package com.globallogic.test.etl;

import com.globallogic.test.etl.db.DatabaseInitializer;
import com.globallogic.test.etl.db.MongoTsvItemRepository;
import com.globallogic.test.etl.db.TsvItemRepository;
import com.globallogic.test.etl.listener.EventType;
import com.globallogic.test.etl.listener.LoggingSubscriber;
import com.globallogic.test.etl.listener.TsvPublisher;
import com.globallogic.test.etl.tsv.TsvProcessor;
import com.globallogic.test.etl.tsv.TsvItemProcessorImpl;
import com.mongodb.client.MongoDatabase;

import java.util.Optional;

public class App {
    private final static String PATH = "src\\main\\resources\\etl_0.tsv";

    public static void main(String[] args) {
        MongoDatabase db = DatabaseInitializer.initDb();
        TsvItemRepository repository = new MongoTsvItemRepository(db);
        TsvProcessor tsvProcessor = new TsvItemProcessorImpl(repository);

        TsvPublisher tsvPublisher = Optional.of(tsvProcessor)
                .map(TsvPublisher.class::cast)
                .get();

        // TODO: subscribe only to VALIDATION_SUCCESS, VALIDATION_ERROR events!
        for (EventType eventType : EventType.values()) { // subscribe to all events
            tsvPublisher.addSubscriber(eventType, new LoggingSubscriber());
        }

        tsvProcessor.migrate(PATH);
    }
}
