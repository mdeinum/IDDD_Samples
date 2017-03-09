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

import org.junit.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.saasovation.common.media.OvationsMediaType;
import com.saasovation.identityaccess.domain.model.identity.Tenant;

public class TenantResourceTest extends ResourceTestCase {

    public TenantResourceTest() {
        super();
    }

    @Test
    public void testGetTenant() throws Exception {
        Tenant tenant = this.tenantAggregate();

        String url = "http://localhost:" + PORT + "/tenants/{tenantId}";

        System.out.println(">>> GET: " + url);

        mockMvc.perform(MockMvcRequestBuilders.get(url, tenant.tenantId().id()))
                .andDo(MockMvcResultHandlers.log())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(OvationsMediaType.ID_OVATION_TYPE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", is(tenant.name())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.active", is(true)));

    }
}
