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

package com.saasovation.common;

import junit.framework.TestCase;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.saasovation.common.domain.model.DomainEventPublisher;
import com.saasovation.common.spring.SpringHibernateSessionProvider;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:applicationContext-common.xml")
@Transactional
public abstract class CommonTestCase {

    @Autowired
    protected SpringHibernateSessionProvider sessionProvider;

    public CommonTestCase() {
        super();
    }

    protected Session session() {
        return this.sessionProvider.session();
    }

    @Before
    public void setUp() throws Exception {

        DomainEventPublisher.instance().reset();
    }

}
