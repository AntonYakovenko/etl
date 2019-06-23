package com.globallogic.test.etl.listener;

import com.globallogic.test.etl.tsv.TsvItem;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public abstract class TsvItemPublisher implements TsvPublisher {

    private final Map<EventType, List<TsvSubscriber>> subscribers = new EnumMap<>(EventType.class);

    public TsvItemPublisher() {
        for (EventType eventType : EventType.values()) {
            subscribers.put(eventType, new ArrayList<>());
        }
    }

    @Override
    public void addSubscriber(EventType eventType, TsvSubscriber subscriber) {
        List<TsvSubscriber> eventSubscribers = subscribers.get(eventType);
        eventSubscribers.add(subscriber);
    }

    @Override
    public void removeSubscriber(EventType eventType, TsvSubscriber subscriber) {
        List<TsvSubscriber> eventSubscribers = subscribers.get(eventType);
        eventSubscribers.remove(subscriber);
    }

    protected void notify(EventType eventType, TsvItem item) {
        List<TsvSubscriber> eventSubscribers = subscribers.get(eventType);
        eventSubscribers.forEach(subscriber -> subscriber.onEvent(eventType, item));
    }
}
