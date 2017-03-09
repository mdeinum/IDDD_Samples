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

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

import org.springframework.http.CacheControl;

import com.saasovation.identityaccess.application.AccessApplicationService;
import com.saasovation.identityaccess.application.ApplicationServiceRegistry;
import com.saasovation.identityaccess.application.IdentityApplicationService;
import com.saasovation.identityaccess.application.NotificationApplicationService;
import com.saasovation.identityaccess.domain.model.identity.User;

public class AbstractResource {

    public AbstractResource() {
        super();
    }

    protected AccessApplicationService accessApplicationService() {
        return ApplicationServiceRegistry.accessApplicationService();
    }

    protected CacheControl cacheControlFor(int aNumberOfSeconds) {
        return CacheControl.maxAge(30, TimeUnit.SECONDS);
    }

    protected IdentityApplicationService identityApplicationService() {
        return ApplicationServiceRegistry.identityApplicationService();
    }

    protected NotificationApplicationService notificationApplicationService() {
        return ApplicationServiceRegistry.notificationApplicationService();
    }

    protected String userETag(User aUser) {


        int hashCode = aUser.hashCode() + aUser.person().hashCode();

        try {
            // change this algorithm as needed
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(Integer.toString(hashCode).getBytes("UTF-8"));
            BigInteger digestValue = new BigInteger(1, messageDigest.digest());
            String strongHash = digestValue.toString(16);

            return strongHash;

        } catch (Throwable t) {
            return Integer.toString(hashCode);
        }
    }
}
