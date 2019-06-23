package com.globallogic.test.etl.db;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

/**
 * Utility class to initialize mongo database with name "etl".
 * Uses specified or default connection data
 */
public final class DatabaseInitializer {

    private static final String DB_NAME = "etl";

    private DatabaseInitializer() {
    }

    public static MongoDatabase initDb() {
        return initDb("localhost", 27017);
    }

    public static MongoDatabase initDb(String host, int port) {
        MongoClient mongoClient = new MongoClient(host, port);
        return mongoClient.getDatabase(DB_NAME);
    }

    public static MongoDatabase initDb(String uri) {
        MongoClient mongoClient = new MongoClient(new MongoClientURI(uri));
        return mongoClient.getDatabase(DB_NAME);
    }
}
