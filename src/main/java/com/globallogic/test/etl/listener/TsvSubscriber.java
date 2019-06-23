package com.globallogic.test.etl.listener;

import com.globallogic.test.etl.tsv.TsvItem;

public interface TsvSubscriber {

    void onEvent(EventType eventType, TsvItem item);
}
