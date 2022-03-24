/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */

package org.odpi.openmetadata.devprojects.connectors.integration.egeria;


import org.apache.kafka.clients.admin.Admin;
import org.odpi.openmetadata.accessservices.itinfrastructure.metadataelements.DataAssetElement;
import org.odpi.openmetadata.accessservices.itinfrastructure.metadataelements.EndpointElement;
import org.odpi.openmetadata.accessservices.itinfrastructure.metadataelements.SoftwareServerPlatformElement;
import org.odpi.openmetadata.accessservices.itinfrastructure.properties.ServerAssetUseProperties;
import org.odpi.openmetadata.accessservices.itinfrastructure.properties.ServerAssetUseType;
import org.odpi.openmetadata.devprojects.connectors.integration.egeria.ffdc.EgeriaInfrastructureConnectorAuditCode;
import org.odpi.openmetadata.devprojects.connectors.integration.egeria.ffdc.EgeriaInfrastructureConnectorErrorCode;
import org.odpi.openmetadata.frameworks.auditlog.ComponentDevelopmentStatus;
import org.odpi.openmetadata.frameworks.connectors.ffdc.ConnectorCheckedException;

import java.util.List;
import java.util.Properties;
import java.util.Set;


/**
 * KafkaTopicsAuditIntegrationConnector catalogues active topics in a kafka broker.
 */
public class KafkaTopicsAuditIntegrationConnector extends EgeriaInfrastructureIntegrationConnectorBase
{

    private static final String topicTypeName           = "KafkaTopic";
    private static final String eventBrokerPlatformName = "Apache Kafka Server";


    public KafkaTopicsAuditIntegrationConnector()
    {
        super.platformName= eventBrokerPlatformName;
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

        /*
         * Record the configuration
         */
        if (auditLog != null)
        {
            auditLog.logMessage(methodName,
                                EgeriaInfrastructureConnectorAuditCode.KAFKA_CONNECTOR_START.getMessageDefinition(connectorName, monitoredPlatforms.toString()));
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
             * The monitored platforms list is built up from the platforms catalogued in open metadata.  It is possible that the endpoint is
             * not known - or the platform is not an OMAG Server Platform.
             */
            for (PlatformDetails platformDetails : monitoredPlatforms)
            {
                SoftwareServerPlatformElement element = super.getContext().getSoftwareServerPlatformByGUID(platformDetails.platformGUID);

                if (element != null)
                {
                    if (eventBrokerPlatformName.equals(element.getProperties().getSoftwareServerPlatformType()))
                    {
                        platformDetails.platformQualifiedName = element.getProperties().getQualifiedName();
                        platformDetails.platformDisplayName = element.getProperties().getDisplayName();

                        List<EndpointElement> endpoints = super.getContext().getEndpointsForInfrastructure(platformDetails.platformGUID, 0, 0);

                        if (endpoints != null)
                        {
                            for (EndpointElement endpoint : endpoints)
                            {
                                if (endpoint.getEndpointProperties().getAddress() != null)
                                {
                                    /*
                                     * Retrieve the list of active topics from Kafka.
                                     */
                                    Properties properties = new Properties();
                                    properties.put("bootstrap.servers", endpoint.getEndpointProperties().getAddress());

                                    Admin            admin            = Admin.create(properties);
                                    Set<String>      activeTopicNames = admin.listTopics().names().get();
                                    admin.close();

                                    if (activeTopicNames != null)
                                    {
                                        platformDetails.platformRootURL = endpoint.getEndpointProperties().getAddress();

                                        if (platformDetails.capabilityGUID == null)
                                        {
                                            platformDetails.capabilityGUID = super.createSoftwareService(platformDetails.platformGUID,
                                                                                                         platformDetails.platformQualifiedName,
                                                                                                         "SoftwareService",
                                                                                                         99,
                                                                                                         "Apache Kafka Topic Manager",
                                                                                                         "Event Broker",
                                                                                                         ComponentDevelopmentStatus.STABLE,
                                                                                                         "kafka",
                                                                                                         "https://kafka.apache.org/");
                                        }

                                        for (String topicName : activeTopicNames)
                                        {
                                            if (topicName != null)
                                            {
                                                DataAssetElement cataloguedTopic = null;
                                                List<DataAssetElement> cataloguedAssets = super.getContext().getDataAssetsByName(topicName, null, 0, 0);

                                                /*
                                                 * Need to find the kafka topic asset.
                                                 */
                                                if (cataloguedAssets != null)
                                                {
                                                    for (DataAssetElement cataloguedAsset : cataloguedAssets)
                                                    {
                                                        String elementTypeName = cataloguedAsset.getElementHeader().getType().getTypeName();
                                                        List<String> elementSuperTypes = cataloguedAsset.getElementHeader().getType().getSuperTypeNames();
                                                        if ((topicTypeName.equals(elementTypeName)) || ((elementSuperTypes != null) && (elementSuperTypes.contains(topicTypeName))))
                                                        {
                                                            cataloguedTopic = cataloguedAsset;
                                                            break;
                                                        }

                                                    }
                                                }

                                                if (cataloguedTopic == null)
                                                {
                                                    if (auditLog != null)
                                                    {
                                                        auditLog.logMessage(methodName,
                                                                            EgeriaInfrastructureConnectorAuditCode.UNKNOWN_TOPIC.getMessageDefinition(connectorName,
                                                                                                                                                      topicName,
                                                                                                                                                      endpoint.getEndpointProperties().getAddress()));
                                                    }
                                                }
                                                else
                                                {
                                                    ServerAssetUseProperties serverAssetUseProperties = new ServerAssetUseProperties();

                                                    serverAssetUseProperties.setUseType(ServerAssetUseType.OWNS);
                                                    super.getContext().createServerAssetUse(platformDetails.capabilityGUID, cataloguedTopic.getElementHeader().getGUID(), serverAssetUseProperties);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else
                {
                    if (auditLog != null)
                    {
                        auditLog.logMessage(methodName,
                                            EgeriaInfrastructureConnectorAuditCode.UNKNOWN_PLATFORM.getMessageDefinition(connectorName,
                                                                                                                         platformDetails.platformDisplayName,
                                                                                                                         platformDetails.platformGUID));


                    }
                }
            }
        }
        catch (Exception error)
        {
            if (auditLog != null)
            {
                auditLog.logException(methodName,
                                      EgeriaInfrastructureConnectorAuditCode.UNABLE_TO_RETRIEVE_TOPICS.getMessageDefinition(connectorName,
                                                                                                                        error.getClass().getName(),
                                                                                                                        error.getMessage()),
                                      error);


            }

            throw new ConnectorCheckedException(EgeriaInfrastructureConnectorErrorCode.UNEXPECTED_EXCEPTION.getMessageDefinition(connectorName,
                                                                                                                                 error.getClass().getName(),
                                                                                                                                 error.getMessage()),
                                                this.getClass().getName(),
                                                methodName,
                                                error);
        }
    }
}
