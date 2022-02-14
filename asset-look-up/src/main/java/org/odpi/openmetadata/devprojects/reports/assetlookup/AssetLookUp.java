/* SPDX-License-Identifier: Apache 2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.devprojects.reports.assetlookup;

import org.odpi.openmetadata.accessservices.assetconsumer.api.AssetConsumerEventListener;
import org.odpi.openmetadata.accessservices.assetconsumer.client.AssetConsumer;
import org.odpi.openmetadata.accessservices.assetconsumer.elements.MeaningElement;
import org.odpi.openmetadata.accessservices.assetconsumer.events.AssetConsumerEvent;
import org.odpi.openmetadata.accessservices.assetconsumer.events.AssetConsumerEventType;
import org.odpi.openmetadata.accessservices.assetconsumer.events.NewAssetEvent;
import org.odpi.openmetadata.accessservices.assetconsumer.events.UpdatedAssetEvent;
import org.odpi.openmetadata.frameworks.connectors.properties.*;
import org.odpi.openmetadata.http.HttpHelper;
import org.odpi.openmetadata.platformservices.client.PlatformServicesClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * AssetLookUp illustrates the use of the Asset Consumer OMAS API to search for and display the metadata linked to an Asset.
 */
public class AssetLookUp extends AssetConsumerEventListener
{
    private String serverName;
    private String platformURLRoot;
    private String clientUserId;

    private AssetConsumer client = null;

    /**
     * Set up the parameters for the sample.
     *
     * @param serverName server to call
     * @param platformURLRoot location of server's platform
     * @param clientUserId userId to access the server
     */
    private AssetLookUp(String serverName,
                        String platformURLRoot,
                        String clientUserId)
    {
        this.serverName = serverName;
        this.platformURLRoot = platformURLRoot;
        this.clientUserId = clientUserId;

        try
        {
            client = new AssetConsumer(serverName, platformURLRoot);
        }
        catch (Exception error)
        {
            System.out.println("There was an exception when creating the Asset Consumer OMAS client.  Error message is: " + error.getMessage());
        }
    }



    /**
     * Process an event that was published by the Asset Consumer OMAS.
     *
     * @param event event object - call getEventType to find out what type of event.
     */
    public void processEvent(AssetConsumerEvent event)
    {
        if (event.getEventType() == AssetConsumerEventType.NEW_ASSET_EVENT)
        {
            NewAssetEvent assetEvent = (NewAssetEvent)event;

            System.out.println("EVENT: " + assetEvent.getEventType().getEventTypeName() + " - for asset " + assetEvent.getAsset().getGUID());
        }
        else if (event.getEventType() == AssetConsumerEventType.UPDATED_ASSET_EVENT)
        {
            UpdatedAssetEvent assetEvent = (UpdatedAssetEvent)event;

            System.out.println("EVENT: " + assetEvent.getEventType().getEventTypeName() + " - for asset " + assetEvent.getAsset().getGUID() + " - at " + assetEvent.getUpdateTime());
        }
    }


    /**
     * Retrieve the version of the platform.  This fails if the platform is not running or the endpoint is populated by a service that is not an
     * OMAG Server Platform.
     *
     * @return platform version or null
     */
    private String getPlatformOrigin()
    {
        try
        {
            /*
             * This client is from the platform services module and queries the runtime state of the platform and the servers that are running on it.
             */
            PlatformServicesClient platformServicesClient = new PlatformServicesClient("MyPlatform", platformURLRoot);

            /*
             * This is the first call to the platform and determines the version of the software.
             * If the platform is not running, or the remote service is not an OMAG Server Platform,
             * the utility fails at this point.
             */
            return platformServicesClient.getPlatformOrigin(clientUserId);
        }
        catch (Exception error)
        {
            System.out.println("\n\nThere was an " + error.getClass().getName() + " exception when calling the platform.  Error message is: " + error.getMessage());
            System.out.println("Ensure the platform URl is correct and the platform is running");
        }

        return null;
    }


    /**
     * Request input form the user.
     *
     * @param requestText text that describes the options
     * @return text from the user broken down into an array of words
     */
    private String[] getUserInput(String requestText)
    {
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println(requestText);

            String commandLine  = br.readLine();

            return commandLine.split(" ");
        }
        catch (Exception error)
        {
            System.out.println("There was a " + error.getClass().getName() + " exception when getting user input.  Error message is: " + error.getMessage());
        }

        return null;
    }


    /**
     * Display the retrieved assets and ask user if they want to see more ...
     *
     * @param assetGUIDPage list of retrieved assets
     * @return if more assets should be returned
     */
    private boolean getRequestForMore(List<String> assetGUIDPage,
                                      boolean      firstPage)
    {
        boolean firstAsset = firstPage;

        if (assetGUIDPage != null)
        {
            for (String assetGUID : assetGUIDPage)
            {
                if (assetGUID != null)
                {
                    this.displayAssetSummary(assetGUID, firstAsset);
                    firstAsset = false;
                }
            }

            String[] commandWords = this.getUserInput("Enter 'more' to retrieve next page ... or just return to go back to the main menu");

            return ((commandWords == null) || (commandWords.length == 0) || (! "more".equals(commandWords[0])));
        }

        return false;
    }


    /**
     * Issue one or more queries for assets, displaying results as a table.  The user controls when they have seen enough.  The purpose
     * is to allow them to discover the assets that are stored in the metadata store.
     *
     * @param command command from previous user input
     * @param searchString string to match against
     */
    private void queryAssets(String command,
                             String searchString)
    {
        try
        {
            boolean firstPage = true;
            boolean receiveAgain = false;
            int startFrom = 0;

            do
            {
                List<String> assetGUIDPage = null;

                if (command.equals("get-assets-by-term"))
                {
                    List<MeaningElement> meanings = client.getMeaningByName(clientUserId, searchString, 0 , 0);

                    if (meanings != null)
                    {
                        System.out.println("Assets for meaning: " + searchString);

                        for (MeaningElement meaning : meanings)
                        {
                            System.out.println("   meaning element: " + meaning.getElementHeader().getGUID());
                            assetGUIDPage = client.getAssetsByMeaning(clientUserId, meaning.getElementHeader().getGUID(), 0, 0);

                            if (assetGUIDPage != null)
                            {
                                startFrom = startFrom + assetGUIDPage.size();

                                receiveAgain = getRequestForMore(assetGUIDPage, firstPage);
                                firstPage = false;
                            }
                        }
                    }
                }
                else
                {
                    if (command.equals("find-assets"))
                    {
                        System.out.println("Find assets containing: " + searchString);

                        assetGUIDPage = client.findAssets(clientUserId, searchString, startFrom, 5);
                    }
                    else if (command.equals("get-assets-by-name"))
                    {
                        System.out.println("Find assets called: " + searchString);

                        assetGUIDPage = client.getAssetsByName(clientUserId, searchString, startFrom, 5);
                    }
                    else
                    {
                        System.out.println("Unrecognized command: " + command + " " + searchString);
                    }

                    if (assetGUIDPage == null)
                    {
                        System.out.println();
                    }
                    else
                    {
                        startFrom = startFrom + assetGUIDPage.size();

                        receiveAgain = getRequestForMore(assetGUIDPage, firstPage);
                        firstPage = false;
                    }
                }
            } while (receiveAgain);
        }
        catch (Exception error)
        {
            System.out.println("There was a " + error.getClass().getName() + " exception when finding an asset.  Error message is: " + error.getMessage());
        }

        System.out.println();
    }


    /**
     * Locate an asset to retrieve.  The user issues a number of search requests to get to a point where they are select an asset by guid.
     * This guid is returned
     *
     * @return guid to
     */
    private String locateAsset()
    {
        String command = null;

        try
        {
            do
            {
                String requestText = "Enter a command along with any optional parameters. Press enter to execute request.\n" +
                                     "  - find-assets        <searchString>  \n" +
                                     "  - get-assets-by-name <name> \n" +
                                     "  - get-assets-by-term <termName> \n" +
                                     "  - get-asset          <guid> \n" +
                                     "  - exit  \n";

                String[] commandWords = getUserInput(requestText);

                if (commandWords != null)
                {
                    if (commandWords.length > 0)
                    {
                        command = commandWords[0];
                    }

                    if (commandWords.length > 2)
                    {
                        if ("get-asset".equals(command))
                        {
                            return commandWords[1];
                        }
                        else
                        {
                            /*
                             * Issue another query.
                             */
                            StringBuilder stringBuffer = new StringBuilder();

                            for (String commandWord : Arrays.copyOfRange(commandWords, 1, commandWords.length))
                            {
                                if (commandWord != null)
                                {
                                    stringBuffer.append(commandWord);
                                    stringBuffer.append(" ");
                                }
                            }

                            String searchString = stringBuffer.toString().strip();

                            queryAssets(command, searchString);
                        }
                    }
                }
            } while (! "exit".equals(command));
        }
        catch (Exception error)
        {
            System.out.println("There was a " + error.getClass().getName() + " exception when locating an asset.  Error message is: " + error.getMessage());
        }

        return null;
    }


    /**
     * This method displays a summary of a retrieved asset.
     *
     * @param assetGUID unique identifier of the asset
     */
    private void displayAssetSummary(String  assetGUID,
                                     boolean firstAsset)
    {
        try
        {
            if (firstAsset)
            {
                System.out.println("| Unique identifier (GUID)         | Unique name (qualifiedName) | Display name       | Description                  |");
                System.out.println("|----------------------------------+-----------------------------+--------------------+------------------------------|");
            }

            client = new AssetConsumer(serverName, platformURLRoot);

            AssetUniverse assetUniverse = client.getAssetProperties(clientUserId, assetGUID);

            if (assetUniverse != null)
            {
                System.out.print("| " + assetUniverse.getGUID());
                System.out.print(" | " + assetUniverse.getQualifiedName());
                System.out.print(" | " + assetUniverse.getDisplayName());
                System.out.print(" | " + assetUniverse.getDescription());
                System.out.println(" |");
            }
        }
        catch (Exception error)
        {
            System.out.println("There was a " + error.getClass().getName() + " exception when calling the Asset Consumer OMAS client.  Error message is: " + error.getMessage());
        }
    }


    /**
     * This method displays all that is known about a retrieved asset.
     *
     * @param assetGUID unique identifier of the asset
     */
    private void displayAsset(String assetGUID)
    {
        try
        {
            client = new AssetConsumer(serverName, platformURLRoot);

            client.addLikeToAsset(clientUserId, assetGUID, true);

            AssetUniverse assetUniverse = client.getAssetProperties(clientUserId, assetGUID);

            if (assetUniverse != null)
            {
                System.out.println(assetUniverse.getAssetTypeName() + " with GUID: " + assetUniverse.getGUID());
                System.out.println("   " + assetUniverse.getAssetTypeName() + " inherits from " + assetUniverse.getAssetSuperTypeNames());
                System.out.println("   qualifiedName: " + assetUniverse.getQualifiedName());
                System.out.println("   displayName: " + assetUniverse.getDisplayName());
                System.out.println("   description: " + assetUniverse.getDescription());

                /*
                 * The ownership determines who is responsible for the digital resource and its metadata.
                 */
                String owner = assetUniverse.getOwner();
                if (owner != null)
                {
                    System.out.println("   owner: " + owner + " (propertyName: " + assetUniverse.getOwnerPropertyName() + " from " + assetUniverse.getOwnerTypeName() + ")");
                }

                /*
                 * The zone membership controls visibility to the asset
                 */
                List<String>       zoneMembership = assetUniverse.getZoneMembership();
                if (zoneMembership == null)
                {
                    System.out.println("   zone membership: all zones" + assetUniverse.getDescription());
                }
                else
                {
                    System.out.println("   zone membership: " + zoneMembership);
                }

                /*
                 * The meanings show the glossary terms that are attached via a semantic assignment relationship.
                 */
                List<AssetMeaning> meanings = assetUniverse.getMeanings();

                if (meanings != null)
                {
                    System.out.println("   assigned meanings: ");

                    for (AssetMeaning meaning : meanings)
                    {
                        if (meaning != null)
                        {
                            System.out.println("      * " + meaning.getName() + " - " + meaning.getDescription());
                        }
                    }
                }

                /*
                 * The origins show where the digital resource came from, from different perspectives.
                 */
                Map<String, String> origins = assetUniverse.getOrigins();
                if (origins != null)
                {
                    System.out.println("   digital resource origins: ");

                    for (String originName : origins.keySet())
                    {
                        if (originName != null)
                        {
                            System.out.println("      * " + originName + " - " + origins.get(originName));
                        }
                    }
                }

                AssetConnections connections = assetUniverse.getConnections();
                if (connections != null)
                {
                    System.out.println("   connections: ");

                    while (connections.hasNext())
                    {
                        ConnectionProperties connection = connections.next();

                        if (connection != null)
                        {
                            System.out.println("      * " + connection.getConnectionName() + ":");
                            if (connection.getEndpoint() != null)
                            {
                                System.out.println("          - endpoint address: " + connection.getEndpoint().getAddress());
                            }
                            if (connection.getConnectorType() != null)
                            {
                                System.out.println("          - connector implementation: " + connection.getConnectorType().getConnectorProviderClassName());
                            }
                        }
                    }
                }

                List<AssetClassification> classifications = assetUniverse.getAssetClassifications();
                if (classifications != null)
                {
                    System.out.println("   classifications: ");

                    for (AssetClassification classification : classifications)
                    {
                        if (classification != null)
                        {
                            System.out.println("      * " + classification.getName() + " - " + classification.getProperties());
                        }
                    }
                }

                AssetSchemaType schemaType = assetUniverse.getSchema();
                if (schemaType != null)
                {
                    System.out.println("   schema: " + schemaType.getDisplayName());
                }

                AssetFeedback feedback = assetUniverse.getFeedback();
                if (feedback != null)
                {
                    AssetComments comments = feedback.getComments();
                    if (comments != null)
                    {
                        System.out.println("   comments: ");

                        while (comments.hasNext())
                        {
                            AssetComment comment = comments.next();

                            if (comment != null)
                            {
                                System.out.println("      * " + comment.getUser() + " commented: " + comment.getCommentText());
                            }
                        }
                    }

                    AssetInformalTags informalTags = feedback.getInformalTags();
                    if (informalTags != null)
                    {
                        System.out.println("   informal tags: ");

                        while (informalTags.hasNext())
                        {
                            AssetInformalTag informalTag = informalTags.next();

                            if (informalTag != null)
                            {
                                System.out.println("      * " + informalTag.getName() + " - " + informalTag.getDescription());
                            }
                        }
                    }

                    AssetLikes likes = feedback.getLikes();
                    if (likes != null)
                    {
                        System.out.println("   likes: ");

                        while (likes.hasNext())
                        {
                            AssetLike like = likes.next();

                            if (like != null)
                            {
                                System.out.println("      * Like from " + like.getUser());
                            }
                        }
                    }

                    AssetRatings ratings = feedback.getRatings();
                    if (ratings != null)
                    {
                        System.out.println("   ratings: ");

                        while (ratings.hasNext())
                        {
                            AssetRating rating = ratings.next();

                            if (rating != null)
                            {
                                System.out.println("      * " + rating.getStarRating() + " from " + rating.getUser());
                            }
                        }
                    }
                }

                AssetRelatedAssets relatedAssets = assetUniverse.getRelatedAssets();
                if (relatedAssets != null)
                {
                    System.out.println("   related assets: ");

                    while (relatedAssets.hasNext())
                    {
                        AssetRelatedAsset relatedAsset = relatedAssets.next();

                        if (relatedAsset != null)
                        {
                            System.out.println("      * " + relatedAsset.getRelationshipAttributeName() + " (" + relatedAsset.getRelationshipTypeName() + ") - " + relatedAsset.getDisplayName() + " - " + relatedAsset.getGUID());
                        }
                    }
                }

                AssetLocations locations = assetUniverse.getKnownLocations();
                if (locations != null)
                {
                    System.out.println("   locations: ");

                    while (locations.hasNext())
                    {
                        AssetLocation location = locations.next();

                        if (location != null)
                        {
                            System.out.println("      * " + location.getDisplayName());
                        }
                    }
                }

                AssetNoteLogs noteLogs = assetUniverse.getNoteLogs();
                if (noteLogs != null)
                {
                    System.out.println("   notelogs: ");

                    while (noteLogs.hasNext())
                    {
                        AssetNoteLog noteLog = noteLogs.next();

                        if (noteLog != null)
                        {
                            System.out.println("      * " + noteLog.getDisplayName());
                        }
                    }
                }

                AssetCertifications certifications = assetUniverse.getCertifications();
                if (certifications != null)
                {
                    System.out.println("   certifications: ");

                    while (certifications.hasNext())
                    {
                        AssetCertification certification = certifications.next();

                        if (certification != null)
                        {
                            System.out.println("      * " + certification.getSummary());
                        }
                    }
                }

                AssetLicenses licenses = assetUniverse.getLicenses();
                if (licenses != null)
                {
                    System.out.println("   licenses: ");

                    while (licenses.hasNext())
                    {
                        AssetLicense license = licenses.next();

                        if (license != null)
                        {
                            System.out.println("      * " + license.getSummary());
                        }
                    }
                }

                AssetExternalIdentifiers externalIdentifiers = assetUniverse.getExternalIdentifiers();
                if (externalIdentifiers != null)
                {
                    System.out.println("   external identifiers: ");

                    while (externalIdentifiers.hasNext())
                    {
                        AssetExternalIdentifier externalIdentifier = externalIdentifiers.next();

                        if (externalIdentifier != null)
                        {
                            System.out.println("      * " + externalIdentifier.getIdentifier() + " - from : " + externalIdentifier.getScope().getQualifiedName());
                        }
                    }
                }

                AssetExternalReferences externalReferences = assetUniverse.getExternalReferences();
                if (externalReferences != null)
                {
                    System.out.println("   external references: ");

                    while (externalReferences.hasNext())
                    {
                        AssetExternalReference externalReference = externalReferences.next();

                        if (externalReference != null)
                        {
                            System.out.println("      * " + externalReference.getDisplayName() + " - " + externalReference.getURI());
                        }
                    }
                }

                AssetRelatedMediaReferences relatedMediaReferences = assetUniverse.getRelatedMediaReferences();
                if (relatedMediaReferences != null)
                {
                    System.out.println("   related media references: ");

                    while (relatedMediaReferences.hasNext())
                    {
                        AssetRelatedMediaReference relatedMediaReference = relatedMediaReferences.next();

                        if (relatedMediaReference != null)
                        {
                            System.out.println("      * " + relatedMediaReference.getDisplayName() + " - " + relatedMediaReference.getMediaType());
                        }
                    }
                }

                System.out.println("   additionalProperties: " + assetUniverse.getAdditionalProperties());

                Map<String, Object> extendedProperties = assetUniverse.getExtendedProperties();
                if (extendedProperties != null)
                {
                    System.out.println("   extendedProperties: " + extendedProperties);
                }
            }
        }
        catch (Exception error)
        {
            System.out.println("There was a " + error.getClass().getName() + " exception when calling the Asset Consumer OMAS client.  Error message is: " + error.getMessage());
        }
    }


    /**
     * Main program that controls the operation of the platform report.  The parameters are passed space separated.
     * The  parameters are used to override the report's default values. If mode is set to "interactive"
     * the caller is prompted for a command.  Otherwise it is assumed to be a guid
     *
     * @param args 1. service platform URL root, 2. client userId, 3. mode/guid
     */
    public static void main(String[] args)
    {
        final String interactiveMode = "interactive";
        final String endInteractiveMode = "exit";

        String  serverName = "mds1";
        String  platformURLRoot = "https://localhost:9443";
        String  clientUserId = "garygeeke";
        String  mode = interactiveMode;

        if (args.length > 0)
        {
            serverName = args[0];
        }

        if (args.length > 1)
        {
            platformURLRoot = args[1];
        }

        if (args.length > 2)
        {
            clientUserId = args[2];
        }

        if (args.length > 3)
        {
            mode = args[3];
        }

        System.out.println("===============================");
        System.out.println("Asset Look Up   " + new Date().toString());
        System.out.println("===============================");
        System.out.print("Running against server: " + serverName + " at " + platformURLRoot);

        AssetLookUp utility = new AssetLookUp(serverName, platformURLRoot, clientUserId);

        HttpHelper.noStrictSSLIfConfigured();

        String platformOrigin = utility.getPlatformOrigin();

        if (platformOrigin != null)
        {
            System.out.print(" - " + platformOrigin);
        }
        else
        {
            System.out.println();
            System.exit(-1);
        }

        System.out.println("Using userId: " + clientUserId);
        System.out.println();


        try
        {
            if (interactiveMode.equals(mode))
            {
                while (! endInteractiveMode.equals(mode))
                {
                    String assetGUID = utility.locateAsset();

                    if (assetGUID != null)
                    {
                        utility.displayAsset(assetGUID);
                    }
                    else
                    {
                        mode = endInteractiveMode;
                    }

                    System.out.println();
                }
            }
            else
            {
                utility.displayAsset(mode);
            }
        }
        catch (Exception  error)
        {
            System.out.println("Exception: " + error.getClass().getName() + " with message " + error.getMessage());
            System.exit(-1);
        }

        System.exit(0);
    }
}
