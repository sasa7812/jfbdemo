/*
 * Copyright 2014 Alexander Nikitin <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 * Released under the MIT license. See LICENSE.
 */
package ru.savvy.springjsf.service.events;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * As jsf managed beans and spring beans controlled by different containers, we need this simple
 * singleton event bus to support event communications between them just enough to hold this demo
 * only one event per subscriber
 *
 * @author sasa <a href="mailto:sasa7812@gmail.com">Alexander Nikitin</a>
 */
@Service
@Scope(value = "singleton")
public class EventBus {
    private Vector<EventListener> eventListeners = new Vector<>();

    public synchronized void subscribe(EventListener listener){
        eventListeners.add(listener);
    }

    public synchronized void unsubscribe(EventListener listener){
        eventListeners.remove(listener);
    }

    public synchronized void fire(String data){
        for(EventListener listenerEntry : eventListeners){
                listenerEntry.onEvent(data);
        }
    }

}
