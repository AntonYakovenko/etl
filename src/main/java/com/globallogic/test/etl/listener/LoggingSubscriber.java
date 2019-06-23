package com.globallogic.test.etl.listener;

import com.globallogic.test.etl.tsv.TsvItem;

public class LoggingSubscriber implements TsvSubscriber {

    @Override
    public void onEvent(EventType eventType, TsvItem item) {
        if (EventType.VALIDATION_SUCCESS == eventType) {
            System.out.printf("INFO: Caught a validation success event on the item: %s\n", item);
        } else if (EventType.VALIDATION_ERROR == eventType) {
            System.out.printf("INFO: Caught a validation error event on the item: %s\n", item);
        } else if (EventType.SAVE_SUCCESS == eventType) {
            System.out.printf("INFO: Caught a save success event on the item: %s\n", item);
        } else if (EventType.SAVE_ERROR == eventType) {
            System.out.printf("INFO: Caught a save error event on the item: %s\n", item);
        }
    }
}
