package com.globallogic.test.etl;

public class Main {
    public static void main(String[] args) {
        TsvReader tsvReader = new TsvReader();
        tsvReader.validateHeaders();
        tsvReader.validateData();

//        MongoTool mongoTool = new MongoTool();
//        mongoTool.mongo();
    }
}
