/* SPDX-License-Identifier: Apache 2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.devprojects.reports.assetlookup;

import org.odpi.openmetadata.accessservices.assetconsumer.client.AssetConsumer;
import org.odpi.openmetadata.frameworks.connectors.properties.AssetUniverse;
import org.odpi.openmetadata.http.HttpHelper;


/**
 * AssetLookUp illustrates the use of the Asset Consumer OMAS API to search for and display the metadata linked to an Asset.
 */
public class AssetLookUp
{
    private String  serverName;
    private String  serverURLRoot;
    private String  clientUserId;

    private AssetConsumer client = null;

    /**
     * Set up the parameters for the sample.
     *
     * @param serverName server to call
     * @param serverURLRoot location of server
     * @param clientUserId userId to access the server
     */
    public AssetLookUp(String  serverName,
                       String  serverURLRoot,
                       String  clientUserId)
    {
        this.serverName = serverName;
        this.serverURLRoot = serverURLRoot;
        this.clientUserId = clientUserId;

        try
        {
            client = new AssetConsumer(serverName, serverURLRoot);
        }
        catch (Exception error)
        {
            System.out.println("There was an exception when creating the Asset Consumer OMAS client.  Error message is: " + error.getMessage());
        }
    }


    String locateAsset()
    {
        return null;
    }

    /**
     * This method displays a retrieved asset.
     *
     * @param assetGUID unique identifier of the asset
     */
    void displayAsset(String assetGUID)
    {
        try
        {
            client = new AssetConsumer(serverName, serverURLRoot);

            AssetUniverse assetUniverse = client.getAssetProperties(clientUserId, assetGUID);

            if (assetUniverse != null)
            {
                System.out.println("qualifiedName: " + assetUniverse.getQualifiedName());
                System.out.println("displayName: " + assetUniverse.getDisplayName());
                System.out.println("description: " + assetUniverse.getDescription());
            }



        }
        catch (Exception error)
        {
            System.out.println("There was a " + error.getClass().getName() + " exception when calling the Asset Consumer OMAS client.  Error message is: " + error.getMessage());
        }
    }


    /**
     * Main program that controls the operation of the sample.  The parameters are passed space separated.
     * The file name must be passed as parameter 1.  The other parameters are used to override the
     * sample's default values.
     *
     * @param args 1. server name, 3. URL root for the server, 4. client userId
     */
    public static void main(String[] args)
    {
        String  serverName = "mds1";
        String  serverURLRoot = "https://localhost:9443";
        String  clientUserId = "erinoverview";


        if (args.length > 0)
        {
            serverName = args[0];
        }

        if (args.length > 1)
        {
            serverURLRoot = args[1];
        }

        if (args.length > 2)
        {
            clientUserId = args[2];
        }

        System.out.println("===============================");
        System.out.println("Asset Look Up   ");
        System.out.println("===============================");
        System.out.println("Running against server: " + serverName + " at " + serverURLRoot);
        System.out.println("Using userId: " + clientUserId);
        System.out.println();

        HttpHelper.noStrictSSLIfConfigured();


        try
        {
            AssetLookUp assetLookUp = new AssetLookUp(serverName, serverURLRoot, clientUserId);

            String assetGUID = assetLookUp.locateAsset();

            if (assetGUID != null)
            {
                assetLookUp.displayAsset(assetGUID);
            }
        }
        catch (Exception  error)
        {
            System.out.println("Exception: " + error.getClass().getName() + " with message " + error.getMessage());
            System.exit(-1);
        }

        System.out.println("Exiting ...");
    }
}
