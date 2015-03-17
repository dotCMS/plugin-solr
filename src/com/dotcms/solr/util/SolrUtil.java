package com.dotcms.solr.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import javax.activation.MimetypesFileTypeMap;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.servlet.SolrRequestParsers;
import com.dotcms.repackage.org.apache.tika.metadata.Metadata;
import com.dotcms.repackage.org.apache.tika.parser.AutoDetectParser;
import com.dotcms.repackage.org.apache.tika.parser.ParseContext;
import com.dotcms.repackage.org.apache.tika.parser.Parser;
import com.dotcms.repackage.org.apache.tika.sax.BodyContentHandler;

import com.dotcms.solr.business.DotSolrException;
import com.dotmarketing.beans.Host;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.cache.FieldsCache;
import com.dotmarketing.common.db.DotConnect;
import com.dotmarketing.db.DbConnectionFactory;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.plugin.business.PluginAPI;
import com.dotmarketing.portlets.categories.model.Category;
import com.dotmarketing.portlets.contentlet.business.ContentletAPI;
import com.dotmarketing.portlets.contentlet.business.HostAPI;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.portlets.fileassets.business.FileAssetAPI;
import com.dotmarketing.portlets.structure.factories.FieldFactory;
import com.dotmarketing.portlets.structure.factories.StructureFactory;
import com.dotmarketing.portlets.structure.model.Field;
import com.dotmarketing.portlets.structure.model.FieldVariable;
import com.dotmarketing.portlets.structure.model.Structure;
import com.dotmarketing.util.Config;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.StringUtils;
import com.dotmarketing.util.UtilMethods;
import com.liferay.portal.model.User;

import com.dotcms.repackage.edu.emory.mathcs.backport.java.util.Arrays;
/**
 * This class manage all the operation we can do over a from/to a Solr index (search, add and delete)
 * @author Oswaldo
 *
 */
public class SolrUtil {	

	/**
	 * Generate a chanel to communicate with a solr server. 
	 * This Methods was provided by S&P
	 * 
	 * @param SolrServerUrl
	 * @return HttpSolrClient
	 * @throws IOException
	 */
	public static HttpSolrClient getHttpSolrServer(String SolrServerUrl)	throws IOException {
		// configure a server object with actual solr values.
		HttpSolrClient solrServer = new HttpSolrClient(
				SolrServerUrl);
		solrServer.setParser(new XMLResponseParser());
		solrServer.setSoTimeout(5000);
		solrServer.setConnectionTimeout(5000);
		return solrServer;
	}

	/**
	 * Validate if the Solr servers is responding
	 * This Methods was provided by S&P
	 * 
	 * @param solrServer solr server url
	 * @return true if the servers is responding, false is the server is not accessible
	 */
	public static boolean checkSolrServerByPing(HttpSolrClient solrServer) {
		// configure a server object with actual solr values.
		try {
			SolrPingResponse solrPingResponse = solrServer.ping();
			int solrPingResponseStatus = solrPingResponse.getStatus();
			if (solrPingResponseStatus == 0)
				return true;
		} catch (Exception exception) {
			Logger.debug(SolrUtil.class, "Ping Failed On the Server : "+ solrServer.getBaseURL(), exception);
		}
		return false;
	}

	/**
	 * Adding documents collection to Solr Index.
	 * This method was update in colaboration with S&P
	 * 
	 * @param SolrServerUrl Solr Server Url
	 * @param docs Collection<SolrInputDocument> collection of elements to include
	 * @throws SolrServerException
	 * @throws IOException
	 */
	public static void addToSolrIndex(HttpSolrClient solrServer,	Collection<SolrInputDocument> docs) throws SolrServerException,	IOException {
		/* Add collection to solr index */
		UpdateResponse rsp = solrServer.add(docs);
		Logger.info(SolrUtil.class, "ADDING SOLR INDEX: " + rsp);
		/* Commit collection to solr index */
		UpdateResponse rsp2 = solrServer.commit();
		Logger.info(SolrUtil.class, "COMMITING SOLR INDEX: " + rsp2);
	}

	/**
	 * Adding SolrInputDocument to Solr Index
	 * This method was update in colaboration with S&P
	 * 
	 * @param SolrServerUrl Solr Server Url
	 * @param doc SolrInputDocument to include
	 * @return boolean, true if the element were added to the Solr index
	 * @throws SolrServerException
	 * @throws IOException
	 */
	public static void addToSolrIndex(String SolrServerUrl, SolrInputDocument doc) throws SolrServerException, IOException {		
		HttpSolrClient server = getHttpSolrServer(SolrServerUrl);
		/* Add collection to solr index */
		if (checkSolrServerByPing(server)) {
			Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
			docs.add(doc);
			addToSolrIndex(server, docs);
		}
	}

	/**
	 * Adding documents collection to Solr Index
	 * This method was update in colaboration with S&P
	 * 
	 * @param SolrServerUrl Solr Server Url
	 * @param docs Collection<SolrInputDocument> collection of elements to include
	 * @throws SolrServerException
	 * @throws IOException
	 */
	public static void addToSolrIndex(String SolrServerUrl, Collection<SolrInputDocument> docs) throws SolrServerException, IOException {
		HttpSolrClient server = getHttpSolrServer(SolrServerUrl);
		/*Add collection to solr index*/		
		if (checkSolrServerByPing(server)) {
			addToSolrIndex(server, docs);
		}		
	}

	/**
	 * Deleting document from Solr Index.
	 * This method was update in colaboration with S&P
	 * 
	 * @param SolrServerUrl Solr Server Url
	 * @param id ID of the element to delete
	 * @throws SolrServerException
	 * @throws IOException
	 */
	public static void deleteFromSolrIndexById(HttpSolrClient solrServer, String id) throws SolrServerException, IOException {
		/* Delete collection from solr index */
		UpdateResponse rsp = solrServer.deleteById(id);
		Logger.debug(SolrUtil.class, "DELETING SOLR INDEX: " + rsp);
		/* Commit collection to solr index */
		UpdateResponse rsp2 = solrServer.commit();
		Logger.debug(SolrUtil.class, "COMMITING SOLR INDEX: " + rsp2);
	}


	/**
	 * Deleting document from Solr Index
	 * This method was update in colaboration with S&P
	 * 
	 * @param SolrServerUrl Solr Server Url
	 * @param id ID of the element to delete 
	 * @throws SolrServerException
	 * @throws IOException
	 */
	public static void deleteFromSolrIndexById(String SolrServerUrl, String id) throws SolrServerException, IOException {
		HttpSolrClient server = getHttpSolrServer(SolrServerUrl);
		/*Delete collection from solr index*/
		if (checkSolrServerByPing(server)) {
			deleteFromSolrIndexById(server, id);
		}		
	}

	/**
	 * Deleting document from Solr Index.
	 * This method was update in colaboration with S&P
	 * 
	 * @param SolrServerUrl Solr Server Url
	 * @param ids List od ID's of the elements to delete
	 * @throws SolrServerException
	 * @throws IOException
	 */
	public static boolean deleteFromSolrIndexById(HttpSolrClient solrServer, List<String> ids) {
		try {
			/* Add collection to solr index */
			UpdateResponse rsp = solrServer.deleteById(ids);
			Logger.debug(SolrUtil.class, "DELETING SOLR INDEX: " + rsp);
			/* Commit collection to solr index */
			UpdateResponse rsp2 = solrServer.commit();
			Logger.debug(SolrUtil.class, "COMMITING SOLR INDEX: " + rsp2);
			return true;
		} catch (Exception e) {
			Logger.error(SolrUtil.class, e.getMessage(), e);
			return false;
		}
	}

	/**
	 * Deleting document from Solr Index
	 * This method was update in colaboration with S&P
	 * 
	 * @param SolrServerUrl Solr Server Url
	 * @param ids List od ID's of the elements to delete 
	 * @throws SolrServerException
	 * @throws IOException
	 */
	public static boolean deleteFromSolrIndexById(String SolrServerUrl, List<String> ids) throws IOException {
		HttpSolrClient server = getHttpSolrServer(SolrServerUrl);
		/* Add collection to solr index */
		if (checkSolrServerByPing(server)) {
			return deleteFromSolrIndexById(server, ids);
		}
		return false;
	}

	/**
	 * Print documents and facets
	 * 
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	public static void print(QueryResponse response) {
		SolrDocumentList docs = response.getResults();
		if (docs != null) {
			Logger.debug(SolrUtil.class, docs.getNumFound() + " documents found, "+ docs.size() + " returned : ");
			for (int i = 0; i < docs.size(); i++) {
				SolrDocument doc = docs.get(i);
				Logger.debug(SolrUtil.class,"\t" + doc.toString());
			}
		}

		List<FacetField> fieldFacets = response.getFacetFields();
		if (fieldFacets != null && fieldFacets.isEmpty()) {
			for (FacetField fieldFacet : fieldFacets) {
				Logger.debug(SolrUtil.class,"\t" + fieldFacet.getName() + " :\t");
				if (fieldFacet.getValueCount() > 0) {
					for (Count count : fieldFacet.getValues()) {
						Logger.debug(SolrUtil.class,count.getName() + "["+ count.getCount() + "]\t");
					}
				}
				Logger.debug(SolrUtil.class,"");
			}
		}

		Map<String, Integer> queryFacets = response.getFacetQuery();
		if (queryFacets != null && !queryFacets.isEmpty()) {
			Logger.debug(SolrUtil.class,"\nQuery facets : ");
			for (String queryFacet : queryFacets.keySet()) {
				Logger.debug(SolrUtil.class,"\t" + queryFacet + "\t["+ queryFacets.get(queryFacet) + "]");
			}
			Logger.debug(SolrUtil.class,"");
		}

		NamedList<NamedList<Object>> spellCheckResponse = (NamedList<NamedList<Object>>) response.getResponse().get("spellcheck");

		if (spellCheckResponse != null) {
			Iterator<Entry<String, NamedList<Object>>> wordsIterator = spellCheckResponse.iterator();

			while (wordsIterator.hasNext()) {
				Entry<String, NamedList<Object>> entry = wordsIterator.next();
				String word = entry.getKey();
				NamedList<Object> spellCheckWordResponse = entry.getValue();
				boolean correct = spellCheckWordResponse.get("frequency").equals(1);
				Logger.debug(SolrUtil.class,"Word: " + word + ",\tCorrect?: " + correct);
				NamedList<Integer> suggestions = (NamedList<Integer>) spellCheckWordResponse.get("suggestions");
				if (suggestions != null && suggestions.size() > 0) {
					Logger.debug(SolrUtil.class,"Suggestions : ");
					Iterator<Entry<String, Integer>> suggestionsIterator = suggestions.iterator();
					while (suggestionsIterator.hasNext()) {
						Logger.debug(SolrUtil.class,"\t"+ suggestionsIterator.next().getKey());
					}
				}
				Logger.debug(SolrUtil.class,"");
			}
		}
	}

	/**
	 * Different types of searches
	 */

	/**
	 * Execute a search in the Solr index, passing all the parameter directly in a url query.
	 * For example, query:"indent=on&version=2.2&q=%2Bcat%3Aelectronics&fq=&start=0&rows=20&fl=*%2Cscore&qt=&wt=&explainOther=&hl.fl="
	 * 
	 * @param SolrServerUrl Solr Server Url
	 * @param query Solr query
	 * @return QueryResponse
	 * @throws SolrServerException
	 * @throws IOException 
	 */
	public static QueryResponse executeSolrGenericSearch(String SolrServerUrl, String query) throws SolrServerException, IOException {
		HttpSolrClient server = getHttpSolrServer(SolrServerUrl);
		SolrParams solrParams = SolrRequestParsers.parseQueryString(query);
		return server.query(solrParams);
	}

	/**
	 * Execute a search in the specified Solr index using a url parameter 
	 * 
	 * @param SolrServerUrl Solr Server Url
	 * @param start initial value to return
	 * @param rows number of row to return
	 * @param queryType  qt=spellcheck || qt=spellchecker (optional)
	 * @param facet Facet parameter. Values accepted "on" or "off"
	 * @param ident Ident parameter. Values accepted "on" or "off"
	 * @param query Solr query
	 * @param myCollection Solr collection name (optional)
	 * @param username Solr username (optional)
	 * @param password Solr password (optional)
	 * @return QueryResponse
	 * @throws SolrServerException
	 * @throws IOException 
	 */
	public static QueryResponse executeURLSolrParamsSearch(String SolrServerUrl, int start, int rows, String queryType,String facet, String ident, String query, String myCollection, String username, String password)
			throws SolrServerException, IOException {

		HttpSolrClient server = getHttpSolrServer(SolrServerUrl);

		StringBuffer request = new StringBuffer();
		if(UtilMethods.isSet(myCollection)){
			request.append("collectionName=" + myCollection);
		}
		if(UtilMethods.isSet(username)){
			request.append("&username=" + username);
		}
		if(UtilMethods.isSet(password)){
			request.append("&password=" + password);
		}
		if(UtilMethods.isSet(ident)){
			request.append("&indent="+ident);
		}
		if(UtilMethods.isSet(facet)){
			request.append("&facet=" + facet);
		}
		if(UtilMethods.isSet(queryType)){
			// qt=spellcheck || qt=spellchecker
			request.append("&qt="+queryType);
		}
		request.append("&q=" + query);
		request.append("&start=" + start);
		request.append("&rows=" + rows);
		SolrParams solrParams = SolrRequestParsers.parseQueryString(request.toString());

		return server.query(solrParams);
	}

	/**
	 * Execute a search in the specified Solr index using a ModifiableSolrParams
	 * 
	 * @param SolrServerUrl Solr Server Url
	 * @param start initial value to return
	 * @param rows number of row to return
	 * @param queryType  qt=spellcheck || qt=spellchecker (optional)
	 * @param facet Facet parameter. Values accepted "on" or "off"
	 * @param ident Ident parameter. Values accepted "on" or "off"
	 * @param query Solr query
	 * @param myCollection Solr collection name (optional)
	 * @param username Solr username (optional)
	 * @param password Solr password (optional)
	 * @return QueryResponse
	 * @throws SolrServerException
	 * @throws IOException 
	 */
	public static QueryResponse executeModifiableSolrParamsSearch(String SolrServerUrl, int start, int rows,String queryType, String facet, String ident, String query, String myCollection, String username, String password)
			throws SolrServerException, IOException {

		HttpSolrClient server = getHttpSolrServer(SolrServerUrl);

		ModifiableSolrParams solrParams = new ModifiableSolrParams();
		if(UtilMethods.isSet(myCollection)){
			solrParams.set("collectionName", myCollection);
		}
		if(UtilMethods.isSet(username)){
			solrParams.set("username", username);
		}
		if(UtilMethods.isSet(password)){
			solrParams.set("password", password);
		}
		if(UtilMethods.isSet(ident)){
			solrParams.set("indent", ident);
		}
		if(UtilMethods.isSet(facet)){
			solrParams.set("facet", facet);
		}
		if(UtilMethods.isSet(queryType)){
			// qt=spellcheck || qt=spellchecker
			solrParams.set("qt",queryType);
		}
		solrParams.set("q", query);
		solrParams.set("start", start);
		solrParams.set("rows", rows);
		return server.query(solrParams);
	}


	/**
	 * Execute a search in the specified Solr index using a SolrQuery
	 * 
	 * @param SolrServerUrl Solr Server Url
	 * @param start initial value to return
	 * @param rows number of row to return
	 * @param queryType  qt=spellcheck || qt=spellchecker (optional)
	 * @param facet Facet parameter. Values accepted "on" or "off"
	 * @param ident Ident parameter. Values accepted "on" or "off"
	 * @param query Solr query
	 * @param myCollection Solr collection name (optional)
	 * @param username Solr username (optional)
	 * @param password Solr password (optional)
	 * @return QueryResponse
	 * @throws SolrServerException
	 * @throws IOException 
	 */
	public static QueryResponse executeSolrQuerySearch(String SolrServerUrl, int start, int rows,String queryType, String facet, String ident, String query, String myCollection, String username, String password)
			throws SolrServerException, IOException {
		HttpSolrClient server = getHttpSolrServer(SolrServerUrl);

		SolrQuery solrQuery = new SolrQuery();
		if(UtilMethods.isSet(myCollection)){
			solrQuery.set("collectionName", myCollection);
		}
		if(UtilMethods.isSet(username)){
			solrQuery.set("username", username);
		}
		if(UtilMethods.isSet(password)){
			solrQuery.set("password", password);
		}
		if(UtilMethods.isSet(ident)){
			solrQuery.set("indent", ident);
		}
		if(UtilMethods.isSet(facet)){
			solrQuery.set("facet", facet);
		}
		if(UtilMethods.isSet(queryType)){
			// qt=spellcheck || qt=spellchecker
			solrQuery.setQuery(queryType);
		}
		solrQuery.setQuery(query);
		solrQuery.setStart(start);
		solrQuery.setRows(rows);
		return server.query(solrQuery);
	}

	private static final String PGVALIDATETABLESQL="SELECT COUNT(table_name) as exist FROM information_schema.tables WHERE Table_Name = 'solr_queue'";
	private static final String PGCREATESQL="CREATE TABLE solr_queue (id bigserial PRIMARY KEY NOT NULL, solr_operation int8, asset_identifier VARCHAR(36) NOT NULL, language_id  int8 NOT NULL, entered_date TIMESTAMP, last_try TIMESTAMP, num_of_tries int8 NOT NULL DEFAULT 0, in_error bool DEFAULT 'f', last_results TEXT)";
	private static final String MYCREATESQL="CREATE TABLE IF NOT EXISTS solr_queue (id BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL, solr_operation bigint, asset_identifier VARCHAR(36) NOT NULL, language_id bigint NOT NULL, entered_date DATETIME, last_try DATETIME, num_of_tries bigint NOT NULL DEFAULT 0, in_error varchar(1) DEFAULT '0', last_results LONGTEXT)";
	private static final String MSVALIDATETABLESQL="SELECT COUNT(*) as exist FROM sysobjects WHERE name = 'solr_queue'";
	private static final String MSCREATESQL="CREATE TABLE solr_queue (id bigint IDENTITY (1, 1)PRIMARY KEY NOT NULL, solr_operation numeric(19,0), asset_identifier VARCHAR(36) NOT NULL, language_id numeric(19,0) NOT NULL, entered_date DATETIME, last_try DATETIME, num_of_tries numeric(19,0) NOT NULL DEFAULT 0, in_error tinyint DEFAULT 0, last_results TEXT)";
	private static final String OCLVALIDATETABLESQL="SELECT COUNT(*) as exist FROM user_tables WHERE table_name='SOLR_QUEUE'";
	private static final String OCLCREATESQL="CREATE TABLE SOLR_QUEUE (id INTEGER NOT NULL, solr_operation number(19,0), asset_identifier VARCHAR(36) NOT NULL, language_id number(19,0) NOT NULL, entered_date DATE, last_try DATE, num_of_tries number(19,0) DEFAULT 0 NOT NULL, in_error number(1,0) DEFAULT 0, last_results NCLOB,PRIMARY KEY (id))";
	private static final String OCLCREATESEQSQL="CREATE SEQUENCE SOLR_QUEUE_SEQ START WITH 1 INCREMENT BY 1"; 
	private static final String OCLCREATETRIGERSQL="CREATE OR REPLACE TRIGGER SOLR_QUEUE_TRIGGER before insert on SOLR_QUEUE for each row begin select SOLR_QUEUE_SEQ.nextval into :new.id from dual; end;";

	/**
	 * Create dotcms Solr assets index table
	 * 
	 * @return boolean, true if the table was created successfully
	 */
	public static boolean createSolrTable(){
		try{
			DotConnect dc = new DotConnect();
			if(DbConnectionFactory.isPostgres()){
				/*Validate if the table doesn't exist then is created*/
				dc.setSQL(PGVALIDATETABLESQL);
				long existTable = (Long)dc.loadObjectResults().get(0).get("exist");

				if(existTable == 0){
					dc.setSQL(PGCREATESQL);
					dc.loadResult();	
				}
			}else if(DbConnectionFactory.isMySql()){
				dc.setSQL(MYCREATESQL);
				dc.loadResult();				
			}else if(DbConnectionFactory.isMsSql()){
				dc.setSQL(MSVALIDATETABLESQL);
				int existTable = (Integer)dc.loadObjectResults().get(0).get("exist");

				if(existTable == 0){
					dc.setSQL(MSCREATESQL);
					dc.loadResult();
				}
			}else{
				dc.setSQL(OCLVALIDATETABLESQL);
				BigDecimal existTable = (BigDecimal)dc.loadObjectResults().get(0).get("exist");
				if(existTable.longValue() == 0){
					dc.setSQL(OCLCREATESEQSQL);
					dc.loadResult();
					dc.setSQL(OCLCREATESQL);
					dc.loadResult();					
					dc.setSQL(OCLCREATETRIGERSQL);
					dc.loadResult();
				}
			}
			return true;
		}catch(Exception e){
			Logger.error(SolrUtil.class,e.getMessage(),e);
			return false;
		}finally{
			DbConnectionFactory.closeConnection();
		}
	}


	private static final String PGDELETESQL="DROP TABLE solr_queue";
	private static final String MYDELETESQL="DROP TABLE solr_queue";
	private static final String MSDELETESQL="DROP TABLE solr_queue";
	private static final String OCLDELETESQL="DROP TABLE SOLR_QUEUE";
	private static final String OCLDELETESEQSQL="DROP SEQUENCE SOLR_QUEUE_SEQ";
	private static final String OCLDELETETRIGGERSQL="DROP TRIGGER SOLR_QUEUE_TRIGGER";

	/**
	 * Delete dotcms Solr assets index table
	 * 
	 * @return boolean, true if the table was created successfully
	 */
	public static boolean deleteSolrTable(){
		try{
			DotConnect dc = new DotConnect();
			if(DbConnectionFactory.isPostgres()){
				dc.setSQL(PGDELETESQL);
				dc.loadResult();				
			}else if(DbConnectionFactory.isMySql()){
				dc.setSQL(MYDELETESQL);
				dc.loadResult();				
			}else if(DbConnectionFactory.isMsSql()){
				dc.setSQL(MSDELETESQL);
				dc.loadResult();
			}else{
				dc.setSQL(OCLDELETETRIGGERSQL);
				dc.loadResult();
				dc.setSQL(OCLDELETESEQSQL);
				dc.loadResult();
				dc.setSQL(OCLDELETESQL);
				dc.loadResult();							
			}
			return true;
		}catch(Exception e){
			Logger.error(SolrUtil.class,e.getMessage(),e);
			return false;
		}finally{
			DbConnectionFactory.closeConnection();
		}
	} 

	/**
	 * Validate if a FieldVariable is present in a FieldVariable List
	 * 
	 * @param list List<FieldVariable>
	 * @param fieldVariableName Variable Name
	 * @return boolean
	 */
	public static boolean containsFieldVariable(List<FieldVariable> list, String fieldVariableName){
		boolean containsVariable =false;
		if(!UtilMethods.isSet(fieldVariableName)){
			return false;
		}
		for(FieldVariable fv : list){
			if(fv.getKey().equals(fieldVariableName)){
				containsVariable= true;
				break;
			}
		}		
		return containsVariable;
	}

	/**
	 * Validate if a FieldVariable is present in a FieldVariable List
	 * 
	 * @param list List<FieldVariable>
	 * @param fieldVariableName List<FieldVariable> variable names
	 * @return boolean
	 */
	public static boolean containsFieldVariable(List<FieldVariable> list, List<String> fieldVariableNames){
		boolean containsVariable =false;
		if(!UtilMethods.isSet(fieldVariableNames)){
			return false;
		}
		for(FieldVariable fv : list){
			if(fieldVariableNames.contains(fv.getKey())){
				containsVariable= true;
				break;
			}
		}		
		return containsVariable;
	}

	/**
	 * Validate if a Metadata FieldVariable is present in a FieldVariable List
	 * @param list List<FieldVariable>
	 * @param field String with the metadata field name
	 * @return boolean
	 */
	public static boolean containsFieldVariableIgnoreField(List<FieldVariable> list,String dynamicIgnoreMetadaField, String field){
		boolean containsMetadataField =false;
		if(!UtilMethods.isSet(dynamicIgnoreMetadaField) || !UtilMethods.isSet(field)){
			return false;
		}
		for(FieldVariable fv : list){
			if(fv.getKey().equals(dynamicIgnoreMetadaField)){
				String[] values = fv.getValue().split(",");
				for(String val : values){
					if(field.equals(val.trim())){
						containsMetadataField= true;
						break;
					}
				}
			}
		}		 
		return containsMetadataField;
	}

	/**
	 * Get all the existing parents for a category
	 * 
	 * @param cat child category
	 * @param user current user
	 * @return Set<Category>
	 * @throws DotSolrException
	 */
	public static Set<Category> getAllParentsCategories(Category cat, User user) throws DotSolrException{
		Set<Category> results = new HashSet<Category>();
		List<Category> parents;
		try {
			parents = APILocator.getCategoryAPI().getParents(cat, user, false);
			results.addAll(parents);
			for(Category parent : parents){
				results.addAll(getAllParentsCategories(parent,user));
			}
		} catch (Exception e) {
			throw new DotSolrException(e.getMessage());
		}

		return results;
	}

	/**
	 * Check if the field contains the field attribute to modify the field name to use in Solr Index
	 * 
	 * @param list List<FieldVariable>
	 * @param fieldAttribute String name of the field with the new field name to use in Solr index
	 * @param defaultSolrFieldName String field velocity var name
	 * @return String
	 */
	public static String getSolrFieldName(List<FieldVariable> list, String fieldAttribute, String defaultSolrFieldName){
		for(FieldVariable fv : list){
			if(fv.getKey().equals(fieldAttribute)){
				String values = fv.getValue();
				return values;
			}
		}		 
		return defaultSolrFieldName;
	}

	/**
	 * Returns a map with the given file's meta data
	 * 
	 * @param f Field
	 * @param file
	 * @return
	 */
	public static Map<String, String> getMetaDataMap(Field f, File file)  {
		Map<String, String> metaMap = new HashMap<String, String>();
		Parser parser = getParser(file);
		Metadata met = new Metadata();
		//set -1 for no limit when parsing text content
		BodyContentHandler handler =  new BodyContentHandler(-1);
		ParseContext context = new ParseContext();
		InputStream fis = null;
		try {
			fis = new FileInputStream(file);
			parser.parse(fis,handler,met,context);
			metaMap = new HashMap<String, String>();

			Set<String> allowedFields=null;
			List<FieldVariable> fieldVariables=APILocator.getFieldAPI().getFieldVariablesForField(f.getInode(), APILocator.getUserAPI().getSystemUser(), false);
			for(FieldVariable fv : fieldVariables) {
				if(fv.getKey().equals("dotIndexPattern")) {
					String[] names=fv.getValue().split(",");
					allowedFields=new HashSet<String>();
					for(String n : names){
						allowedFields.add(n.trim());
					}
				}
			}

			for(int i = 0; i <met.names().length; i++) {
				String name = met.names()[i];
				if(UtilMethods.isSet(name) && met.get(name)!=null){
					// we will want to normalize our metadata for searching
					String[]x  = translateKey(name);
					for(String y : x){
						if(!UtilMethods.isSet(allowedFields) || allowedFields.contains(y))
							metaMap.put(y, met.get(name));	
					}
				}
			}
			if(handler!=null && UtilMethods.isSet(handler.toString())){
				metaMap.put(FileAssetAPI.CONTENT_FIELD, handler.toString());	
			}
		} catch (Exception e) {
			Logger.error(SolrUtil.class, "Could not parse file metadata for file : "+ file.getAbsolutePath()); 
		} finally{
			metaMap.put(FileAssetAPI.SIZE_FIELD,  String.valueOf(file.length()));
			if(fis!=null){
				try {
					fis.close();
				} catch (IOException e) {}
			}
		}

		return metaMap;
	}

	/**
	 * normalize metadata from various filetypes
	 * this method will return an array of metadata keys
	 * that we can use to normalize the values in our fileAsset metadata
	 * For example, tiff:ImageLength = "height" for image files, so 
	 * we return {"tiff:ImageLength", "height"} and both metadata
	 * are written to our metadata field
	 * 
	 * @param key
	 * @return
	 */
	private static String[] translateKey(String key){
		String[] x= getTranslationMap().get(key);
		if(x ==null){
			x = new String[]{StringUtils.sanitizeCamelCase(key)};
		}
		return x;
	}


	private static Map<String, String[]> translateMeta = null;

	/**
	 * Return file parser
	 * 
	 * @param binFile
	 * @return Parser
	 */
	@SuppressWarnings("unchecked")
	private static Parser getParser(File binFile) {
		String mimeType =  new MimetypesFileTypeMap().getContentType(binFile);
		String[] mimeTypes = Config.getStringArrayProperty("CONTENT_PARSERS_MIMETYPES");
		String[] parsers = Config.getStringArrayProperty("CONTENT_PARSERS");
		int index = Arrays.binarySearch(mimeTypes, mimeType);
		if(index>-1 && parsers.length>0){
			String parserClassName = parsers[index];
			Class<Parser> parserClass;
			try {
				parserClass = (Class<Parser>)Class.forName(parserClassName);
				return parserClass.newInstance();
			} catch(Exception e){
				Logger.warn(SolrUtil.class, "A content parser for mime type " + mimeType + " was found but could not be instantiated, using default content parser."); 
			}
		}
		return  new AutoDetectParser();
	}	

	/**
	 * Translate meta inf
	 * 
	 * @return Map<String, String[]>
	 */
	private static Map<String, String[]> getTranslationMap(){
		if(translateMeta ==null){
			synchronized ("translateMeta".intern()) {
				if(translateMeta ==null){
					translateMeta=	new HashMap<String, String[]>();
					translateMeta.put("tiff:ImageWidth"		, new String[]{"tiff:ImageWidth","width"});
					translateMeta.put("tiff:ImageLength"	, new String[]{"tiff:ImageLength","height"});
				}
			}
		}
		return translateMeta;
	}

	/**
	 * Added in plugin Update
	 */
	private static final String pluginId = "com.dotcms.solr";
	private static final String SOLR_SERVER_CONFIGURATION_FIELD="Solr Configuration";
	private static final String SOLR_SERVER_FIELD="Solr Servers";
	private static final String SOLR_SERVER_FIELD_VAR_NAME="solrServers";
	private static PluginAPI pluginAPI = APILocator.getPluginAPI();
	private static HostAPI hostAPI = APILocator.getHostAPI();

	/**
	 * Update the host structure including the Solr server field
	 * 
	 * @throws DotDataException 
	 * @throws NumberFormatException 
	 * @throws DotSecurityException 
	 */
	public static void UpdateHostTable() throws NumberFormatException, DotDataException, DotSecurityException{
		Structure host = StructureFactory.getStructureByVelocityVarName("Host");
		Field solrServers = host.getFieldVar(SOLR_SERVER_FIELD_VAR_NAME);
		String solrServersList = "";
		if(!UtilMethods.isSet(solrServers) || !UtilMethods.isSet(solrServers.getInode())){
			Field sectionDivider = new Field(SOLR_SERVER_CONFIGURATION_FIELD, Field.FieldType.LINE_DIVIDER, Field.DataType.SECTION_DIVIDER, host, false, false, false, 30, false, true, false);
			FieldFactory.saveField(sectionDivider);
			FieldsCache.clearCache();

			solrServers = new Field(SOLR_SERVER_FIELD,Field.FieldType.CUSTOM_FIELD,Field.DataType.LONG_TEXT,host,false,false,false,31, false, false, false);
			solrServers.setVelocityVarName(SOLR_SERVER_FIELD_VAR_NAME);			
			solrServers.setDefaultValue("");
			String value="<select id=\"solrServersTemp\" name=\"solrServersTemp\" onChange=\"updateSolrServer(this)\" multiple=\"multiple\" >\n";

			int serversNumber = Integer.parseInt(pluginAPI.loadProperty(pluginId, "com.dotcms.solr.SOLR_SERVER_NUMBER"));
			for(int server=0; server < serversNumber; server++ ){
				String solrServerUrl = pluginAPI.loadProperty(pluginId, "com.dotcms.solr."+server+".SOLR_SERVER");
				value+="<option value=\""+solrServerUrl+"\">"+solrServerUrl+"</option>\n";
				solrServersList+=","+solrServerUrl;
			}			

			value+="</select>\n<script type=\"text/javascript\">\n";
			value+="function initSolrServerField(){\n";
			value+="  var selectedValue= document.getElementById('solrServers').value;\n";
			value+="  var currentSelectedValues= selectedValue.split(',');\n";
			value+="  var selectField = document.getElementById('solrServersTemp');\n";
			value+="  for (j=0; j<currentSelectedValues.length; j++) {\n";
			value+="    for (i=0; i<selectField.options.length; i++) {\n";					
			value+="      if (selectField.options[i].value == currentSelectedValues[j]) {\n";
			value+="       selectField.options[i].selected=true;\n";
			value+="       break;\n";
			value+="      }\n";
			value+="    }\n";
			value+="  }\n";
			value+="}\n";
			value+="function updateSolrServer(selectField){\n";
			value+="  var selected='';\n";
			value+="  for (i=0; i<selectField.options.length; i++) {\n";
			value+="    if (selectField.options[i].selected) {\n";
			value+="      selected += ','+selectField.options[i].value;\n";					      
			value+="    }\n";
			value+="  }\n";
			value+="  if(selected != ''){\n";
			value+="    selected = selected.substring(1);\n";
			value+="  }\n";
			value+="  document.getElementById('solrServers').value=selected;\n";
			value+="}\n";
			value+="initSolrServerField();\n</script>";			
			solrServers.setValues(value);
			FieldFactory.saveField(solrServers);
			FieldsCache.clearCache();

			/*Update Host contentlet to look in all the solr servers*/
			boolean updateHost = Boolean.parseBoolean(pluginAPI.loadProperty(pluginId, "com.dotcms.solr.SET_ALL_SOLR_SERVERS_IN_HOSTS"));
			if(updateHost){
				if(UtilMethods.isSet(solrServersList)){
					solrServersList = solrServersList.substring(1);	
				}
				User user = APILocator.getUserAPI().getSystemUser();
				List<Host> allHosts = APILocator.getHostAPI().findAll(user, false);
				ContentletAPI conAPI = APILocator.getContentletAPI();
				for(Host currentHost : allHosts){
					if(!currentHost.isSystemHost()){
						Contentlet cont = conAPI.checkout(currentHost.getInode(), user, false);
						conAPI.setContentletProperty(cont, solrServers, solrServersList);
						cont = conAPI.checkin(cont, user, false);
						conAPI.publish(cont, user, false);
					}
				}
			}
		}
	}

	/**
	 * Get the list of Solr servers associated with that content
	 * 
	 * @param con Contentlet
	 * @param user 
	 * @return List<String>
	 */
	public static List<String> getContentletSolrServers(Contentlet con, User user){
		List<String> solarServers = new ArrayList<String>();
		Field solrSeversField = null;
		try{
			Host host = hostAPI.find(con.getHost(), user, false);
			solrSeversField = host.getStructure().getFieldVar(SOLR_SERVER_FIELD_VAR_NAME);
			String servers = (String)APILocator.getContentletAPI().getFieldValue(host, solrSeversField);
			if(UtilMethods.isSet(servers)){
				for(String server : servers.split(",")){
					solarServers.add(server);
				}
			}
		}catch(Exception e){
			Logger.error(SolrUtil.class, "Error getting contentlet Solr Servers associated."+e.getMessage(),e);			 
		}
		return solarServers;
	}

	/**
	 * Escape quotation marks so they work in javascript fields
	 */
	public static String escapeQuotes(String fixme) {
		String doubleQuote = "\"";

		String singleQuote = "'";
		String escapedSingleQuote = "\\'";

		if (fixme != null) {
			fixme = fixme.trim();

			try {
				// first replace double quotes with single quotes
				fixme = fixme.replaceAll(doubleQuote, singleQuote);

				// now escape all the single quotes
				fixme = fixme.replaceAll(singleQuote, escapedSingleQuote);

				return fixme;
			} catch (Exception e) {
				Logger.error(UtilMethods.class, "Could not parse string [" + fixme + "] for escaping quotes: " + e.toString(), e);
				return "";
			}
		} else {
			return "";
		}
	}

	/**
	 * Date Utils
	 */
	public static final String UTC_TIME_ZONE = "UTC";
	public static final String SOLR_DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	/**
	 * Return the date string using the specified format
	 * 
	 * @param dateForReference
	 * @param dateFormat
	 * @param timeZone
	 * @return String
	 */
	public static String getFormattedDateText(Date dateForReference, String dateFormat, String timeZone) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
		if (timeZone != null && timeZone.trim().length() > 0) {
			TimeZone timeZoneForReference = TimeZone.getTimeZone(timeZone);
			if (timeZoneForReference != null) {
				simpleDateFormat.setTimeZone(timeZoneForReference);
			}
		}
		return simpleDateFormat.format(dateForReference);
	}

	/**
	 * Return the date string using the generic format
	 * 
	 * @param dateForReference
	 * @param dateFormat
	 * @return String
	 */
	public static String getGenericFormattedDateText(Date dateForReference,	String dateFormat) {
		return getFormattedDateText(dateForReference, dateFormat, null);
	}

	/**
	 * Return the date string using the UTCDate format
	 * 
	 * @param dateForReference
	 * @param dateFormat
	 * @return String
	 */
	public static String getFormattedUTCDateText(Date dateForReference,	String dateFormat) {
		return getFormattedDateText(dateForReference, dateFormat, UTC_TIME_ZONE);
	}
}