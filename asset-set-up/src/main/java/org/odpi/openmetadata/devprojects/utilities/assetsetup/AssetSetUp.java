/* SPDX-License-Identifier: Apache 2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.devprojects.utilities.assetsetup;

import org.odpi.openmetadata.accessservices.assetconsumer.client.AssetConsumer;
import org.odpi.openmetadata.accessservices.assetowner.client.CSVFileAssetOwner;
import org.odpi.openmetadata.accessservices.datamanager.client.DatabaseManagerClient;
import org.odpi.openmetadata.accessservices.datamanager.client.ExternalReferenceManagerClient;
import org.odpi.openmetadata.accessservices.digitalarchitecture.client.ConnectionManager;
import org.odpi.openmetadata.accessservices.digitalarchitecture.client.LocationManager;
import org.odpi.openmetadata.accessservices.digitalarchitecture.client.ValidValuesManager;
import org.odpi.openmetadata.accessservices.governanceprogram.client.GovernanceZoneManager;
import org.odpi.openmetadata.accessservices.governanceprogram.client.SubjectAreaManager;
import org.odpi.openmetadata.accessservices.governanceprogram.properties.GovernanceZoneProperties;
import org.odpi.openmetadata.accessservices.assetmanager.client.ExternalAssetManagerClient;
import org.odpi.openmetadata.accessservices.itinfrastructure.client.CapabilityManagerClient;
import org.odpi.openmetadata.accessservices.subjectarea.SubjectArea;
import org.odpi.openmetadata.frameworks.connectors.ffdc.InvalidParameterException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.PropertyServerException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.UserNotAuthorizedException;
import org.odpi.openmetadata.http.HttpHelper;


/**
 * AssetSetUp illustrates the use of the Governance Program OMAS API to create governance zones
 * for Coco Pharmaceuticals.
 */
public class AssetSetUp
{
    private String serverName;
    private String serverURLRoot;
    private String clientUserId;

    private AssetConsumer                  assetConsumerClient            = null;
    private CSVFileAssetOwner              csvOnboardingClient            = null;
    private GovernanceZoneManager          governanceZoneManager          = null;
    private SubjectAreaManager             subjectAreaManager             = null;
    private SubjectArea                    subjectAreaClient              = null;
    private ExternalAssetManagerClient     externalAssetManagerClient     = null;
    private DatabaseManagerClient          databaseManagerClient          = null;
    private ExternalReferenceManagerClient externalReferenceManagerClient = null;
    private ConnectionManager              connectionManager              = null;
    private LocationManager                locationManager                = null;
    private ValidValuesManager             validValuesManager             = null;
    private CapabilityManagerClient        capabilityManagerClient        = null;

    /**
     * Set up the parameters for the sample.
     *
     * @param serverName server to call
     * @param serverURLRoot location of server
     * @param clientUserId userId to access the server
     */
    public AssetSetUp(String serverName,
                      String serverURLRoot,
                      String clientUserId)
    {
        this.serverName = serverName;
        this.serverURLRoot = serverURLRoot;
        this.clientUserId = clientUserId;
    }


    /**
     * Set up a new zone
     *
     * @param zoneName qualified name
     * @param displayName display name
     * @param description longer description
     * @param criteria what types of assets are found in this zone
     * @throws InvalidParameterException bad parameters passed to governanceZoneManager
     * @throws UserNotAuthorizedException userId is not allowed to create zones
     * @throws PropertyServerException service is not running - or is in trouble
     */
    private void createZone(String     zoneName,
                            String     displayName,
                            String     description,
                            String     criteria) throws InvalidParameterException,
                                                        UserNotAuthorizedException,
                                                        PropertyServerException
    {
        System.out.println("------------------------------------------------------------------------");
        System.out.println(zoneName);
        System.out.println("------------------------------------------------------------------------");
        System.out.println(" ==> qualifiedName: " + zoneName);
        System.out.println(" ==> displayName:   " + displayName);
        System.out.println(" ==> description:   " + description);
        System.out.println(" ==> criteria:      " + criteria);
        System.out.println(" ");

        GovernanceZoneProperties zoneProperties = new GovernanceZoneProperties();

        zoneProperties.setQualifiedName(zoneName);
        zoneProperties.setDisplayName(displayName);
        zoneProperties.setDescription(description);
        zoneProperties.setCriteria(criteria);

        governanceZoneManager.createGovernanceZone(clientUserId, zoneProperties);
    }


    /**
     * This runs the sample
     */
    public void run()
    {
        try
        {
            governanceZoneManager = new GovernanceZoneManager(serverName, serverURLRoot);

            GovernanceZoneSampleDefinitions[] zoneSampleDefinitions = GovernanceZoneSampleDefinitions.values();

            for (GovernanceZoneSampleDefinitions zoneDefinition : zoneSampleDefinitions)
            {
                createZone(zoneDefinition.getZoneName(),
                           zoneDefinition.getDisplayName(),
                           zoneDefinition.getDescription(),
                           zoneDefinition.getCriteria());
            }


        }
        catch (Exception error)
        {
            System.out.println("There was an exception when calling the GovernanceZoneManager governanceZoneManager.  Error message is: " + error.getMessage());
        }
    }


    /**
     * Main program that controls the operation of the sample.  The parameters are passed space separated.
     * The file name must be passed as parameter 1.  The other parameters are used to override the
     * sample's default values.
     *
     * @param args 1. file name 2. server name, 3. URL root for the server, 4. governanceZoneManager userId
     */
    public static void main(String[] args)
    {
        String  serverName = "cocoMDS2";
        String  serverURLRoot = "https://localhost:9443";
        String  clientUserId = "erinoverview";


        if (args.length > 1)
        {
            serverName = args[1];
        }

        if (args.length > 2)
        {
            serverURLRoot = args[2];
        }

        if (args.length > 3)
        {
            clientUserId = args[3];
        }

        System.out.println("===============================");
        System.out.println("Create Governance Zones Sample   ");
        System.out.println("===============================");
        System.out.println("Running against server: " + serverName + " at " + serverURLRoot);
        System.out.println("Using userId: " + clientUserId);
        System.out.println();

        HttpHelper.noStrictSSLIfConfigured();


        try
        {
            AssetSetUp sample = new AssetSetUp(serverName, serverURLRoot, clientUserId);

            sample.run();
        }
        catch (Exception  error)
        {
            System.out.println("Exception: " + error.getClass().getName() + " with message " + error.getMessage());
            System.exit(-1);
        }
    }
}
