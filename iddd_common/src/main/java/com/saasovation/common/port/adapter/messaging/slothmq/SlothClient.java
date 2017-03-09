//   Copyright 2012,2013 Vaughn Vernon
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package com.saasovation.common.port.adapter.messaging.slothmq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlothClient extends SlothWorker {

    private static SlothClient instance;

    private Map<String, ExchangeListener> exchangeListeners;

    public static synchronized SlothClient instance() {
        if (instance == null) {
            instance = new SlothClient();
        }

        return instance;
    }

    public void close() {
        logger.debug("Closing...");

        super.close();

        List<ExchangeListener> listeners = new ArrayList<ExchangeListener>(this.exchangeListeners.values());

        for (ExchangeListener listener : listeners) {
            this.unregister(listener);
        }

        logger.debug("Closed.");
    }

    public void closeAll() {
        instance = null;

        this.close();

        this.sendToServer("CLOSE:");
    }

    public void publish(String anExchangeName, String aType, String aMessage) {
        String encodedMessage = "PUBLISH:" + anExchangeName + "TYPE:" + aType + "MSG:" + aMessage;

        this.sendToServer(encodedMessage);
    }

    public void register(ExchangeListener anExchangeListener) {
        this.exchangeListeners.put(anExchangeListener.name(), anExchangeListener);
        logger.debug("Registered: {} total listeners: {}", anExchangeListener.getClass().getSimpleName(), this.exchangeListeners.size());

        this.sendToServer("SUBSCRIBE:" + this.port() + ":" + anExchangeListener.exchangeName());
    }

    public void unregister(ExchangeListener anExchangeListener) {
        this.exchangeListeners.remove(anExchangeListener.name());
        logger.debug("Unegistered: {} total listeners: {}", anExchangeListener.getClass().getSimpleName(), this.exchangeListeners.size());

        this.sendToServer("UNSUBSCRIBE:" + this.port() + ":" + anExchangeListener.exchangeName());
    }

    private SlothClient() {
        super();

        this.exchangeListeners = Collections.synchronizedMap(new HashMap<>());

        this.attach();
        this.receiveAll();
    }

    private void attach() {
        this.sendToServer("ATTACH:" + this.port());
    }

    private void dispatchMessage(String anEncodedMessage) {
        int exchangeDivider = anEncodedMessage.indexOf("PUBLISH:");
        int typeDivider = anEncodedMessage.indexOf("TYPE:", exchangeDivider + 8);
        int msgDivider = anEncodedMessage.indexOf("MSG:", typeDivider + 5);

        String exchangeName = anEncodedMessage.substring(exchangeDivider + 8, typeDivider);
        String type = anEncodedMessage.substring(typeDivider + 5, msgDivider);
        String message = anEncodedMessage.substring(msgDivider + 4);

        List<ExchangeListener> listeners = null;
        logger.info("Dispatching: Exchange: {} Type: {} Msg: {}", exchangeName, type, message);

        listeners = new ArrayList<>(this.exchangeListeners.values());
        for (ExchangeListener listener : listeners) {
            if (listener.exchangeName().equals(exchangeName) && listener.listensTo(type)) {
                try {
                    listener.filteredDispatch(type, message);
                } catch (Exception e) {
                    logger.error("Exception while dispatching message: {}", anEncodedMessage, e);
                }
            } else {
                logger.debug("Skipping: {} not on exchange: {} or listening to type: {}", listener.getClass(), exchangeName, type);
            }
        }
    }

    private void receiveAll() {
        Thread receiverThread = new Thread() {

            @Override
            public void run() {
                while (!isClosed()) {
                    String receivedData = receive();

                    if (receivedData != null) {
                        dispatchMessage(receivedData.trim());
                    } else {
                        sleepFor(10L);
                    }
                }
            }
        };

        receiverThread.start();
    }
}
