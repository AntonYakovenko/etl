package com.globallogic.test.etl;

public class Main {
    public static void main(String[] args) {
        Etl etl = new Etl();
        etl.validateHeaders();
        etl.validateData();
    }
}
