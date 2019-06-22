package com.globallogic.test.etl;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.time.LocalDate;
import java.util.stream.StreamSupport;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoTool {

    private static final String TSV_ITEMS = "tsvItems";

    public void mongo() {
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        MongoDatabase db = mongoClient.getDatabase("etl");

        if (!collectionExists(db)) {
            db.createCollection(TSV_ITEMS);
        }

        MongoCollection<TsvItem> tsvItems = db.getCollection(TSV_ITEMS, TsvItem.class)
                .withCodecRegistry(getTsvItemCodecRegistry());

        TsvItem item = new TsvItem();
        item.setId("1");
        item.setName("name1");
        item.setQuantity(1);
        item.setDateCreated(LocalDate.now());

        tsvItems.insertOne(item);

    }

    public static boolean collectionExists(MongoDatabase db) {
        return StreamSupport.stream(db.listCollectionNames().spliterator(), false)
                .anyMatch(TSV_ITEMS::equals);
    }

    public static CodecRegistry getTsvItemCodecRegistry() {
        PojoCodecProvider tsvItemCodecProvider = PojoCodecProvider.builder().
                register(TsvItem.class)
                .build();
        return fromRegistries(fromProviders(tsvItemCodecProvider),
                MongoClient.getDefaultCodecRegistry());
    }

}
