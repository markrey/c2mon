/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.daq.common.messaging.impl;

import cern.c2mon.daq.common.conf.core.ProcessConfigurationHolder;
import cern.c2mon.daq.common.messaging.ProcessRequestSender;
import cern.c2mon.daq.config.DaqProperties;
import cern.c2mon.shared.common.process.ProcessConfiguration;
import cern.c2mon.shared.daq.process.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;

import javax.jms.*;

/**
 * Implementation of ProcessRequestSender interface for ActiveMQ JMS middleware.
 *
 * @author mbrightw
 * @author vilches (refactoring updates)
 */
public class ActiveRequestSender implements ProcessRequestSender {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ActiveRequestSender.class);

  /**
   * Constant of the PIK request time out
   */
  private static final long PIK_REQUEST_TIMEOUT = 5000;

  /**
   * Reference to the JmsTemplate used to send messages to the server
   * (instantiated in Spring XML).
   */
  private JmsTemplate jmsTemplate;

  /**
   * ProcessMessageConverter helper class (fromMessage/ToMessage)
   */
  private ProcessMessageConverter processMessageConverter;

  private DaqProperties properties;

  @Autowired
  public ActiveRequestSender(final DaqProperties properties, final JmsTemplate processRequestJmsTemplate) {
    this.properties = properties;
    this.jmsTemplate = processRequestJmsTemplate;
    this.processMessageConverter = new ProcessMessageConverter();
  }


  @Override
  public ProcessConfigurationResponse sendProcessConfigurationRequest(final String processName) {
    LOGGER.debug("sendProcessConfigurationRequest - Sending Process Configuration request to server.");
    // use of JmsTemplate here means exceptions are caught by Spring and
    // converted!
    // JMS template NOT used for reply as need to use same connection &
    // session
    final Destination requestDestination = jmsTemplate.getDefaultDestination();
    ProcessConfigurationResponse processConfigurationResponse = (ProcessConfigurationResponse) jmsTemplate.execute(new SessionCallback<Object>() {
      public Object doInJms(final Session session) throws JMSException {
        TemporaryQueue replyQueue = session.createTemporaryQueue();

        ProcessConfigurationRequest processConfigurationRequest = new ProcessConfigurationRequest(processName);
        processConfigurationRequest.setprocessPIK(ProcessConfigurationHolder.getInstance().getprocessPIK());

        Message message = processMessageConverter.toMessage(processConfigurationRequest, session);
        message.setJMSReplyTo(replyQueue);
        MessageProducer messageProducer = session.createProducer(requestDestination);
        try {
          Long requestTimeout = properties.getServerRequestTimeout();
          messageProducer.setTimeToLive(requestTimeout);
          messageProducer.send(message);

          // wait for reply (receive timeout is set in XML)
          MessageConsumer consumer = session.createConsumer(replyQueue);
          try {
            Message replyMessage = consumer.receive(requestTimeout);
            if (replyMessage == null) {
              return null;
            } else {
              return processMessageConverter.fromMessage(replyMessage);
            }
          } finally {
            consumer.close();
          }
        } finally {
          messageProducer.close();
        }
      }
    }, true); // start the connection for receiving messages

    // Can be null if there is a TimeOut
    return processConfigurationResponse;
  }

  @Override
  public ProcessConnectionResponse sendProcessConnectionRequest(final String processName) {
    LOGGER.debug("sendProcessConnectionRequest - Sending Process Connection Request to server.");
    // use of JmsTemplate here means exceptions are caught by Spring and
    // converted!
    // JMS template NOT used for reply as need to use same connection &
    // session
    final Destination requestDestination = jmsTemplate.getDefaultDestination();

    ProcessConnectionResponse processConnectionResponse = (ProcessConnectionResponse) jmsTemplate.execute(new SessionCallback<Object>() {
      public Object doInJms(final Session session) throws JMSException {
        TemporaryQueue replyQueue = session.createTemporaryQueue();
        // Process PIK Request
        ProcessConnectionRequest processConnectionRequest = new ProcessConnectionRequest(processName);
//        configurationController.setStartUp(processConnectionRequest.getProcessStartupTime().getTime());

        Message message = processMessageConverter.toMessage(processConnectionRequest, session);
        message.setJMSReplyTo(replyQueue);
        MessageProducer messageProducer = session.createProducer(requestDestination);
        try {
          // TimeOut (too long for the PIK) 12000
          //messageProducer.setTimeToLive(commonConfiguration.getRequestTimeout());
          messageProducer.setTimeToLive(PIK_REQUEST_TIMEOUT);
          messageProducer.send(message);

          // If there is a timeout SystemExit

          // wait for reply (receive timeout is set in XML) -> 12000
          MessageConsumer consumer = session.createConsumer(replyQueue);
          try {
            //Message replyMessage = consumer.receive(commonConfiguration.getRequestTimeout());
            Message replyMessage = consumer.receive(PIK_REQUEST_TIMEOUT);
            if (replyMessage == null) {
              return null;
            } else {
              // Convert the XML and return it as a ProcessConnectionRespond object
              return processMessageConverter.fromMessage(replyMessage);
            }
          } finally {
            consumer.close();
          }
        } finally {
          messageProducer.close();
        }
      }
    }, true); // start the connection for receiving PIK messages

    // Can be null if there is a TimeOut
    return processConnectionResponse;
  }

  @Override
  public void sendProcessDisconnectionRequest(ProcessConfiguration processConfiguration, long startupTime) {
    LOGGER.debug("sendProcessDisconnectionRequest - Sending Process Disconnection notification to server.");
//    ProcessConfiguration processConfiguration = this.configurationController.getProcessConfiguration();
    ProcessDisconnectionRequest processDisconnectionRequest;

    // processConfiguration set up for ProcessDisconnectionRequest compatibility

    // ID
    if (processConfiguration.getProcessID() == null) {
      processConfiguration.setProcessID(ProcessDisconnectionRequest.NO_ID);
    }
    // Name
    if (processConfiguration.getProcessName() == null) {
      processConfiguration.setProcessName(ProcessDisconnectionRequest.NO_PROCESS);
    }
    // PIK
    if (processConfiguration.getprocessPIK() == null) {
      processConfiguration.setprocessPIK(ProcessDisconnectionRequest.NO_PIK);
    }

    // We don't care if there is NO_PIK or NO_PROCESS. The server will take care
    if (processConfiguration.getProcessID() != ProcessDisconnectionRequest.NO_ID) {
      processDisconnectionRequest = new ProcessDisconnectionRequest(processConfiguration.getProcessID(), processConfiguration.getProcessName(),
          processConfiguration.getprocessPIK(), startupTime);
    } else {
      processDisconnectionRequest = new ProcessDisconnectionRequest(processConfiguration.getProcessName(), processConfiguration.getprocessPIK(),
          startupTime);
    }

    LOGGER.trace("sendProcessDisconnectionRequest - Converting and sending disconnection message");

    jmsTemplate.setMessageConverter(this.processMessageConverter);
    jmsTemplate.convertAndSend(processDisconnectionRequest);

    LOGGER.trace("sendProcessDisconnectionRequest - Process Disconnection for " + processConfiguration.getProcessName() + " sent");
  }

  /**
   * Useful for overwriting the default JmsTemplate that is wired in a start-up
   * (for using a different connection for instance).
   *
   * @param jmsTemplate the jmsTemplate to set
   */
  public void setJmsTemplate(JmsTemplate jmsTemplate) {
    this.jmsTemplate = jmsTemplate;
  }
}
