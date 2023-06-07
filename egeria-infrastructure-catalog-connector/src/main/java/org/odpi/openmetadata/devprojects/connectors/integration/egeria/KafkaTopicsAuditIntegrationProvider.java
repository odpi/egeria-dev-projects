/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */

package org.odpi.openmetadata.devprojects.connectors.integration.egeria;

import org.odpi.openmetadata.frameworks.auditlog.AuditLogReportingComponent;
import org.odpi.openmetadata.frameworks.connectors.properties.beans.ConnectorType;
import org.odpi.openmetadata.frameworks.integration.connectors.IntegrationConnectorProvider;


/**
 * KafkaTopicsAuditIntegrationProvider is the connector provider for the kafka integration connector that extracts topic names from the
 * broker and ensures they are catalogued.
 */
public class KafkaTopicsAuditIntegrationProvider extends IntegrationConnectorProvider
{
    /*
     * Unique identifier of the connector for the audit log.
     */
    private static final int    connectorComponentId   = 701;

    /*
     * Unique identifier for the connector type.
     */
    private static final String connectorTypeGUID      = "4bdb586e-2845-40ff-9457-f52e59fbde13";

    /*
     * Descriptive information about the connector for the connector type and audit log.
     */
    private static final String connectorQualifiedName = "Egeria:DevProjects:IntegrationConnector:Topics:KafkaTopicsAudit";
    private static final String connectorDisplayName   = "Kafka Topic Audit Integration Connector";
    private static final String connectorDescription   = "Connector validates that all topics an Apache Kafka event broker are catalogued.";
    private static final String connectorWikiPage      = "https://egeria-project.org/connectors/integration/kafka-topics-audit-integration-connector/";

    /*
     * Class of the connector.
     */
    private static final Class<?> connectorClass       = KafkaTopicsAuditIntegrationConnector.class;


    /**
     * Constructor used to initialize the ConnectorProvider with the Java class name of the specific
     * store implementation.
     */
    public KafkaTopicsAuditIntegrationProvider()
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
