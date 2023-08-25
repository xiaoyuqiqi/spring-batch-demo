package com.cosmosource.batch.listener;

import org.springframework.batch.core.ItemReadListener;
import org.springframework.stereotype.Component;

/**
 * 
 */
@Component
public class MyItemReaderListener implements ItemReadListener<String> {
    @Override
    public void beforeRead() {
        System.out.println("before read");
    }

    @Override
    public void afterRead(String item) {
        System.out.println("after read: " + item);
    }

    @Override
    public void onReadError(Exception ex) {
        System.out.println("on read error: " + ex.getMessage());
    }
}
