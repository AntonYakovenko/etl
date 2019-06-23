package com.globallogic.test.etl.listener;

public interface TsvPublisher {

    void addSubscriber(EventType eventType, TsvSubscriber subscriber);

    void removeSubscriber(EventType eventType, TsvSubscriber subscriber);
}
