/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.ofbiz.service.jms;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.JNDIContextFactory;
import org.ofbiz.entity.Delegator;
import org.ofbiz.service.GenericServiceException;

/**
 * JmsTopicListener - Topic (Pub/Sub) Message Listener.
 */
public class JmsTopicListener extends AbstractJmsListener {

    private static final Debug.OfbizLogger module = Debug.getOfbizLogger(java.lang.invoke.MethodHandles.lookup().lookupClass());

    private TopicConnection con = null;
    private TopicSession session = null;
    private Topic topic = null;

    private String jndiServer, jndiName, topicName, userName, password;

    /**
     * Creates a new JmsTopicListener - Should only be called by the JmsListenerFactory.
     */
    public JmsTopicListener(Delegator delegator, String jndiServer, String jndiName, String topicName, String userName, String password) {
        super(delegator);
        this.jndiServer = jndiServer;
        this.jndiName = jndiName;
        this.topicName = topicName;
        this.userName = userName;
        this.password = password;
    }

    public void close() throws GenericServiceException {
        try {
            if (session != null)
                session.close();
            if (con != null)
                con.close();
        } catch (JMSException e) {
            throw new GenericServiceException("Cannot close connection(s).", e);
        }
    }

    public synchronized void load() throws GenericServiceException {
        try {
            InitialContext jndi = JNDIContextFactory.getInitialContext(jndiServer);
            TopicConnectionFactory factory = (TopicConnectionFactory) jndi.lookup(jndiName);

            if (factory != null) {
                con = factory.createTopicConnection(userName, password);
                con.setExceptionListener(this);
                session = con.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
                topic = (Topic) jndi.lookup(topicName);
                if (topic != null) {
                    TopicSubscriber subscriber = session.createSubscriber(topic);
                    subscriber.setMessageListener(this);
                    con.start();
                    this.setConnected(true);
                    if (Debug.verboseOn()) Debug.logInfo("Listening to topic [" + topicName + "] on [" + jndiServer + "]...", module);
                } else {
                    throw new GenericServiceException("Topic lookup failed.");
                }
            } else {
                throw new GenericServiceException("Factory (broker) lookup failed.");
            }
        } catch (NamingException ne) {
            throw new GenericServiceException("JNDI lookup problems; listener not running.", ne);
        } catch (JMSException je) {
            throw new GenericServiceException("JMS internal error; listener not running.", je);
        } catch (GeneralException ge) {
            throw new GenericServiceException("Problems with InitialContext; listener not running.", ge);
        }
    }
}
