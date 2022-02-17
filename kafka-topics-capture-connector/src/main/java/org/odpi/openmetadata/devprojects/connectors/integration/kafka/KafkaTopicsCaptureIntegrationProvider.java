/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */

package org.odpi.openmetadata.devprojects.connectors.integration.kafka;

import org.odpi.openmetadata.frameworks.auditlog.AuditLogReportingComponent;
import org.odpi.openmetadata.frameworks.connectors.properties.beans.ConnectorType;
import org.odpi.openmetadata.governanceservers.integrationdaemonservices.connectors.IntegrationConnectorProvider;

import java.util.ArrayList;
import java.util.List;


/**
 * KafkaTopicsCaptureIntegrationProvider is the connector provider for the kafka integration connector that extracts topic names from the broker.
 */
public class KafkaTopicsCaptureIntegrationProvider extends IntegrationConnectorProvider
{
    /*
     * Unique identifier of the connector for the audit log.
     */
    private static final int    connectorComponentId   = 95;

    /*
     * Unique identifier for the connector type.
     */
    private static final String connectorTypeGUID      = "c8956c71-ead1-49bd-9e38-d7b53a8880c3";

    /*
     * Descriptive information about the connector for the connector type and audit log.
     */
    private static final String connectorQualifiedName = "Egeria:DevProjects:IntegrationConnector:Topics:KafkaTopicsCapture";
    private static final String connectorDisplayName   = "Kafka Topic Capture Integration Connector";
    private static final String connectorDescription   = "Connector maintains a list of KafkaTopic assets associated with an Apache Kafka event broker.";
    private static final String connectorWikiPage      = "https://odpi.github.io/egeria-docs/connectors/integration/kafka-topics-capture-integration-connector/";

    /*
     * Class of the connector.
     */
    private static final Class<?> connectorClass       = KafkaTopicsCaptureIntegrationConnector.class;


    static final String TEMPLATE_QUALIFIED_NAME_CONFIGURATION_PROPERTY = "templateQualifiedName";

    /**
     * Constructor used to initialize the ConnectorProvider with the Java class name of the specific
     * store implementation.
     */
    public KafkaTopicsCaptureIntegrationProvider()
    {
        super();

        /*
         * Set up the class name of the connector that this provider creates.
         */
        super.setConnectorClassName(connectorClass.getName());

        /*
         * Set up the connector type that should be included in a connection used to configure this connector.
         */
        ConnectorType connectorType = new ConnectorType();
        connectorType.setType(ConnectorType.getConnectorTypeType());
        connectorType.setGUID(connectorTypeGUID);
        connectorType.setQualifiedName(connectorQualifiedName);
        connectorType.setDisplayName(connectorDisplayName);
        connectorType.setDescription(connectorDescription);
        connectorType.setConnectorProviderClassName(this.getClass().getName());
        List<String> recognizedConfigurationProperties = new ArrayList<>();
        recognizedConfigurationProperties.add(TEMPLATE_QUALIFIED_NAME_CONFIGURATION_PROPERTY);
        connectorType.setRecognizedConfigurationProperties(recognizedConfigurationProperties);

        super.connectorTypeBean = connectorType;

        /*
         * Set up the component description used in the connector's audit log messages.
         */
        AuditLogReportingComponent componentDescription = new AuditLogReportingComponent();

        componentDescription.setComponentId(connectorComponentId);
        componentDescription.setComponentName(connectorQualifiedName);
        componentDescription.setComponentDescription(connectorDescription);
        componentDescription.setComponentWikiURL(connectorWikiPage);

        super.setConnectorComponentDescription(componentDescription);
    }
}
