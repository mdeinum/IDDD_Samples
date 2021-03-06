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

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saasovation.common.media.OvationsMediaType;
import com.saasovation.common.serializer.ObjectSerializer;
import com.saasovation.identityaccess.domain.model.identity.Group;

@RestController
@RequestMapping("/tenants/{tenantId}/groups")
public class GroupResource extends AbstractResource {

    public GroupResource() {
        super();
    }

    @GetMapping(value = "{groupName}", produces = OvationsMediaType.ID_OVATION_TYPE)
    public ResponseEntity<String> getGroup(@PathVariable("tenantId") String aTenantId, @PathVariable("groupName") String aGroupName) {

        Group group = this.identityApplicationService().group(aTenantId, aGroupName);

        if (group == null) {
            return ResponseEntity.notFound().build();
        }

        return this.groupResponse(group);
    }

    private ResponseEntity<String> groupResponse(

            Group aGroup) {

        String representation = ObjectSerializer.instance().serialize(aGroup);

        return ResponseEntity.ok().cacheControl(this.cacheControlFor(30)).body(representation);
    }
}
