dotCMS SOLR Plugin
====

The SOLR plugin for the dotCMS system can be used to post content and all related fields to a SOLR installation for indexing.  

The plugin contains a portlet that shows the queue of content to be indexed and workflow actionlets that can be used in custom workflow to post and remove content from the index as the content is published and or unpublished.

This version allows users to configure SOLR Server(s) per host, that means, if you have contents associated to different host and you want to index/update each host with a particular SOLR server(s).Also if you have a host where you don't want to index its content, you only need leave blank the Solr server multi select for that host, and that host contents will not be updated. 


Configuring the Plugin
----------------------

Once plugin has been copied into your server, you will need to adjust some variables in your local plugin.properties file located in the /conf folder:

1) Set the frequency of the quartz job that will execute the indexing in your SOLR instance (quartz.job.cron.expression).

2) Set the max number of attempts per content (com.dotcms.solr.NUM_OF_TRIES). After this limit is reached, dotCMS won't keep trying to send the content to your SOLR instance; the content will be marked with an error flag in the portlet, and the error will be shown there as well.

3) Set the max number of contentlets sent to SOLR simultaneously (com.dotcms.solr.DOCUMENTS_PER_REQUEST).

4) Set the name of the field variable that will be used to translate the dotCMS field name to the SOLR field name (com.dotcms.solr.MODIFY_FIELD_NAME_ATTRIBUTE). For example, if the varname of a dotCMS field is "title", and you need that field to be named "solrTitle" in your SOLR instance, you can associate a variable to the dotCMS field indicating what the name of the field will be in SOLR. The name of the field variable is what you set in com.dotcms.solr.MODIFY_FIELD_NAME_ATTRIBUTE.

5) Set the name of the field variable that will identify the fields that should not be sent to your SOLR instance (com.dotcms.solr.IGNORE_FIELDS_WITH_ATTRIBUTE). Every dotCMS field assigned with a variable called as indicated in thir property won't be sent to SOLR.

6) Set the name of the field varibale that will identify the metadata information that should not be sent to your SOLR instance (com.dotcms.solr.IGNORE_METADATA_FIELD_ATTRIBUTES). Every dotCMS metadata field assigned with a variable called as indicated in thir property won't be sent to SOLR.

7) Set the list of dotCMS metadata fields that won't ever be sent to SOLR (com.dotcms.solr.IGNORE_METADATA_FIELDS). Unlike com.dotcms.solr.IGNORE_METADATA_FIELD_ATTRIBUTES which allows you to ignore metadata fields from a particular dotCMS structure, this comma separated list of metadata fields will be ignored from every single structure being indexed in your SOLR instance.

8) Set wheter or not the table containing the SOLR indexation actions must be dropped and created again on plugin deployment (com.dotcms.solr.DROP_AND_RECREATE_TABLE).

9) Set the number of SOLR instances that will be indexed (com.dotcms.solr.SOLR_SERVER_NUMBER).

10) Set the number of SOLR indexing actions that need to be shown per page in the backend porlet (com.dotcms.solr.RESULTS_PER_PAGE).

11) Set the connection properties to each one of your SOLR instances ( "?" is a number between 0 and com.dotcms.solr.SOLR_SERVER_NUMBER - 1):

	a) com.dotcms.solr.?.SOLR_SERVER
	b) com.dotcms.solr.?.PARAMS_NUMBER
	c) com.dotcms.solr.?.PARAMETER_%_NAME ("%" is a number between 0 and com.dotcms.solr.?.PARAMS_NUMBER - 1)
	d) com.dotcms.solr.?.PARAMS_%_VALUE

12) Set wheter or not all the host should be pre-set with all the solr servers values (com.dotcms.solr.SET_ALL_SOLR_SERVERS_IN_HOSTS). If is set false, then the user should manually select per each host which Solr server will use.


How to add Solr to your workflow Schemes
-----------------------------------------

First you need to Create or Edit a workflow schema under workflow schema portlet. Then you need to add a workflow step. In that "Step",  you need to include the "Add to Solr" action.
Do the same thing to add the "Delete from Solr" action.
After that, your scheme will show the two actions.
Apply that scheme to all the structure, you want to have it. Then when you edit one of those structure contentlets you will see you "Add to Solr" and "Delete from Solr" button in the left menu.


Indexing Content in your SOLR Instance(s)
-----------------------------------------

Once the plugin has been installed, configured, and deployed, you will need to do the following in order to be able to index content into your SORL instance(s):

1) Include the "Add/Update to SOLR Index" and/or "Remove Content from the SOLR Index" subactions to the workflow schema(s) associated to the structure you need to be indexed in SOLR.

2) Add the SOLR indexing tool to any of your dotCMS tabs so you can have access to the SOLR action queue.

FAQs:
-----

1.	If I already run the plugin the first time, but I need to add or remove a Solr server?
In this case you have to:

a) Modify in your plugin.properties file com.dotcms.solr.SOLR_SERVER_NUMBER  and the parameter mentioned in the configuration section number 11. Adding/removing the solr server(s) you want.

b)About your current host "Solr Servers" configuration. Here you have two choices:

	b.1) If you don't have any trouble that the system, reset, all the host values and the you apply manually your "Solr servers" selection. Then delete from these two fields: "Solr Configuration" and "Solr Servers" from the "Host" Structure. Also in the plugin MANIFEST.MF file modify the plugin version to a different number. For example: from "Plugin-Version: 2.0" to "Plugin-Version: 2.0.1".

	b.2) If you don't want to lose all you current selected values per host, you should manually add/remove the Solr server(s) under the custom field Solr Servers"  in the Structure portlet for the "Host" structure. 

c) Redeploy your plugin.

d) Restart your dotCMS server.

e) Modify manually your "Solr Server" per each Host. If it is necessary. 


Plugin Github Tags
------------------
These plugin have two versions:

1.  Tag plugin-solr-1.0-dotcms-2.x

This version send all the content to all the Solr servers specified in the plugin.properties file. You don't have the option of selecting Solr servers per host.
To obtain this version you need to git clone the master of the plugin and then run this command:
git checkout plugin-solr-1.0-dotcms-2.x

2.  Tag plugin-solr-2.0-dotcms-2.x

This version have the option of selecting Solr servers per host.
To obtain this version you need to git clone the master of the plugi. and then run this command:
git checkout plugin-solr-2.0-dotcms-2.x


Note
----
Remember to rename the "plugin-solr" folder to "com.dotcms.solr"