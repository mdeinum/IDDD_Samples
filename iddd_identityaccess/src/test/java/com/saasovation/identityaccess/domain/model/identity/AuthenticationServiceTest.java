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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.saasovation.identityaccess.domain.model.DomainRegistry;
import com.saasovation.identityaccess.domain.model.IdentityAccessTest;

public class AuthenticationServiceTest extends IdentityAccessTest {

    public AuthenticationServiceTest() {
        super();
    }

    @Test
    public void testAuthenticationSuccess() throws Exception {

        User user = this.userAggregate();

        DomainRegistry
            .userRepository()
            .add(user);

        UserDescriptor userDescriptor =
            DomainRegistry
                .authenticationService()
                .authenticate(
                        user.tenantId(),
                        user.username(),
                        FIXTURE_PASSWORD);

        assertNotNull(userDescriptor);
        assertFalse(userDescriptor.isNullDescriptor());
        assertEquals(userDescriptor.tenantId(), user.tenantId());
        assertEquals(userDescriptor.username(), user.username());
        assertEquals(userDescriptor.emailAddress(), user.person().emailAddress().address());
    }

    public void testAuthenticationTenantFailure() throws Exception {

        User user = this.userAggregate();

        DomainRegistry
            .userRepository()
            .add(user);

        UserDescriptor userDescriptor =
            DomainRegistry
                .authenticationService()
                .authenticate(
                        DomainRegistry.tenantRepository().nextIdentity(),
                        user.username(),
                        FIXTURE_PASSWORD);

        assertNotNull(userDescriptor);
        assertTrue(userDescriptor.isNullDescriptor());
    }

    public void testAuthenticationUsernameFailure() throws Exception {

        User user = this.userAggregate();

        DomainRegistry
            .userRepository()
            .add(user);

        UserDescriptor userDescriptor =
            DomainRegistry
                .authenticationService()
                .authenticate(
                        user.tenantId(),
                        FIXTURE_USERNAME2,
                        user.password());

        assertNotNull(userDescriptor);
        assertTrue(userDescriptor.isNullDescriptor());
    }

    public void testAuthenticationPasswordFailure() throws Exception {

        User user = this.userAggregate();

        DomainRegistry
            .userRepository()
            .add(user);

        UserDescriptor userDescriptor =
            DomainRegistry
                .authenticationService()
                .authenticate(
                        user.tenantId(),
                        user.username(),
                        FIXTURE_PASSWORD + "-");

        assertNotNull(userDescriptor);
        assertTrue(userDescriptor.isNullDescriptor());
    }
}
