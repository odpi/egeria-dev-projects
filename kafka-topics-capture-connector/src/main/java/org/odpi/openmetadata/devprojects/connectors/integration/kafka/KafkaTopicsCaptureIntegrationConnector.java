/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */

package org.odpi.openmetadata.devprojects.connectors.integration.kafka;


import org.apache.kafka.clients.admin.Admin;
import org.odpi.openmetadata.accessservices.datamanager.metadataelements.TopicElement;
import org.odpi.openmetadata.accessservices.datamanager.properties.TemplateProperties;
import org.odpi.openmetadata.accessservices.datamanager.properties.TopicProperties;
import org.odpi.openmetadata.devprojects.connectors.integration.kafka.ffdc.KafkaTopicsCaptureConnectorAuditCode;
import org.odpi.openmetadata.devprojects.connectors.integration.kafka.ffdc.KafkaTopicsCaptureConnectorErrorCode;
import org.odpi.openmetadata.frameworks.connectors.ffdc.ConnectorCheckedException;
import org.odpi.openmetadata.frameworks.connectors.properties.ConnectionProperties;
import org.odpi.openmetadata.frameworks.connectors.properties.EndpointProperties;
import org.odpi.openmetadata.integrationservices.topic.connector.TopicIntegratorConnector;
import org.odpi.openmetadata.integrationservices.topic.connector.TopicIntegratorContext;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;


/**
 * KafkaTopicsCaptureIntegrationConnector catalogues active topics in a kafka broker.
 */
public class KafkaTopicsCaptureIntegrationConnector extends TopicIntegratorConnector
{
    private String templateQualifiedName = null;
    private String templateGUID = null;
    private String targetRootURL = null;


    /**
     * The context provides the integration connector with access to the open metadata ecosystem.
     */
    private TopicIntegratorContext myContext = null;


    /**
     * Initialize the connector.
     *
     * @param connectorInstanceId - unique id for the connector instance - useful for messages etc
     * @param connectionProperties - POJO for the configuration used to create the connector.
     */
    @Override
    public void initialize(String connectorInstanceId, ConnectionProperties connectionProperties)
    {
        super.initialize(connectorInstanceId, connectionProperties);

        EndpointProperties  endpoint = connectionProperties.getEndpoint();

        if (endpoint != null)
        {
            targetRootURL = endpoint.getAddress();
        }

        Map<String, Object> configurationProperties = connectionProperties.getConfigurationProperties();

        if (configurationProperties != null)
        {
            templateQualifiedName = configurationProperties.get(KafkaTopicsCaptureIntegrationProvider.TEMPLATE_QUALIFIED_NAME_CONFIGURATION_PROPERTY).toString();
        }
    }


    /**
     * Indicates that the connector is completely configured and can begin processing.
     * This call can be used to register with non-blocking services.
     *
     * @throws ConnectorCheckedException there is a problem within the connector.
     */
    @Override
    public void start() throws ConnectorCheckedException
    {
        super.start();

        final String methodName = "start";

        myContext = super.getContext();

        /*
         * Record the configuration
         */
        if (auditLog != null)
        {
            auditLog.logMessage(methodName,
                                KafkaTopicsCaptureConnectorAuditCode.CONNECTOR_CONFIGURATION.getMessageDefinition(connectorName,
                                                                                                                  targetRootURL,
                                                                                                                  templateQualifiedName));
        }

        /*
         * Retrieve the template if one has been requested
         */
        if (templateQualifiedName != null)
        {
            try
            {
                List<TopicElement> templateElements = myContext.getTopicsByName(templateQualifiedName, 0, 0);

                if (templateElements != null)
                {
                    for (TopicElement templateElement : templateElements)
                    {
                        String qualifiedName = templateElement.getProperties().getQualifiedName();

                        if (templateQualifiedName.equals(qualifiedName))
                        {
                            templateGUID = templateElement.getElementHeader().getGUID();
                        }
                    }
                }
            }
            catch (Exception error)
            {
                if (auditLog != null)
                {
                    auditLog.logException(methodName,
                                          KafkaTopicsCaptureConnectorAuditCode.MISSING_TEMPLATE.getMessageDefinition(connectorName, templateQualifiedName),
                                          error);
                }

            }
        }
    }


    /**
     * Requests that the connector does a comparison of the metadata in the third party technology and open metadata repositories.
     * Refresh is called when the integration connector first starts and then at intervals defined in the connector's configuration
     * as well as any external REST API calls to explicitly refresh the connector.
     *
     * This method performs two sweeps. It first retrieves the topics from the event broker (Kafka) and validates that are in the
     * catalog - adding or updating them if necessary. The second sweep is to ensure that all of the topics catalogued
     * actually exist in the event broker.
     *
     * @throws ConnectorCheckedException there is a problem with the connector.  It is not able to refresh the metadata.
     */
    @Override
    public void refresh() throws ConnectorCheckedException
    {
        final String methodName = "refresh";

        try
        {
            /*
             * Retrieve the list of active topics from Kafka.
             */
            Properties properties = new Properties();
            properties.put("bootstrap.servers", targetRootURL);
            Admin            admin            = Admin.create(properties);
            Set<String>      activeTopicNames = admin.listTopics().names().get();
            admin.close();

            if (activeTopicNames != null)
            {
                if (auditLog != null)
                {
                    auditLog.logMessage(methodName,
                                        KafkaTopicsCaptureConnectorAuditCode.RETRIEVED_TOPICS.getMessageDefinition(connectorName,
                                                                                                                   "localhost:9092",
                                                                                                                   Integer.toString(activeTopicNames.size())));
                }

                /*
                 * Retrieve the topics that are catalogued for this event broker.
                 * Remove the topics from the catalog that are no longer present in the event broker.
                 * Remove the names of the topics that are cataloged from the active topic names.
                 * At the end of this loop, the active topic names will just contain the names of the
                 * topics that are not catalogued.
                 */
                int startFrom = 0;
                List<TopicElement> cataloguedTopics = myContext.getMyTopics(startFrom, 0);

                while (cataloguedTopics != null)
                {
                    startFrom = startFrom + cataloguedTopics.size();

                    for (TopicElement topicElement : cataloguedTopics)
                    {
                        String topicName = topicElement.getProperties().getQualifiedName();
                        String topicGUID = topicElement.getElementHeader().getGUID();

                        if (! activeTopicNames.contains(topicName))
                        {
                            /*
                             * The topic no longer exists so delete it from the catalog.
                             */
                            myContext.removeTopic(topicGUID, topicName);

                            if (auditLog != null)
                            {
                                auditLog.logMessage(methodName,
                                                    KafkaTopicsCaptureConnectorAuditCode.TOPIC_DELETED.getMessageDefinition(connectorName,
                                                                                                                            topicName,
                                                                                                                            topicGUID));
                            }
                        }
                        else
                        {
                            activeTopicNames.remove(topicName);
                        }
                    }

                    cataloguedTopics = myContext.getMyTopics(startFrom, 0);
                }


                String topicGUID;

                /*
                 * Add the remaining active topics to the catalog.
                 */
                for (String topicName : activeTopicNames)
                {
                    if (templateGUID == null)
                    {
                        TopicProperties topicProperties = new TopicProperties();

                        topicProperties.setQualifiedName(topicName);
                        topicProperties.setTypeName("KafkaTopic");

                        topicGUID = myContext.createTopic(topicProperties);

                        if (topicGUID != null)
                        {
                            if (auditLog != null)
                            {
                                auditLog.logMessage(methodName,
                                                    KafkaTopicsCaptureConnectorAuditCode.TOPIC_CREATED.getMessageDefinition(connectorName,
                                                                                                                            topicName,
                                                                                                                            topicGUID));
                            }
                        }
                    }
                    else
                    {
                        TemplateProperties templateProperties = new TemplateProperties();

                        templateProperties.setQualifiedName(topicName);

                        topicGUID = myContext.createTopicFromTemplate(templateGUID, templateProperties);

                        if (topicGUID != null)
                        {
                            if (auditLog != null)
                            {
                                auditLog.logMessage(methodName,
                                                    KafkaTopicsCaptureConnectorAuditCode.TOPIC_CREATED_FROM_TEMPLATE.getMessageDefinition(connectorName,
                                                                                                                                          topicName,
                                                                                                                                          topicGUID,
                                                                                                                                          templateQualifiedName,
                                                                                                                                          templateGUID));
                            }
                        }
                    }
                }
            }
        }
        catch (Exception error)
        {
            if (auditLog != null)
            {
                auditLog.logException(methodName,
                                      KafkaTopicsCaptureConnectorAuditCode.UNABLE_TO_RETRIEVE_TOPICS.getMessageDefinition(connectorName,
                                                                                                                          "localhost:9092",
                                                                                                                          error.getClass().getName(),
                                                                                                                          error.getMessage()),
                                      error);


            }

            throw new ConnectorCheckedException(KafkaTopicsCaptureConnectorErrorCode.UNEXPECTED_EXCEPTION.getMessageDefinition(connectorName,
                                                                                                                               error.getClass().getName(),
                                                                                                                               error.getMessage()),
                                                this.getClass().getName(),
                                                methodName,
                                                error);
        }
    }



    /**
     * Shutdown kafka monitoring
     *
     * @throws ConnectorCheckedException something failed in the super class
     */
    @Override
    public void disconnect() throws ConnectorCheckedException
    {
        final String methodName = "disconnect";


        if (auditLog != null)
        {
            auditLog.logMessage(methodName,
                                KafkaTopicsCaptureConnectorAuditCode.CONNECTOR_STOPPING.getMessageDefinition(connectorName));
        }

        super.disconnect();
    }
}
