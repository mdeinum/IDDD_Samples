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

package com.saasovation.collaboration.port.adapter.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.saasovation.collaboration.domain.model.collaborator.Collaborator;
import com.saasovation.collaboration.domain.model.tenant.Tenant;

public class HttpUserInRoleAdapter implements UserInRoleAdapter {

    private static final String HOST = "localhost";
    private static final String PORT = "8081";
    private static final String PROTOCOL = "http";
    private static final String URL_TEMPLATE =
            "/idovation/tenants/{tenantId}/users/{username}/inRole/{role}";

    private final RestTemplate restTemplate = new RestTemplate();

    public HttpUserInRoleAdapter() {
        super();
    }

    public <T extends Collaborator> T toCollaborator(
            Tenant aTenant,
            String anIdentity,
            String aRoleName,
            Class<T> aCollaboratorClass) {

        T collaborator = null;

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(this.buildURLFor(URL_TEMPLATE), String.class, aTenant.id(), anIdentity, aRoleName);


            if (response.getStatusCodeValue() == 200) {
                collaborator =
                    new CollaboratorTranslator()
                        .toCollaboratorFromRepresentation(
                            response.getBody(),
                            aCollaboratorClass);
            } else if (response.getStatusCodeValue() == 204) {
                // not an error, return null
            } else {
                throw new IllegalStateException(
                        "There was a problem requesting the user: "
                        + anIdentity
                        + " in role: "
                        + aRoleName
                        + " with resulting status: "
                        + response.getStatusCode());
            }

        } catch (Throwable t) {
            throw new IllegalStateException(
                    "Failed because: " + t.getMessage(), t);
        }

        return collaborator;
    }

    private String buildURLFor(String aTemplate) {
        String url =
            PROTOCOL
            + "://"
            + HOST + ":" + PORT
            + aTemplate;

        return url;
    }
}
