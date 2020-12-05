package com.example.demo.queue;

import com.example.demo.controller.Indexer;
import com.example.demo.queue.event.DocumentIndexEvent;
import com.example.demo.util.CustomThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class IndexProcessingQueue {


    private final ExecutorService executorService =  Executors.newSingleThreadExecutor(new CustomThreadFactory("notification-service"));
    private final BlockingQueue<Object> eventQueue = new LinkedBlockingQueue<>();

    @Autowired
    private Indexer indexer;

    private volatile boolean stop = false;


    @PostConstruct
    public void postConstruct() {
        this.executorService.submit(() -> this.handleNotifications());
    }

    public void addDocumentIndexEvent(DocumentIndexEvent documentIndexEvent) {
        if (documentIndexEvent == null) {
            return;
        }
        this.eventQueue.offer(documentIndexEvent);
    }

    public void handleNotifications() {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        List<Object> events = new ArrayList<>();

        while (!stop) {
            try {
                // waiting until an element becomes available
                events.add(this.eventQueue.take());
            } catch (Exception ex) {
                System.out.println("Error on taking from eventQueue" + ex);
            }
            try {
                this.eventQueue.drainTo(events);
            } catch (Exception ex) {
                System.out.println("Error on draining from eventQueue" + ex);
            }
            List<DocumentIndexEvent> documentIndexEvents = new ArrayList<>();

            events.forEach((e) -> {
                if (e instanceof DocumentIndexEvent) {
                    documentIndexEvents.add((DocumentIndexEvent) e);
                }
            });
            try {
                if (!documentIndexEvents.isEmpty()) {
                    for (DocumentIndexEvent event : documentIndexEvents) {
                        if (event.isDelete()) {
                            indexer.deleteDocument(event.getJsonObject());
                        }else{
                            indexer.indexDocument(event.getJsonObject());
                        }
                    }
                }
            } catch (Exception ex) {
                System.out.println("Error on processing records" + ex.getMessage());
            }
            events = new ArrayList<>();
        }
    }

    @PreDestroy
    public void preDestroy() {
        this.stop = true;
        if (executorService != null) {
            try {
                // wait 5 seconds for closing all threads
                executorService.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
