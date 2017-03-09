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

package com.saasovation.identityaccess.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.saasovation.common.media.RepresentationReader;
import com.saasovation.identityaccess.domain.model.DomainRegistry;
import com.saasovation.identityaccess.domain.model.access.Role;
import com.saasovation.identityaccess.domain.model.identity.User;

public class UserResourceTest extends ResourceTestCase {

    public UserResourceTest() {
        super();
    }

    @Test
    public void testGetAuthenticUser() throws Exception {
        User user = this.userAggregate();
        DomainRegistry.userRepository().add(user);

        String url = "http://localhost:" + PORT + "/tenants/{tenantId}/users/{username}/autenticatedWith/{password}";

        System.out.println(">>> GET: " + url);

        String output = mockMvc.perform(MockMvcRequestBuilders.get(url, user.tenantId().id(), user.username(), FIXTURE_PASSWORD))
                .andDo(log())
                .andReturn().getResponse().getContentAsString();

        RepresentationReader reader = new RepresentationReader(output);

        assertEquals(user.tenantId().id(), reader.stringValue("tenantId.id"));
        assertEquals(user.username(), reader.stringValue("username"));
        assertEquals(user.person().emailAddress().address(), reader.stringValue("emailAddress"));
    }

    @Test
    public void testGetAuthenticUserWrongPassword() throws Exception {
        User user = this.userAggregate();
        DomainRegistry.userRepository().add(user);

        String url = "http://localhost:" + PORT + "/tenants/{tenantId}/users/{username}/autenticatedWith/{password}";

        System.out.println(">>> GET: " + url);

         mockMvc.perform(MockMvcRequestBuilders.get(url, user.tenantId().id(), user.username(), UUID.randomUUID().toString()))
                .andDo(log())
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetUser() throws Exception {
        User user = this.userAggregate();
        DomainRegistry.userRepository().add(user);

        String url = "http://localhost:" + PORT + "/tenants/{tenantId}/users/{username}";

        System.out.println(">>> GET: " + url);

        String entity = mockMvc.perform(MockMvcRequestBuilders.get(url, user.tenantId().id(), user.username()))
                .andDo(log())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        RepresentationReader reader = new RepresentationReader(entity);
        assertEquals(user.username(), reader.stringValue("username"));
        assertTrue(reader.booleanValue("enabled"));
    }

    @Test
    public void testGetNonExistingUser() throws Exception {
        User user = this.userAggregate();
        DomainRegistry.userRepository().add(user);

        String url = "http://localhost:" + PORT + "/tenants/{tenantId}/users/{username}";

        System.out.println(">>> GET: " + url);
        mockMvc.perform(MockMvcRequestBuilders.get(url, user.tenantId().id(), user.username() + "!"))
                .andDo(log())
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

    }

    @Test
    public void testIsUserInRole() throws Exception {
        User user = this.userAggregate();
        DomainRegistry.userRepository().add(user);

        Role role = this.roleAggregate();
        role.assignUser(user);
        DomainRegistry.roleRepository().add(role);

        String url = "http://localhost:" + PORT + "/tenants/{tenantId}/users/{username}/inRole/{role}";

        System.out.println(">>> GET: " + url);

        String entity = mockMvc.perform(MockMvcRequestBuilders.get(url, user.tenantId().id(), user.username(), role.name()))
                .andDo(log())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        RepresentationReader reader = new RepresentationReader(entity);
        assertEquals(user.username(),  reader.stringValue("username"));
        assertEquals(role.name(), reader.stringValue("role"));
    }

    @Test
    public void testIsUserNotInRole() throws Exception {
        User user = this.userAggregate();
        DomainRegistry.userRepository().add(user);

        Role role = this.roleAggregate();
        DomainRegistry.roleRepository().add(role);

        String url = "http://localhost:" + PORT + "/tenants/{tenantId}/users/{username}/inRole/{role}";

        System.out.println(">>> GET: " + url);

        mockMvc.perform(MockMvcRequestBuilders.get(url, user.tenantId().id(), user.username(), role.name()))
                .andDo(log())
                .andExpect(status().isNoContent());
    }
}
