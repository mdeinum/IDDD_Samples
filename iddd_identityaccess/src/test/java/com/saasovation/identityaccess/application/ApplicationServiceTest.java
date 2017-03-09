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

package com.saasovation.identityaccess.application;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.saasovation.common.domain.model.DomainEventPublisher;
import com.saasovation.common.event.EventStore;
import com.saasovation.common.persistence.CleanableStore;
import com.saasovation.identityaccess.domain.model.DomainRegistry;
import com.saasovation.identityaccess.domain.model.access.Role;
import com.saasovation.identityaccess.domain.model.access.RoleRepository;
import com.saasovation.identityaccess.domain.model.identity.ContactInformation;
import com.saasovation.identityaccess.domain.model.identity.EmailAddress;
import com.saasovation.identityaccess.domain.model.identity.Enablement;
import com.saasovation.identityaccess.domain.model.identity.FullName;
import com.saasovation.identityaccess.domain.model.identity.Group;
import com.saasovation.identityaccess.domain.model.identity.GroupRepository;
import com.saasovation.identityaccess.domain.model.identity.Person;
import com.saasovation.identityaccess.domain.model.identity.PostalAddress;
import com.saasovation.identityaccess.domain.model.identity.RegistrationInvitation;
import com.saasovation.identityaccess.domain.model.identity.Telephone;
import com.saasovation.identityaccess.domain.model.identity.Tenant;
import com.saasovation.identityaccess.domain.model.identity.TenantRepository;
import com.saasovation.identityaccess.domain.model.identity.User;
import com.saasovation.identityaccess.domain.model.identity.UserRepository;

@RunWith(SpringRunner.class)
@ContextConfiguration({"classpath:applicationContext-common.xml",
                              "classpath:applicationContext-identityaccess-application.xml",
                              "classpath:applicationContext-identityaccess-test.xml"})
@TestPropertySource("classpath:/application-test.properties")
@Transactional
public abstract class ApplicationServiceTest {

    protected static final String FIXTURE_GROUP_NAME = "Test Group";
    protected static final String FIXTURE_PASSWORD = "SecretPassword!";
    protected static final String FIXTURE_ROLE_NAME = "Test Role";
    protected static final String FIXTURE_TENANT_DESCRIPTION = "This is a test tenant.";
    protected static final String FIXTURE_TENANT_NAME = "Test Tenant";
    protected static final String FIXTURE_USER_EMAIL_ADDRESS = "jdoe@saasovation.com";
    protected static final String FIXTURE_USER_EMAIL_ADDRESS2 = "zdoe@saasovation.com";
    protected static final String FIXTURE_USERNAME = "jdoe";
    protected static final String FIXTURE_USERNAME2 = "zdoe";

    protected Tenant activeTenant;

    @Autowired
    protected EventStore eventStore;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private RoleRepository roleRepository;

    public ApplicationServiceTest() {
        super();
    }

    protected Group group1Aggregate() {
        return this.tenantAggregate()
                   .provisionGroup(FIXTURE_GROUP_NAME + " 1", "A test group 1.");
    }

    protected Group group2Aggregate() {
        return this.tenantAggregate()
                   .provisionGroup(FIXTURE_GROUP_NAME + " 2", "A test group 2.");
    }

    protected Role roleAggregate() {
        return this.tenantAggregate()
                   .provisionRole(FIXTURE_ROLE_NAME, "A test role.", true);
    }

    protected Tenant tenantAggregate() {
        if (activeTenant == null) {

            activeTenant =
                    DomainRegistry
                        .tenantProvisioningService()
                        .provisionTenant(
                                FIXTURE_TENANT_NAME,
                                FIXTURE_TENANT_DESCRIPTION,
                                new FullName("John", "Doe"),
                                new EmailAddress(FIXTURE_USER_EMAIL_ADDRESS),
                                new PostalAddress(
                                        "123 Pearl Street",
                                        "Boulder",
                                        "CO",
                                        "80301",
                                        "US"),
                                new Telephone("303-555-1210"),
                                new Telephone("303-555-1212"));
        }

        return activeTenant;
    }

    protected User userAggregate() {

        Tenant tenant = this.tenantAggregate();

        RegistrationInvitation invitation =
                tenant.offerRegistrationInvitation("open-ended").openEnded();

        User user =
                tenant.registerUser(
                        invitation.invitationId(),
                        "jdoe",
                        FIXTURE_PASSWORD,
                        Enablement.indefiniteEnablement(),
                        new Person(
                                tenant.tenantId(),
                                new FullName("John", "Doe"),
                                new ContactInformation(
                                        new EmailAddress(FIXTURE_USER_EMAIL_ADDRESS),
                                        new PostalAddress(
                                                "123 Pearl Street",
                                                "Boulder",
                                                "CO",
                                                "80301",
                                                "US"),
                                        new Telephone("303-555-1210"),
                                        new Telephone("303-555-1212"))));

        return user;
    }

    @Before
    public void setUp() throws Exception {
        DomainEventPublisher.instance().reset();
        cleanUp();
    }

    private void cleanUp() {
        this.clean((CleanableStore) this.eventStore);
        this.clean((CleanableStore) this.groupRepository);
        this.clean((CleanableStore) this.roleRepository);
        this.clean((CleanableStore) this.tenantRepository);
        this.clean((CleanableStore) this.userRepository);

    }

    @After
    public void tearDown() throws Exception {
        cleanUp();
    }

    private void clean(CleanableStore aCleanableStore) {
        aCleanableStore.clean();
    }
}
