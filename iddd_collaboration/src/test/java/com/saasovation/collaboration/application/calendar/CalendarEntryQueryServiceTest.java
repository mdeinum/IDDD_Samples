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

package com.saasovation.collaboration.application.calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Date;

import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;

import com.saasovation.collaboration.application.ApplicationTest;
import com.saasovation.collaboration.application.calendar.data.CalendarEntryData;
import com.saasovation.collaboration.domain.model.DomainRegistry;
import com.saasovation.collaboration.domain.model.calendar.CalendarEntry;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class CalendarEntryQueryServiceTest extends ApplicationTest {

    public CalendarEntryQueryServiceTest() {
        super();
    }

    @Test
    public void testCalendarEntryDataOfId() throws Exception {

        CalendarEntry calendarEntry = this.calendarEntryAggregate();

        DomainRegistry.calendarEntryRepository().save(calendarEntry);

        CalendarEntryData calendarEntryData =
                calendarEntryQueryService
                    .calendarEntryDataOfId(
                            calendarEntry.tenant().id(),
                            calendarEntry.calendarEntryId().id());

        assertNotNull(calendarEntryData);
        assertNotNull(calendarEntryData.getAlarmAlarmUnitsType());
        assertNotNull(calendarEntryData.getCalendarEntryId());
        assertNotNull(calendarEntryData.getCalendarId());
        assertEquals(calendarEntry.calendarId().id(), calendarEntryData.getCalendarId());
        assertNotNull(calendarEntryData.getDescription());
        assertNotNull(calendarEntryData.getLocation());
        assertNotNull(calendarEntryData.getOwnerEmailAddress());
        assertNotNull(calendarEntryData.getOwnerIdentity());
        assertNotNull(calendarEntryData.getOwnerName());
        assertNotNull(calendarEntryData.getRepetitionType());
        assertEquals(calendarEntry.tenant().id(), calendarEntryData.getTenantId());
        assertNotNull(calendarEntryData.getInvitees());
        assertTrue(calendarEntryData.getInvitees().isEmpty());
    }

    @Test
    public void testCalendarEntryDataOfCalendarId() throws Exception {

        CalendarEntry[] calendarEntries = this.calendarEntryAggregates();

        for (CalendarEntry calendarEntry : calendarEntries) {
            DomainRegistry.calendarEntryRepository().save(calendarEntry);
        }

        Collection<CalendarEntryData> queriedCalendarEntries =
                calendarEntryQueryService
                    .calendarEntryDataOfCalendarId(
                            calendarEntries[0].tenant().id(),
                            calendarEntries[0].calendarId().id());

        assertNotNull(queriedCalendarEntries);
        assertFalse(queriedCalendarEntries.isEmpty());
        assertEquals(calendarEntries.length, queriedCalendarEntries.size());

        for (CalendarEntryData calendarEntryData : queriedCalendarEntries) {
            assertNotNull(calendarEntryData);
            assertNotNull(calendarEntryData.getAlarmAlarmUnitsType());
            assertNotNull(calendarEntryData.getCalendarEntryId());
            assertNotNull(calendarEntryData.getCalendarId());
            assertEquals(calendarEntries[0].calendarId().id(), calendarEntryData.getCalendarId());
            assertNotNull(calendarEntryData.getDescription());
            assertNotNull(calendarEntryData.getLocation());
            assertNotNull(calendarEntryData.getOwnerEmailAddress());
            assertNotNull(calendarEntryData.getOwnerIdentity());
            assertNotNull(calendarEntryData.getOwnerName());
            assertNotNull(calendarEntryData.getRepetitionType());
            assertEquals(calendarEntries[0].tenant().id(), calendarEntryData.getTenantId());
            assertNotNull(calendarEntryData.getInvitees());
            assertFalse(calendarEntryData.getInvitees().isEmpty());
        }
    }

    @Test
    public void testTimeSpanningCalendarEntries() throws Exception {

        CalendarEntry[] calendarEntries = this.calendarEntryAggregates();

        assertEquals(3, calendarEntries.length);

        for (CalendarEntry calendarEntry : calendarEntries) {
            DomainRegistry.calendarEntryRepository().save(calendarEntry);
        }

        Date earliestDate = new Date();
        Date latestDate = earliestDate;

        for (CalendarEntry calendarEntry : calendarEntries) {
            if (calendarEntry.timeSpan().begins().before(earliestDate)) {
                earliestDate = calendarEntry.timeSpan().begins();
            }

            if (calendarEntry.timeSpan().ends().after(latestDate)) {
                latestDate = calendarEntry.timeSpan().ends();
            }

            Collection<CalendarEntryData> queriedCalendarEntries =
                    calendarEntryQueryService
                        .timeSpanningCalendarEntries(
                                calendarEntry.tenant().id(),
                                calendarEntry.calendarId().id(),
                                this.beginningOfDay(calendarEntry.timeSpan().begins()),
                                this.endOfDay(calendarEntry.timeSpan().ends()));

            assertNotNull(queriedCalendarEntries);
            assertFalse(queriedCalendarEntries.isEmpty());
            assertEquals(1, queriedCalendarEntries.size());

            CalendarEntryData calendarEntryData = queriedCalendarEntries.iterator().next();

            assertNotNull(calendarEntryData.getAlarmAlarmUnitsType());
            assertNotNull(calendarEntryData.getCalendarEntryId());
            assertNotNull(calendarEntryData.getCalendarId());
            assertEquals(calendarEntries[0].calendarId().id(), calendarEntryData.getCalendarId());
            assertNotNull(calendarEntryData.getDescription());
            assertNotNull(calendarEntryData.getLocation());
            assertNotNull(calendarEntryData.getOwnerEmailAddress());
            assertNotNull(calendarEntryData.getOwnerIdentity());
            assertNotNull(calendarEntryData.getOwnerName());
            assertNotNull(calendarEntryData.getRepetitionType());
        }

        Collection<CalendarEntryData> queriedCalendarEntries =
                calendarEntryQueryService
                    .timeSpanningCalendarEntries(
                            calendarEntries[0].tenant().id(),
                            calendarEntries[0].calendarId().id(),
                            this.beginningOfDay(earliestDate),
                            this.endOfDay(latestDate));

        assertNotNull(queriedCalendarEntries);
        assertFalse(queriedCalendarEntries.isEmpty());
        assertEquals(3, queriedCalendarEntries.size());
    }
}
