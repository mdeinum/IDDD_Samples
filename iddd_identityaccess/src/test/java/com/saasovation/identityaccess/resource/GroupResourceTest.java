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

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.Test;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.saasovation.common.media.OvationsMediaType;
import com.saasovation.identityaccess.domain.model.DomainRegistry;
import com.saasovation.identityaccess.domain.model.identity.Group;

public class GroupResourceTest extends ResourceTestCase {

    public GroupResourceTest() {
        super();
    }

    @Test
    public void testGetGroup() throws Exception {
        Group group = this.group1Aggregate();
        DomainRegistry.groupRepository().add(group);

        String url = "http://localhost:" + PORT + "/tenants/{tenantId}/groups/{groupName}";

        mockMvc.perform(get(url, group.tenantId().id(), group.name()))
                .andDo(log())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(OvationsMediaType.ID_OVATION_TYPE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId.id", is(group.tenantId().id())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", is(group.name())));

//        System.out.println(">>> GET: " + url);
//        ClientRequest request = new ClientRequest(url);
//        request.pathParameter("tenantId", group.tenantId().id());
//        request.pathParameter("groupName", group.name());
//        String output = request.getTarget(String.class);
//        System.out.println(output);
//
//        RepresentationReader reader = new RepresentationReader(output);
//
//        assertEquals(group.tenantId().id(), reader.stringValue("tenantId.id"));
//        assertEquals(group.name(), reader.stringValue("name"));
    }
}
