# egeria-dev-projects

Fun projects a developer can do with Egeria.  Each module has working code that you can use "as is" or you
may extend it for your own uses.

* **component-id-report** - list the component ids in use in your Egeria deployment.  
  these component ids are used when registering with the audit log and are included in
  each audit log message from the component.  Using unique component ids helps to pinpoint
  exactly which component produced a specific audit log record.
  
  Code starts with the components that are shipped with Egeria.  Update to include your
  connector implementation.
  
* **egeria-config-utility** - issues commands to configure different types of OMAG servers.
  It has a list of default values at the top of the file and that you can update for your environment
  and you can extend with new commands and options.

* **egeria-ops-utility** - issues commands to start and stop different types of OMAG servers.
  It has a list of default values at the top of the file and that you can update for your environment
  and you can extend with new commands and options.
  
* **egeria-platform-report** - issue commands to a running platform an reports on the status of
  the platform itself and the servers running on it.  There are different options that control the
  detail displayed.  Extend it with new options and layouts.
  
* **event-display-audit-log-connector** - an implementation of an Audit Log Destination Connector
  that displays the contents of event added ans additionalInformation in EVENT audit log record.
  Change the format or add additional information.
  
* **kafka-topics-audit-connector** - an implementation of an Integration Connector that validates that all
  topics that are known to an Apache Kafka server are also catalogued in open metadata.

* **asset-look-up** - a report about the metadata associated with an Asset entity.  Use **asset-set-up**
  to populate your metadata repository and then explore ...
  
* **asset-set-up** - a utility to populate a metadata access store with a variety of
  metadata centred around an Asset.  This is a metadata element describing a digital
  resource.
  
----
License: [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/),
Copyright Contributors to the ODPi Egeria project.
