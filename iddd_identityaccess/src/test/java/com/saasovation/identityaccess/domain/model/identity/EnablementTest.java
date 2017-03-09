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

package com.saasovation.identityaccess.domain.model.identity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.transaction.event.TransactionalEventListener;

import com.saasovation.identityaccess.domain.model.IdentityAccessTest;

public class EnablementTest extends IdentityAccessTest {

    public EnablementTest() {
        super();
    }

    @Test
    public void testEnablementEnabled() throws Exception {

        Enablement enablement = new Enablement(true, null, null);

        assertTrue(enablement.isEnablementEnabled());
    }

    @Test
    public void testEnablementDisabled() throws Exception {

        Enablement enablement = new Enablement(false, null, null);

        assertFalse(enablement.isEnablementEnabled());
    }

    @Test
    public void testEnablementOutsideStartEndDates() throws Exception {

        Enablement enablement =
            new Enablement(
                    true,
                    this.dayBeforeYesterday(),
                    this.yesterday());

        assertFalse(enablement.isEnablementEnabled());
    }

    @Test
    public void testEnablementUnsequencedDates() throws Exception {

        boolean failure = false;

        try {
            new Enablement(
                    true,
                    this.tomorrow(),
                    this.today());
        } catch (Throwable t) {
            failure = true;
        }

        assertTrue(failure);
    }

    @Test
    public void testEnablementEndsTimeExpired() throws Exception {

        Enablement enablement =
            new Enablement(
                    true,
                    this.dayBeforeYesterday(),
                    this.yesterday());

        assertTrue(enablement.isTimeExpired());
    }

    @Test
    public void testEnablementHasNotBegunTimeExpired() throws Exception {

        Enablement enablement =
            new Enablement(
                    true,
                    this.tomorrow(),
                    this.dayAfterTomorrow());

        assertTrue(enablement.isTimeExpired());
    }
}
