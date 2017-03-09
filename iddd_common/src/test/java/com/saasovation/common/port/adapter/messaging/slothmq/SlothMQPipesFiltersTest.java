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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import junit.framework.TestCase;

import com.saasovation.common.domain.model.DomainEventPublisher;
import com.saasovation.common.notification.Notification;
import com.saasovation.common.notification.NotificationReader;
import com.saasovation.common.notification.NotificationSerializer;
import com.saasovation.common.port.adapter.messaging.AllPhoneNumbersCounted;
import com.saasovation.common.port.adapter.messaging.AllPhoneNumbersListed;
import com.saasovation.common.port.adapter.messaging.MatchedPhoneNumbersCounted;
import com.saasovation.common.port.adapter.messaging.PhoneNumbersMatched;

public class SlothMQPipesFiltersTest extends TestCase {

    private ExchangePublisher publisher;

    private ExchangeListener matchtedPhoneNumberCounter;
    private PhoneNumberExecutive phoneNumberExecutive;
    private ExchangeListener phoneNumberFinder;
    private ExchangeListener totalPhoneNumbersCounter;

    private static String[] phoneNumbers = new String[] {
        "303-555-1212   John",
        "212-555-1212   Joe",
        "718-555-1212   Zoe",
        "720-555-1212   Manny",
        "312-555-1212   Jerry",
        "303-555-9999   Sally"
    };

    public SlothMQPipesFiltersTest() {
        super();
    }

    public void testPhoneNumbersCounter() throws Exception {
        String processId = this.phoneNumberExecutive.start(phoneNumbers);

        PhoneNumberProcess process = this.phoneNumberExecutive.processOfId(processId);
        while (!process.isCompleted()) {
            Thread.sleep(25L);
            process = this.phoneNumberExecutive.processOfId(processId);
        }

        assertNotNull(process);
        assertEquals(2, process.matchedPhoneNumbers());
        assertEquals(6, process.totalPhoneNumbers());
    }

    @Override
    protected void setUp() throws Exception {
        DomainEventPublisher.instance().reset();

        SlothServer.executeInProcessDetachedServer();

        Thread.sleep(500L);

        phoneNumberExecutive = new PhoneNumberExecutive();
        phoneNumberFinder = new PhoneNumberFinder();
        matchtedPhoneNumberCounter = new MatchedPhoneNumberCounter();
        totalPhoneNumbersCounter = new TotalPhoneNumbersCounter();

        SlothClient.instance().register(this.phoneNumberExecutive);
        SlothClient.instance().register(phoneNumberFinder);
        SlothClient.instance().register(matchtedPhoneNumberCounter);
        SlothClient.instance().register(totalPhoneNumbersCounter);

        this.publisher = new ExchangePublisher("PhoneNumberExchange");

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {

        phoneNumberExecutive.close();
        phoneNumberFinder.close();
        matchtedPhoneNumberCounter.close();
        totalPhoneNumbersCounter.close();

        SlothClient.instance().closeAll();

        super.tearDown();
    }

    private void send(Notification aNotification) {
        String serializedNotification =
                NotificationSerializer.instance().serialize(aNotification);

        this.publisher.publish(aNotification.typeName(), serializedNotification);
    }

    private class PhoneNumberProcess {

        private String id;
        private int matchedPhoneNumbers;
        private int totalPhoneNumbers;

        public PhoneNumberProcess() {
            super();

            this.id = UUID.randomUUID().toString().toUpperCase();
            this.matchedPhoneNumbers = -1;
            this.totalPhoneNumbers = -1;
        }

        public boolean isCompleted() {
            return this.matchedPhoneNumbers() >= 0 && this.totalPhoneNumbers() >= 0;
        }

        public String id() {
            return this.id;
        }

        public int matchedPhoneNumbers() {
            return this.matchedPhoneNumbers;
        }

        public void setMatchedPhoneNumbers(int aMatchedPhoneNumbersCount) {
            this.matchedPhoneNumbers = aMatchedPhoneNumbersCount;
        }

        public int totalPhoneNumbers() {
            return this.totalPhoneNumbers;
        }

        public void setTotalPhoneNumbers(int aTotalPhoneNumberCount) {
            this.totalPhoneNumbers = aTotalPhoneNumberCount;
        }
    }

    private class PhoneNumberExecutive extends ExchangeListener {


        private Map<String, PhoneNumberProcess> processes;

        public PhoneNumberExecutive() {
            super();

            this.processes = Collections.synchronizedMap(new HashMap<>());
        }

        public PhoneNumberProcess processOfId(String aProcessId) {
            return this.processes.get(aProcessId);
        }

        public String start(String[] aPhoneNumbers) {

            PhoneNumberProcess process = new PhoneNumberProcess();

            this.processes.put(process.id(), process);

            String allPhoneNumbers = "";

            for (String phoneNumber : aPhoneNumbers) {
                if (!allPhoneNumbers.isEmpty()) {
                    allPhoneNumbers = allPhoneNumbers + "\n";
                }

                allPhoneNumbers = allPhoneNumbers + phoneNumber;
            }

            Notification notification =
                    new Notification(
                            1L,
                            new AllPhoneNumbersListed(
                                    process.id(),
                                    allPhoneNumbers));

            send(notification);

            logger.info("STARTED: {}", process.id());

            return process.id();
        }

        @Override
        protected String exchangeName() {
            return "PhoneNumberExchange";
        }

        @Override
        protected void filteredDispatch(String aType, String aTextMessage) {

            NotificationReader reader = new NotificationReader(aTextMessage);

            String processId = reader.eventStringValue("processId");

            PhoneNumberProcess process = this.processes.get(processId);

            if (reader.typeName().contains("AllPhoneNumbersCounted")) {
                process.setTotalPhoneNumbers(reader.eventIntegerValue("totalPhoneNumbers"));
                logger.debug("AllPhoneNumbersCounted...");
            } else if (reader.typeName().contains("MatchedPhoneNumbersCounted")) {
                process.setMatchedPhoneNumbers(reader.eventIntegerValue("matchedPhoneNumbers"));
                logger.debug("MatchedPhoneNumbersCounted...");
            }

            if (process.isCompleted()) {
               logger.debug(
                        "Process: {}: {} of {} phone numbers found.", process.id(), process.matchedPhoneNumbers(), process.totalPhoneNumbers());
            }
        }

        @Override
        protected String[] listensTo() {
            return new String[] {
                    "com.saasovation.common.port.adapter.messaging.AllPhoneNumbersCounted",
                    "com.saasovation.common.port.adapter.messaging.MatchedPhoneNumbersCounted",
            };
        }

        @Override
        protected String name() {
            return this.getClass().getName();
        }
    }

    private class PhoneNumberFinder extends ExchangeListener {

        public PhoneNumberFinder() {
            super();
        }

        @Override
        protected String exchangeName() {
            return "PhoneNumberExchange";
        }

        @Override
        protected void filteredDispatch(String aType, String aTextMessage) {
            logger.debug("AllPhoneNumbersListed (to match)...");

            NotificationReader reader = new NotificationReader(aTextMessage);

            String allPhoneNumbers = reader.eventStringValue("allPhoneNumbers");

            String[] allPhoneNumbersToSearch = allPhoneNumbers.split("\n");

            String foundPhoneNumbers = "";

            for (String phoneNumber : allPhoneNumbersToSearch) {
                if (phoneNumber.contains("303-")) {
                    if (!foundPhoneNumbers.isEmpty()) {
                        foundPhoneNumbers = foundPhoneNumbers + "\n";
                    }
                    foundPhoneNumbers = foundPhoneNumbers + phoneNumber;
                }
            }

            Notification notification =
                    new Notification(
                            1L,
                            new PhoneNumbersMatched(
                                    reader.eventStringValue("processId"),
                                    foundPhoneNumbers));

            send(notification);
        }

        @Override
        protected String[] listensTo() {
            return new String[] { "com.saasovation.common.port.adapter.messaging.AllPhoneNumbersListed" };
        }

        @Override
        protected String name() {
            return this.getClass().getName();
        }
    }

    private class MatchedPhoneNumberCounter extends ExchangeListener {

        public MatchedPhoneNumberCounter() {
            super();
        }

        @Override
        protected String exchangeName() {
            return "PhoneNumberExchange";
        }

        @Override
        protected void filteredDispatch(String aType, String aTextMessage) {

            logger.debug("PhoneNumbersMatched (to count)...");

            NotificationReader reader = new NotificationReader(aTextMessage);

            String allMatchedPhoneNumbers = reader.eventStringValue("matchedPhoneNumbers");

            String[] allPhoneNumbersToCount = allMatchedPhoneNumbers.split("\n");

            Notification notification =
                    new Notification(
                            1L,
                            new MatchedPhoneNumbersCounted(
                                    reader.eventStringValue("processId"),
                                    allPhoneNumbersToCount.length));

            send(notification);
        }

        @Override
        protected String[] listensTo() {
            return new String[] { "com.saasovation.common.port.adapter.messaging.PhoneNumbersMatched" };
        }

        @Override
        protected String name() {
            return this.getClass().getName();
        }
    }

    private class TotalPhoneNumbersCounter extends ExchangeListener {

        public TotalPhoneNumbersCounter() {
            super();
        }

        @Override
        protected String exchangeName() {
            return "PhoneNumberExchange";
        }

        @Override
        protected void filteredDispatch(String aType, String aTextMessage) {

            logger.debug("AllPhoneNumbersListed (to total)...");

            NotificationReader reader = new NotificationReader(aTextMessage);

            String allPhoneNumbers = reader.eventStringValue("allPhoneNumbers");

            String[] allPhoneNumbersToCount = allPhoneNumbers.split("\n");

            Notification notification =
                    new Notification(
                            1L,
                            new AllPhoneNumbersCounted(
                                    reader.eventStringValue("processId"),
                                    allPhoneNumbersToCount.length));

            send(notification);
        }

        @Override
        protected String[] listensTo() {
            return new String[] { "com.saasovation.common.port.adapter.messaging.AllPhoneNumbersListed" };
        }

        @Override
        protected String name() {
            return this.getClass().getName();
        }
    }
}
