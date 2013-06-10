package com.dotcms.solr.business;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.dotmarketing.portlets.contentlet.model.Contentlet;

/**
 * This class allow to add/update, delete and search error in the solr_queue table
 * from the actionlets
 * @author Oswaldo
 *
 */
public abstract class SolrAPI {

	/*Basic operations*/
	public static final long PROCESSED_SOLR_ELEMENT=0;
	public static final long ADD_OR_UPDATE_SOLR_ELEMENT=1;
	public static final long DELETE_SOLR_ELEMENT=2;
	
	private static SolrAPI solrAPI = null;
	public static SolrAPI getInstance(){
		if(solrAPI == null){
			solrAPI = SolrAPIImpl.getInstance();
		}
		return solrAPI;	
	}

	/**
	 * Include in the solr_queue table the content to add or update in the Solr Index
	 * @param con Contentlet
	 */
	public abstract void addContentToSolr(Contentlet con) throws DotSolrException;

	/**
	 * Include in the solr_queue table the content to remove in the Solr Index
	 * @param con Contentlet
	 */
	public abstract void removeContentFromSolr(Contentlet con) throws DotSolrException;
	
	/**
	 * Include in the solr_queue table the content to remove in the Solr Index
	 * @param identifier Contentlet identifier
	 * @param languageId contentlet languageId
	 */
	public abstract void removeContentFromSolr(String identifier, long languageId) throws DotSolrException;

	/**
	 * Get a list of all the elements in the solr_queue table that could be processes because some error
	 * @return List<Map<String,Object>>
	 */
	public abstract List<Map<String,Object>> getQueueErrors() throws DotSolrException;

	/**
	 * Get the total of all the elements in the solr_queue table that could be processes because some error
	 * @param condition WHERE condition
	 * @return List<Map<String,Object>>
	 * @throws DotSolrException
	 */
	public abstract List<Map<String,Object>> getQueueErrorsCounter(String condition) throws DotSolrException;
	
	/**
	 * Get a list of all the elements in the solr_queue table that could be processes because some error
	 * @param condition WHERE condition
	 * @param orderBy ORDER BY condition
	 * @param offset first row to return
	 * @param limit max number of rows to return
	 * @return List<Map<String,Object>>
	 * @throws DotSolrException
	 */
	public abstract List<Map<String,Object>> getQueueErrorsPaginated(String condition, String orderBy, String offset, String limit) throws DotSolrException;
	
	/**
	 * Get the total of All the Assets in the solr_queue table paginated
	 * @param condition WHERE condition
	 * @return List<Map<String,Object>>
	 * @throws DotSolrException
	 */
	public abstract List<Map<String,Object>> getSolrQueueContentletsCounter(String condition) throws DotSolrException;
	
	/**
	 * Get All the Assets in the solr_queue table paginated
	 * @param condition WHERE condition
	 * @param orderBy ORDER BY condition
	 * @param offset first row to return
	 * @param limit max number of rows to return
	 * @return List<Map<String,Object>>
	 * @throws DotSolrException
	 */
	public abstract List<Map<String,Object>> getSolrQueueContentletsPaginated(String condition, String orderBy, String offset, String limit) throws DotSolrException;
	
	/**
	 * Get the total of Assets not processed yet to update the Solr index paginated
	 * @param condition WHERE condition
	 * @return List<Map<String,Object>>
	 * @throws DotSolrException
	 */
	public abstract List<Map<String,Object>> getSolrQueueContentletToProcessCounter(String condition) throws DotSolrException;
	
	/**
	 * Get the Assets not processed yet to update the Solr index paginated
	 * @param condition WHERE condition
	 * @param orderBy ORDER BY condition
	 * @param offset first row to return
	 * @param limit max number of rows to return
	 * @return List<Map<String,Object>>
	 * @throws DotSolrException
	 */
	public abstract List<Map<String,Object>> getSolrQueueContentletToProcessPaginated(String condition, String orderBy, String offset, String limit) throws DotSolrException;
	
	/**
	 * Get the Assets not processed yet to update the Solr index
	 * @return List<Map<String,Object>>
	 */
	public abstract List<Map<String,Object>> getSolrQueueContentletToProcess() throws DotSolrException;
	
	/**
	 * update element from solr_queue table by id
	 * @param id ID of the element in the solr_queue
	 * @param next_try date of the next intent to execute the query
	 * @param in_error bolean indication if there was an error
	 * @param last_results error message
	 * @return boolean
	 */
	public abstract void updateElementStatusFromSolrQueueTable(long id, Date last_try,int num_of_tries, boolean in_error,String last_results ) throws DotSolrException;
	
	/**
	 * Delete element from solr_queue table by id
	 * @param id ID of the element in the table
	 * @return boolean
	 */
	public abstract void deleteElementFromSolrQueueTable(long id) throws DotSolrException;
	
	/**
	 * Delete all elements from solr_queue table
	 * @return boolean
	 */
	public abstract void deleteAllElementsFromSolrQueueTable() throws DotSolrException;
}
