package com.example.demo.service;

/**
 * Service contract for replaying events from Dead Letter Topics (DLT) back to destination topics.
 */
public interface ReplayService {

    void replayEventFromDlt(String dltTopic, String key, Object payload);

    void replayAllFailedEvents(String dltTopic);
}
