/* SPDX-License-Identifier: Apache 2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.devprojects.utilities.serverops;

import org.odpi.openmetadata.adminservices.client.OMAGServerOperationsClient;
import org.odpi.openmetadata.http.HttpHelper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * ServerOps provides a utility for starting and stopping servers on an OMAG Server Platform.
 */
public class ServerOps
{
    private String platformURLRoot;
    private String clientUserId;


    /**
     * Set up the parameters for the sample.
     *
     * @param platformURLRoot location of server
     * @param clientUserId userId to access the server
     */
    private ServerOps(String platformURLRoot,
                      String clientUserId)
    {
        this.platformURLRoot = platformURLRoot;
        this.clientUserId    = clientUserId;
    }


    /**
     * Start the named server on the platform.  This will fail if the platform is not running,
     * or if the user is not authorized to issue operations requests to the platform or if the
     * server is not configured.
     *
     * @param serverName string name
     */
    private void startServer(String serverName)
    {
        try
        {
            OMAGServerOperationsClient client = new OMAGServerOperationsClient(clientUserId, serverName, platformURLRoot);

            System.out.println("Starting " + serverName + " ...");
            System.out.println(client.activateWithStoredConfig());
        }
        catch (Exception error)
        {
            System.out.println("There was an " + error.getClass().getName() + " exception when calling the platform.  Error message is: " + error.getMessage());
        }
    }


    /**
     * Start the list of named servers.
     *
     * @param serverNames list of names
     */
    private void startServers(List<String> serverNames)
    {
        if (serverNames != null)
        {
            for (String serverName : serverNames)
            {
                if (serverName != null)
                {
                    this.startServer(serverName);
                }
            }
        }
    }


    /**
     * Stop the requested server.    This will fail if the server or the platform is not running,
     * or if the user is not authorized to issue operations requests to the platform.
     *
     * @param serverName string name
     */
    private void stopServer(String serverName)
    {
        try
        {
            OMAGServerOperationsClient client = new OMAGServerOperationsClient(clientUserId, serverName, platformURLRoot);

            System.out.println("Stopping " + serverName + " ...");

            client.deactivateTemporarily();

            System.out.println(serverName + " stopped.");
        }
        catch (Exception error)
        {
            System.out.println("There was an " + error.getClass().getName() + " exception when calling the platform.  Error message is: " + error.getMessage());
        }
    }


    /**
     * Stop the list of named servers.
     *
     * @param serverNames list of names
     */
    private void stopServers(List<String> serverNames)
    {
        if (serverNames != null)
        {
            for (String serverName : serverNames)
            {
                if (serverName != null)
                {
                    this.stopServer(serverName);
                }
            }
        }
    }


    /**
     * Run the requested command.
     *
     * @param mode command
     * @param serverArray list of server names
     */
    private void runCommand(String   mode,
                            String[] serverArray)
    {
        List<String> serverList = null;

        if (serverArray != null)
        {
            serverList = Arrays.asList(serverArray);
        }

        if ("start".equals(mode))
        {
            this.startServers(serverList);
        }
        else if ("stop".equals(mode))
        {
            this.stopServers(serverList);
        }
    }


    /**
     * Main program that controls the operation of the platform report.  The parameters are passed space separated.
     * The  parameters are used to override the report's default values. If mode is set to "interactive"
     * the caller is prompted for a command and one to many server names.
     *
     * @param args 1. service platform URL root, 2. client userId, 3. mode 4. server name 5. server name ...
     */
    public static void main(String[] args)
    {
        final String interactiveMode = "interactive";
        final String endInteractiveMode = "exit";

        String       platformURLRoot = "https://localhost:9443";
        String       clientUserId = "garygeeke";
        String       mode = interactiveMode;

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
            mode = args[2];
        }

        System.out.println("===============================");
        System.out.println("OMAG Server Operations Utility:    " + new Date().toString());
        System.out.println("===============================");
        System.out.println("Running against platform: " + platformURLRoot);
        System.out.println("Using userId: " + clientUserId);
        System.out.println();

        ServerOps utility = new ServerOps(platformURLRoot, clientUserId);

        HttpHelper.noStrictSSLIfConfigured();

        try
        {
            if (interactiveMode.equals(mode))
            {
                while (! endInteractiveMode.equals(mode))
                {
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                    System.out.println("Enter a command {start, stop, exit} along with one or more space separate server names. Press enter to execute request.");

                    String   command = br.readLine();
                    String[] commandWords = command.split(" ");

                    if (commandWords.length > 0)
                    {
                        mode = commandWords[0];

                        if (commandWords.length > 1)
                        {
                            utility.runCommand(mode,  Arrays.copyOfRange(commandWords, 1, commandWords.length));
                        }
                    }

                    System.out.println();
                }
            }
            else
            {
               utility.runCommand(mode, Arrays.copyOfRange(args, 3, args.length));
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
