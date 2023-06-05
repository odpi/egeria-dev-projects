/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */

package org.odpi.openmetadata.devprojects.connectors.integration.egeria;


import org.odpi.openmetadata.accessservices.itinfrastructure.api.ITInfrastructureEventListener;
import org.odpi.openmetadata.accessservices.itinfrastructure.events.ITInfrastructureOutTopicEvent;
import org.odpi.openmetadata.accessservices.itinfrastructure.metadataelements.SoftwareServerPlatformElement;
import org.odpi.openmetadata.accessservices.itinfrastructure.properties.SoftwareCapabilityProperties;
import org.odpi.openmetadata.devprojects.connectors.integration.egeria.ffdc.EgeriaInfrastructureConnectorAuditCode;
import org.odpi.openmetadata.devprojects.connectors.integration.egeria.ffdc.EgeriaInfrastructureConnectorErrorCode;
import org.odpi.openmetadata.frameworks.auditlog.ComponentDevelopmentStatus;
import org.odpi.openmetadata.frameworks.connectors.ffdc.ConnectorCheckedException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.InvalidParameterException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.PropertyServerException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.UserNotAuthorizedException;
import org.odpi.openmetadata.integrationservices.infrastructure.connector.InfrastructureIntegratorConnector;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * EgeriaInfrastructureIntegrationConnectorBase catalogues a deployment of Egeria.
 */
public abstract class EgeriaInfrastructureIntegrationConnectorBase extends InfrastructureIntegratorConnector implements ITInfrastructureEventListener
{
    List<PlatformDetails> monitoredPlatforms = new ArrayList<>();
    String                clientUserId = "garygeeke";
    String                platformName = null;


    /**
     * PlatformDetails acts as a cache of knowledge about a particular platform.
     */
    class PlatformDetails
    {
        String platformRootURL = null;
        String platformGUID = null;
        String platformTypeName = null;
        String platformQualifiedName = null;
        String platformDisplayName = null;
        String capabilityGUID = null;

        @Override
        public String toString()
        {
            return "PlatformDetails{" +
                           "platformRootURL='" + platformRootURL + '\'' +
                           ", platformGUID='" + platformGUID + '\'' +
                           ", platformTypeName='" + platformTypeName + '\'' +
                           ", platformQualifiedName='" + platformQualifiedName + '\'' +
                           ", platformDisplayName='" + platformDisplayName + '\'' +
                           ", capabilityGUID='" + capabilityGUID + '\'' +
                           '}';
        }
    }


    private static final String platformTypeName       = "SoftwareServerPlatform";


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
         * This is the user id to call the Egeria OMAG Server Platforms to extract information from.
         */
        if (connectionProperties.getUserId() != null)
        {
            clientUserId = connectionProperties.getUserId();
        }

        try
        {
            super.getContext().registerListener(this);
            super.getContext().setInfrastructureManagerIsHome(false);

            /*
             * Populate the monitored platforms with the catalogued SoftwareServerPlatforms.
             */
            List<SoftwareServerPlatformElement> cataloguedPlatforms = super.getContext().findSoftwareServerPlatforms(platformName, null, 0, 0);

            if (cataloguedPlatforms != null)
            {
                for (SoftwareServerPlatformElement platform : cataloguedPlatforms)
                {
                    assessElementForMonitoring(platform.getElementHeader().getGUID(),
                                               platform.getElementHeader().getType().getTypeName(),
                                               platform.getElementHeader().getType().getSuperTypeNames());
                }
            }
        }
        catch (Exception error)
        {
            throw new ConnectorCheckedException(EgeriaInfrastructureConnectorErrorCode.UNEXPECTED_EXCEPTION.getMessageDefinition(connectorName,
                                                                                                                                 error.getClass().getName(),
                                                                                                                                 error.getMessage()),
                                                this.getClass().getName(),
                                                methodName,
                                                error);
        }
    }



    /**
     * Create an entity for a software service.
     *
     * @param serverGUID unique identifier of the server entity
     * @param serverName name of the server
     * @param serviceType type of service to catalog
     * @param serviceCode component code of service
     * @param serviceFullName full name of service
     * @param serviceDescription description of service
     * @param developmentStatus status of service
     * @param serviceURLMarker segment of the URL in API that identifies this component
     * @param wikiURL url of wiki for design and diagnosis
     * @return unique identifier of new service
     * @throws ConnectorCheckedException no context
     * @throws InvalidParameterException bad parameters passed
     * @throws UserNotAuthorizedException problem with user Id
     * @throws PropertyServerException problem with open metadata repository
     */
    String createSoftwareService(String                     serverGUID,
                                 String                     serverName,
                                 String                     serviceType,
                                 int                        serviceCode,
                                 String                     serviceFullName,
                                 String                     serviceDescription,
                                 ComponentDevelopmentStatus developmentStatus,
                                 String                     serviceURLMarker,
                                 String                     wikiURL) throws ConnectorCheckedException,
                                                                            InvalidParameterException,
                                                                            UserNotAuthorizedException,
                                                                            PropertyServerException
    {
        SoftwareCapabilityProperties properties = new SoftwareCapabilityProperties();

        if (serviceType != null)
        {
            properties.setTypeName(serviceType);
        }
        else
        {
            properties.setTypeName("SoftwareService");
        }

        properties.setQualifiedName(serverName + ":" + serviceFullName);
        properties.setDisplayName(serviceFullName);
        properties.setDescription(serviceDescription);

        Map<String, String> additionalProperties = new HashMap<>();

        additionalProperties.put("componentCode", Integer.toString(serviceCode));
        additionalProperties.put("componentWikiURL", wikiURL);
        additionalProperties.put("developmentStatus", developmentStatus.getName());
        additionalProperties.put("serviceURLMarker", serviceURLMarker);

        properties.setAdditionalProperties(additionalProperties);

        String serviceGUID = super.getContext().createSoftwareCapability(null, properties);

        super.getContext().deployCapability(serviceGUID, serverGUID, null);

        return serviceGUID;
    }


    /**
     * Called each time an event that is published by the IT Infrastructure OMAS, it is looking for Software Server Platforms to add to monitoredPlatforms.
     */
    public void processEvent(ITInfrastructureOutTopicEvent event)
    {
        assessElementForMonitoring(event.getElementHeader().getGUID(),
                                   event.getElementHeader().getType().getTypeName(),
                                   event.getElementHeader().getType().getSuperTypeNames());
    }


    /**
     * If the element is a software server platform and it is not already being monitored then it is added to monitored platforms.
     *
     * @param elementGUID unique id of element
     * @param elementTypeName type name of element
     * @param elementSuperTypes super types of element
     */
    private void assessElementForMonitoring(String       elementGUID,
                                            String       elementTypeName,
                                            List<String> elementSuperTypes)
    {
        if ((platformTypeName.equals(elementTypeName)) || ((elementSuperTypes != null) && (elementSuperTypes.contains(platformTypeName))))
        {
            /*
             * Element is a software server platform. Is this a new platform?
             */
            boolean alreadyMonitored = false;

            for (PlatformDetails platformDetails : monitoredPlatforms)
            {
                if (elementGUID.equals(platformDetails.platformGUID))
                {
                    alreadyMonitored = true;
                }
            }

            /*
             * All new platforms are added to monitored platforms
             */
            if (! alreadyMonitored)
            {
                PlatformDetails platformDetails = new PlatformDetails();

                platformDetails.platformGUID = elementGUID;
                platformDetails.platformTypeName = elementTypeName;

                monitoredPlatforms.add(platformDetails);
            }
        }
    }



    /**
     * Shutdown monitoring
     *
     * @throws ConnectorCheckedException something failed in the super class
     */
    @Override
    public synchronized void disconnect() throws ConnectorCheckedException
    {
        final String methodName = "disconnect";

        if (auditLog != null)
        {
            auditLog.logMessage(methodName,
                                EgeriaInfrastructureConnectorAuditCode.CONNECTOR_STOPPING.getMessageDefinition(connectorName));
        }

        super.disconnect();
    }
}
