package com.globallogic.test.etl.db;

import com.globallogic.test.etl.tsv.TsvItem;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoTsvItemRepository implements TsvItemRepository {
    private static final String TSV_ITEMS = "tsvItems";
    private final MongoDatabase db;
    private final MongoCollection<TsvItem> collection;

    public MongoTsvItemRepository(MongoDatabase db) {
        this.db = db;
        this.collection = initCollection();
    }

    private MongoCollection<TsvItem> initCollection() {
        if (!collectionExists(db)) {
            db.createCollection(TSV_ITEMS);
        }
        return db.getCollection(TSV_ITEMS, TsvItem.class)
                .withCodecRegistry(getTsvItemCodecRegistry());
    }

    @Override
    public Optional<TsvItem> find(String id) {
        Document document = new Document("_id", id);
        return StreamSupport.stream(collection.find(document).spliterator(), false)
                .findFirst();
    }

    @Override
    public void save(TsvItem item) {
        try {
            collection.insertOne(item);
        } catch (MongoException ex) {
            System.err.printf("ERROR: Error while inserting document with ID = %s: %s\n",
                    item.getId(), ex.getMessage());
        }
    }

    @Override
    public void delete(String id) {
        Document document = new Document("_id", id);
        collection.deleteOne(document);
    }

    private boolean collectionExists(MongoDatabase db) {
        return StreamSupport.stream(db.listCollectionNames().spliterator(), false)
                .anyMatch(TSV_ITEMS::equals);
    }

    private CodecRegistry getTsvItemCodecRegistry() {
        PojoCodecProvider tsvItemCodecProvider = PojoCodecProvider.builder().
                register(TsvItem.class)
                .build();
        return fromRegistries(fromProviders(tsvItemCodecProvider),
                MongoClient.getDefaultCodecRegistry());
    }
}
