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

package com.saasovation.identityaccess.domain.model;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.saasovation.identityaccess.domain.model.access.AuthorizationService;
import com.saasovation.identityaccess.domain.model.access.RoleRepository;
import com.saasovation.identityaccess.domain.model.identity.AuthenticationService;
import com.saasovation.identityaccess.domain.model.identity.EncryptionService;
import com.saasovation.identityaccess.domain.model.identity.GroupMemberService;
import com.saasovation.identityaccess.domain.model.identity.GroupRepository;
import com.saasovation.identityaccess.domain.model.identity.PasswordService;
import com.saasovation.identityaccess.domain.model.identity.TenantProvisioningService;
import com.saasovation.identityaccess.domain.model.identity.TenantRepository;
import com.saasovation.identityaccess.domain.model.identity.UserRepository;

public class DomainRegistry implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    public static AuthenticationService authenticationService() {
        return applicationContext.getBean(AuthenticationService.class);
    }

    public static AuthorizationService authorizationService() {
        return applicationContext.getBean(AuthorizationService.class);
    }

    public static EncryptionService encryptionService() {
        return applicationContext.getBean(EncryptionService.class);
    }

    public static GroupMemberService groupMemberService() {
        return applicationContext.getBean(GroupMemberService.class);
    }

    public static GroupRepository groupRepository() {
        return applicationContext.getBean(GroupRepository.class);
    }

    public static PasswordService passwordService() {
        return applicationContext.getBean(PasswordService.class);
    }

    public static RoleRepository roleRepository() {
        return applicationContext.getBean(RoleRepository.class);
    }

    public static TenantProvisioningService tenantProvisioningService() {
        return applicationContext.getBean(TenantProvisioningService.class);
    }

    public static TenantRepository tenantRepository() {
        return applicationContext.getBean(TenantRepository.class);
    }

    public static UserRepository userRepository() {
        return applicationContext.getBean(UserRepository.class);
    }

    @Override
    public synchronized void setApplicationContext(ApplicationContext anApplicationContext) throws BeansException {

        DomainRegistry.applicationContext = anApplicationContext;
    }
}
