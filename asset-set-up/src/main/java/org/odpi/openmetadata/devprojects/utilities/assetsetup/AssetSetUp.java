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

import java.util.List;


/**
 * AssetSetUp illustrates the use of the a variety of OMAS APIs to catalog a file in the open metadata ecosystem.
 */
public class AssetSetUp
{
    private static final String fileName = "sample-data/oak-dene-drop-foot-weekly-measurements/week1.csv";

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
     * Set up the parameters for the utility.
     *
     * @param serverName server to call
     * @param serverURLRoot location of server
     * @param clientUserId userId to access the server
     */
    private AssetSetUp(String serverName,
                       String serverURLRoot,
                       String clientUserId)
    {
        this.serverName = serverName;
        this.serverURLRoot = serverURLRoot;
        this.clientUserId = clientUserId;
    }


    /**
     * This runs the utility
     */
    private void run()
    {
        try
        {
            CSVFileAssetOwner client = new CSVFileAssetOwner(serverName, serverURLRoot);

            List<String> assetGUIDs = client.addCSVFileToCatalog(clientUserId,
                                                                 fileName,
                                                                 "This is a new file asset created by AssetSetUp.",
                                                                 fileName);

            System.out.println("New assets created: " +  assetGUIDs);
        }
        catch (Exception error)
        {
            System.out.println("There was a " + error.getClass().getName() + " exception when calling the OMAG Server Platform.  Error message is: " + error.getMessage());
        }
    }


    /**
     * Main program that initiates the operation of the AssetSetUp utility.  The parameters are optional.  They are passed space separated.
     * They are used to override the utility's default values.
     *
     * @param args 1. service platform URL root, 2. client userId, 3. server name
     */
    public static void main(String[] args)
    {
        String  platformURLRoot = "https://localhost:9443";
        String  clientUserId = "erinoverview";
        String  serverName = "cocoMDS1";

        if (args.length > 0)
        {
            platformURLRoot = args[0];
        }

        if (args.length > 1)
        {
            clientUserId = args[1];
        }

        if (args.length > 2)
        {
            serverName = args[2];
        }

        System.out.println("===============================");
        System.out.println("Asset Set Up Utility:          ");
        System.out.println("===============================");
        System.out.println("Running against platform: " + platformURLRoot);
        System.out.println("Focused on server: " + serverName);
        System.out.println("Using userId: " + clientUserId);
        System.out.println();

        HttpHelper.noStrictSSLIfConfigured();

        try
        {
            AssetSetUp assetSetUp = new AssetSetUp(serverName, platformURLRoot, clientUserId);

            assetSetUp.run();
        }
        catch (Exception  error)
        {
            System.out.println("Exception: " + error.getClass().getName() + " with message " + error.getMessage());
            System.exit(-1);
        }
    }
}
