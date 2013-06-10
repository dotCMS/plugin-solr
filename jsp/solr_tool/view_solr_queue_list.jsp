<%@page import="com.dotcms.solr.util.SolrUtil"%>
<%@page import="com.dotmarketing.plugin.business.PluginAPI"%>
<%@page import="com.dotmarketing.util.URLEncoder"%>
<%@page import="java.util.Date"%>
<%@page import="com.dotmarketing.portlets.contentlet.business.ContentletAPI"%>
<%@page import="java.util.ArrayList"%>
<%@page import="com.liferay.portal.model.User"%>
<%@page import="com.dotmarketing.business.web.WebAPILocator"%>
<%@page import="com.dotmarketing.portlets.contentlet.model.Contentlet"%>
<%@page import="com.dotcms.solr.business.DotSolrException"%>
<%@page import="java.util.Map"%>
<%@page import="com.dotcms.solr.business.SolrAPI"%>
<%@page import="java.util.List"%>
<%@page import="com.dotmarketing.business.APILocator"%>
<%@page import="java.util.Calendar"%>
<%@page import="com.dotmarketing.util.UtilMethods"%>
<%@ page import="com.liferay.portal.language.LanguageUtil"%>
<style>
/*.solr_red {
 background-color: #CC9999;  
}
.solr_box { 
	color: white; 
	background-color: #ba2929; 
	width: 200px; 
	height: 50px; 
	padding: 10px; 
}
.solr_tcenter{
 	text-align: center;
 }
 .listingTableNoBorder {
    border: 0;
    font-size: 88%;
    padding: 5px;
    margin: 10px auto;
    width: 99%;
}*/
</style>
<script type="text/javascript">
   dojo.require("dijit.Tooltip");
</script>  
<%
User user = WebAPILocator.getUserWebAPI().getLoggedInUser(request);
ContentletAPI conAPI = APILocator.getContentletAPI();
if(user == null){
	response.setStatus(403);
	return;
}

SolrAPI solrAPI = SolrAPI.getInstance();  
PluginAPI pluginAPI = APILocator.getPluginAPI();
String showPendingsStr = request.getParameter("showPendings");
boolean showPendings = false;
if(UtilMethods.isSet(showPendingsStr)){
	showPendings=Boolean.parseBoolean(showPendingsStr);
}

String showErrorsStr = request.getParameter("showErrors");
boolean showErrors=false;
if(UtilMethods.isSet(showErrorsStr)){
	showErrors=Boolean.parseBoolean(showErrorsStr);
}

String sortBy = request.getParameter("sort");
if(!UtilMethods.isSet(sortBy)){
	sortBy="id asc";
}
String offset = request.getParameter("offset");
if(!UtilMethods.isSet(offset)){
	offset="0";
}
String limit = request.getParameter("limit");
if(!UtilMethods.isSet(limit)){
	limit=pluginAPI.loadProperty("com.dotcms.solr","com.dotcms.solr.RESULTS_PER_PAGE");
}
String query = request.getParameter("query");
if(!UtilMethods.isSet(query)){
	query="";
}

String nastyError = null;

boolean userIsAdmin = false;
if(APILocator.getRoleAPI().doesUserHaveRole(user, APILocator.getRoleAPI().loadCMSAdminRole())){
	userIsAdmin=true;
}

String layout = request.getParameter("layout");
if(!UtilMethods.isSet(layout)) {
	layout = "";
}

String referer = new URLEncoder().encode("/c/portal/layout?p_l_id=" + layout + "&p_p_id=EXT_SOLR_TOOL&");
List<Map<String,Object>> iresults =  null;
String counter =  "0";

boolean deleteQueueElements=false;
String deleteQueueElementsStr = request.getParameter("delete");
if(UtilMethods.isSet(deleteQueueElementsStr)){
	deleteQueueElements=true;	
}
String elementsToDelete=null;

try{
	if(deleteQueueElements){
		if(deleteQueueElementsStr.equals("all")){
			solrAPI.deleteAllElementsFromSolrQueueTable();
		}else{
			for(String id : deleteQueueElementsStr.split(",")){
				solrAPI.deleteElementFromSolrQueueTable(Long.parseLong(id.trim()));
			}
		}
	}
	
	if(showPendings && showErrors){
		iresults =  solrAPI.getSolrQueueContentletsPaginated(query, sortBy, offset, limit);
		counter =  solrAPI.getSolrQueueContentletsCounter(query).get(0).get("count").toString();
	}else if (showPendings) {
		iresults = solrAPI.getSolrQueueContentletToProcessPaginated(query, sortBy, offset, limit);
		counter =  solrAPI.getSolrQueueContentletToProcessCounter(query).get(0).get("count").toString();
	}else if(showErrors) {
		iresults = solrAPI.getQueueErrorsPaginated(query, sortBy, offset, limit);	
		counter =  solrAPI.getQueueErrorsCounter(query).get(0).get("count").toString();
	}else {
		iresults =  new ArrayList();
		counter="0";
	}
}catch(DotSolrException e){
	iresults = new ArrayList();
	nastyError = e.toString();
}catch(Exception pe){
	iresults = new ArrayList();
	nastyError = pe.toString();
}
%>
<script type="text/javascript">
 function solrQueueCheckUncheckAll(){
	   var check=false;
	   if(dijit.byId("queue_all").checked){
		   check=true;
	   }
	   var nodes = dojo.query('.queue_to_delete');
	   dojo.forEach(nodes, function(node) {
		    dijit.getEnclosingWidget(node).set("checked",check);
	   }); 
   }
   function doQueuePagination(offset,limit) {		
		var url="layout=<%=layout%>&showPendings=<%=showPendings%>&showErrors=<%=showErrors%>&query=<%=query%>&sort=<%=sortBy%>";
		url+="&offset="+offset;
		url+="&limit="+limit;		
		refreshQueueList(url);
	}
   
   function deleteQueue(){
	   var url="layout=<%=layout%>&showPendings=<%=showPendings%>&showErrors=<%=showErrors%>&query=<%=query%>&sort=<%=sortBy%>&offset=0&limit=<%=limit%>";	
		if(dijit.byId("queue_all").checked){
			url+="&delete=all";
		}else{
			var ids="";
			var nodes = dojo.query('.queue_to_delete');
			   dojo.forEach(nodes, function(node) {
				   if(dijit.getEnclosingWidget(node).checked){
					   ids+=","+dijit.getEnclosingWidget(node).value; 
				   }
			   });
			if(ids != ""){   
				url+="&delete="+ids.substring(1);
			}
		}
		refreshQueueList(url);	   
   }
</script>
<%if(UtilMethods.isSet(nastyError)){%>
		<dl>
			<dt style='color:red;'><%= LanguageUtil.get(pageContext, "SOLR_Query_Error") %> </dt>
			<dd><%=nastyError %></dd>
		</dl>
<%}else if(iresults.size() >0){ %>										
	<table class="listingTable shadowBox">
		<tr>
			<th><input dojoType="dijit.form.CheckBox" type="checkbox" name="queue_all" value="all" id="queue_all" onclick="solrQueueCheckUncheckAll()" /></th>		
			<th><strong><%= LanguageUtil.get(pageContext, "SOLR_Identifier") %></strong></th>	
			<th style="width:40px"><strong><%= LanguageUtil.get(pageContext, "SOLR_Operation_Type") %></strong></th>
			<th colspan="2"><strong><%= LanguageUtil.get(pageContext, "SOLR_Date_Entered") %></strong></th>			
		</tr>
		<% for(Map<String,Object> c : iresults) {
			String errorclass="";
			if(UtilMethods.isSet(c.get("last_results"))){
				errorclass="class=\"solr_red\"";				 
			}
		%>
			<tr <%=errorclass%>>
				<td><input dojoType="dijit.form.CheckBox" type="checkbox" class="queue_to_delete" name="queue_to_delete" value="<%=c.get("id") %>" id="queue_to_delete_<%=c.get("id") %>" /></td>
				<%try{
					Contentlet con = conAPI.findContentletByIdentifier((String)c.get("asset_identifier"),true,Long.parseLong(c.get("language_id").toString()),user, false);
				%>
				<td><a href="/c/portal/layout?p_l_id=<%=layout%>&p_p_id=EXT_11&p_p_action=1&p_p_state=maximized&p_p_mode=view&_EXT_11_struts_action=/ext/contentlet/edit_contentlet&_EXT_11_cmd=edit&inode=<%=con.getInode() %>&referer=<%=referer %>"><%=con.getTitle()%></a></td>
				<%
				}catch(Exception e){
					nastyError=e.getMessage();
				%>
				<td><%=c.get("asset_identifier") %></td>
				<%} %>
				<td style="width:40px"><img class="center" src="/html/images/icons/<%=(c.get("solr_operation").toString().equals("1")?"plus.png":"cross.png")%>"/></td>
			    <td><%=UtilMethods.dateToHTMLDate((Date)c.get("entered_date"),"MM/dd/yyyy hh:mma") %></td>
			    <%if(UtilMethods.isSet(c.get("last_results"))){ %>
			    	<td id="error_<%=c.get("id")%>"><%= LanguageUtil.get(pageContext, "SOLR_Failed")%> <%=c.get("num_of_tries")%> <%= LanguageUtil.get(pageContext, "SOLR_Num_Times")+UtilMethods.shortenString(c.get("last_results").toString(),30)%></td>
			    	<script type="text/javascript">
			    		new dijit.Tooltip({
					     	connectId: ["error_<%=c.get("id")%>"],
					     	label: "<%=SolrUtil.escapeQuotes(UtilMethods.javaScriptify((String)c.get("last_results")))%>"
					  	});
			    	</script>
			    <%}else{ %>
			    	<td><%= LanguageUtil.get(pageContext, "SOLR_Pending")%></td>
			    <%} %>
			</tr>
		<%}%>
	</table>
	<table class="solr_listingTableNoBorder">
		<tr>
			<%
			long begin=Long.parseLong(offset);
			long end = Long.parseLong(offset)+Long.parseLong(limit);
			long total = Long.parseLong(counter);
			if(begin > 0){ 
				long previous=(begin-Long.parseLong(limit));
				if(previous < 0){
					previous=0;					
				}
			%>
			<td style="width:130px"><button dojoType="dijit.form.Button" onClick="doQueuePagination(<%=previous%>,<%=limit%>);return false;" iconClass="previousIcon"><%= LanguageUtil.get(pageContext, "SOLR_Previous") %></button></td>
			<%}else{ %>
			<td style="width:130px">&nbsp;</td>
			<%} %>
			<td class="solr_tcenter"><strong> <%=begin+1%> - <%=end < total?end:total%> <%= LanguageUtil.get(pageContext, "SOLR_Of") %> <%=total%> </strong></td>
			<%if(end < total){ 
				long next=(end < total?end:total);
			%>
			<td style="width:130px"><button class="solr_right" dojoType="dijit.form.Button" onClick="doQueuePagination(<%=next%>,<%=limit%>);return false;" iconClass="nextIcon"><%= LanguageUtil.get(pageContext, "SOLR_Next") %></button></td>
			<%}else{ %>
			<td style="width:130px">&nbsp;</td>
			<%} %>
		</tr>
	</table>
<% }else{ %>
	<table class="listingTable shadowBox">
		<tr>
			<th style="width:30px">&nbsp;</th>		
			<th><strong><%= LanguageUtil.get(pageContext, "SOLR_Identifier") %></strong></th>	
			<th style="width:40px"><strong><%= LanguageUtil.get(pageContext, "SOLR_Operation_Type") %></strong></th>
			<th colspan="2"><strong><%= LanguageUtil.get(pageContext, "SOLR_Date_Entered") %></strong></th>			
		</tr>
		<tr>
			<td colspan="5" class="solr_tcenter"><%= LanguageUtil.get(pageContext, "SOLR_No_Results") %></td>
		</tr>
	</table>
<%} %>