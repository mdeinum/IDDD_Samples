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
import com.saasovation.identityaccess.application.command.AuthenticateUserCommand;
import com.saasovation.identityaccess.application.representation.UserInRoleRepresentation;
import com.saasovation.identityaccess.application.representation.UserRepresentation;
import com.saasovation.identityaccess.domain.model.identity.User;
import com.saasovation.identityaccess.domain.model.identity.UserDescriptor;

@RestController
@RequestMapping("/tenants/{tenantId}/users")
public class UserResource extends AbstractResource {

    public UserResource() {
        super();
    }

    @GetMapping(value = "{username}/autenticatedWith/{password}", produces = OvationsMediaType.ID_OVATION_TYPE)
    public ResponseEntity<String> getAuthenticUser(
            @PathVariable("tenantId") String aTenantId,
            @PathVariable("username") String aUsername,
            @PathVariable("password") String aPassword) {

        UserDescriptor userDescriptor =
                this.identityApplicationService()
                    .authenticateUser(
                            new AuthenticateUserCommand(
                                    aTenantId,
                                    aUsername,
                                    aPassword));

        if (userDescriptor.isNullDescriptor()) {
            return ResponseEntity.notFound().build();
        }

        return this.userDescriptorResponse(userDescriptor);
    }

    @GetMapping(value = "{username}", produces = OvationsMediaType.ID_OVATION_TYPE)
    public ResponseEntity<String> getUser(
            @PathVariable("tenantId") String aTenantId,
            @PathVariable("username") String aUsername) {

        User user = this.identityApplicationService().user(aTenantId, aUsername);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return this.userResponse(user);
    }

    @GetMapping(value = "{username}/inRole/{role}", produces =OvationsMediaType.ID_OVATION_TYPE )
    public ResponseEntity<String> getUserInRole(
            @PathVariable("tenantId") String aTenantId,
            @PathVariable("username") String aUsername,
            @PathVariable("role") String aRoleName) {

        ResponseEntity<String> response;

        User user = null;

        try {
            user = this.accessApplicationService()
                       .userInRole(
                               aTenantId,
                               aUsername,
                               aRoleName);
        } catch (Exception e) {
            // fall through
        }

        if (user != null) {
            response = this.userInRoleResponse(user, aRoleName);
        } else {
            response = ResponseEntity.noContent().build();
        }

        return response;
    }

    private ResponseEntity<String> userDescriptorResponse(UserDescriptor aUserDescriptor) {

        String representation = ObjectSerializer.instance().serialize(aUserDescriptor);
        return
                ResponseEntity.ok()
                        .cacheControl(this.cacheControlFor(30))
                        .body(representation);
    }

    private ResponseEntity<String> userInRoleResponse(User aUser, String aRoleName) {

        UserInRoleRepresentation userInRoleRepresentation =
                new UserInRoleRepresentation(aUser, aRoleName);

        String representation =
                ObjectSerializer
                    .instance()
                    .serialize(userInRoleRepresentation);

        return
                ResponseEntity.ok()
                        .cacheControl(this.cacheControlFor(60))
                        .body(representation);
    }

    private ResponseEntity<String> userResponse(User aUser) {

        String eTag = this.userETag(aUser);


        ResponseEntity<String> response;
//        ResponseBuilder conditionalBuilder = aRequest.evaluatePreconditions(eTag);
//
//        if (conditionalBuilder != null) {
//            response =
//                    conditionalBuilder
//                        .cacheControl(this.cacheControlFor(3600))
//                        .tag(eTag)
//                        .build();
//        } else {
            String representation =
                    ObjectSerializer
                        .instance()
                        .serialize(new UserRepresentation(aUser));

            response =
                    ResponseEntity.ok()
                        .cacheControl(this.cacheControlFor(3600))
                        .eTag(eTag)
                        .body(representation);
//        }

        return response;
    }
}
