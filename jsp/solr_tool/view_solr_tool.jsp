<%@page import="com.liferay.portal.util.WebKeys"%>
<%@page import="com.dotmarketing.business.Layout"%>
<%@page import="com.dotmarketing.util.UtilMethods"%>
<%@page import="com.liferay.portal.model.User"%>
<%@page import="com.dotmarketing.business.web.WebAPILocator"%>
<%@page import="com.dotmarketing.util.URLEncoder"%>
<%@ page import="com.liferay.portal.language.LanguageUtil"%>
<html xmlns="http://www.w3.org/1999/xhtml">

<%
User user = WebAPILocator.getUserWebAPI().getLoggedInUser(request);
if(user == null){
	response.setStatus(403);
	return;
}

Layout layoutOb = (Layout) request.getAttribute(WebKeys.LAYOUT);
String layout = null;
if (layoutOb != null) {
	layout = layoutOb.getId();
}

%>
<style>
/* to remove when dotcms backend css code is added*/
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
 .solr_listingTableNoBorder {
    border: 0;
    font-size: 88%;
    padding: 5px;
    margin: 10px auto;
    width: 99%;
}
 .solr_right{
 	float:right;
 }
 
 #menu ul.dropdown li.dotCMS_EXT_SOLR_TOOL a span {
 background: url(/html/plugins/com.dotcms.solr/solr_tool/images/solr.gif) no-repeat 0 0
 }*/
</style>
<script type="text/javascript">
	dojo.require("dijit.form.NumberTextBox");
    dojo.require("dojox.layout.ContentPane");
	
	function doQueueFilter () {
	
		var url="";
		if(dijit.byId("showPendings").checked){
			url="&showPendings=true";
		}
		if(dijit.byId("showErrors").checked){
			url+="&showErrors=true";
		}
		if(url==""){
			dijit.byId("showPendings").setValue(true);
			url="showPendings=true";
		}
		url="layout=<%=layout%>"+url;
		refreshQueueList(url);
	}
	var lastUrlParams ;
	
	function refreshQueueList(urlParams){
		lastUrlParams = urlParams;
		var url = "/html/plugins/com.dotcms.solr/solr_tool/view_solr_queue_list.jsp?"+ urlParams;		
		
		var myCp = dijit.byId("solrToolCp");	
		
		if (myCp) {
			myCp.destroy();
			//myCp.destroyRecursive(true);
		}
		myCp = new dojox.layout.ContentPane({
			id : "solrToolCp"
		}).placeAt("queue_results");

		myCp.attr("href", url);
		myCp.refresh();
	}
	
	function doLuceneFilter () {
		
		var url="";
		url="&query="+encodeURIComponent(dijit.byId("query").value);
		url+="&sort="+dijit.byId("sort").value;
		url="layout=<%=layout%>"+url;
		refreshLuceneList(url);
	}
	
	var lastLuceneUrlParams ;
	
	function refreshLuceneList(urlParams){
		lastLuceneUrlParams = urlParams;
		var url = "/html/plugins/com.dotcms.solr/solr_tool/view_solr_content_list.jsp?"+ urlParams;		
		
		var myCp = dijit.byId("solrLuceneToolCp");	
		
		if (myCp) {
			myCp.destroy();
			//myCp.destroyRecursive(true);
		}
		
		myCp = new dojox.layout.ContentPane({
			id : "solrLuceneToolCp"
		}).placeAt("lucene_results");

		myCp.attr("href", url);
		myCp.refresh();
	}
	function loadSolrServers(){
		var url = "/html/plugins/com.dotcms.solr/solr_tool/view_solr_servers.jsp";		
		
		var myCp = dijit.byId("solrServersToolCp");	
		
		if (myCp) {
			myCp.destroy();
			//myCp.destroyRecursive(true);
		}
		myCp = new dojox.layout.ContentPane({
			id : "solrServersToolCp"
		}).placeAt("solr_servers");

		myCp.attr("href", url);
		myCp.refresh();
	}
</script>
<div class="portlet-wrapper">
	<div>
		<img src="/html/plugins/com.dotcms.solr/solr_tool/images/solr.gif"> <%= LanguageUtil.get(pageContext, "SOLR_Manager") %>
		<hr/>
	</div>
	<div id="mainTabContainer" dojoType="dijit.layout.TabContainer" dolayout="false">
  		<div id="search" dojoType="dijit.layout.ContentPane" title="<%= LanguageUtil.get(pageContext, "SOLR_Search") %>" >
  			<div>
				<dl>	
					<dt><strong><%= LanguageUtil.get(pageContext, "SOLR_Lucene_Query") %> </strong></dt>
					<dd>
						<textarea dojoType="dijit.form.Textarea" name="query" style="width:500px;min-height: 150px;" id="query" type="text"></textarea>
					</dd>
					<dt><strong><%= LanguageUtil.get(pageContext, "SOLR_Sort") %> </strong></dt><dd><input name="sort" id="sort" dojoType="dijit.form.TextBox" type="text" value="modDate" size="10" /></dd>
					<dt></dt><dd><button dojoType="dijit.form.Button" onClick="doLuceneFilter();" iconClass="searchIcon"><%= LanguageUtil.get(pageContext, "SOLR_Search_Content") %></button></dd>
				</dl>
			</div>
			<hr>
			<div>&nbsp;</div>
			<div id="lucene_results">
			</div>
		</div>	
  		<div id="queue" dojoType="dijit.layout.ContentPane" title="<%= LanguageUtil.get(pageContext, "SOLR_Queue") %>" >
  		    <div>
				<button dojoType="dijit.form.Button" onClick="deleteQueue();" iconClass="deleteIcon">
					<%= LanguageUtil.get(pageContext, "SOLR_Delete_from_queue") %> 
				</button>
				&nbsp;&nbsp;<%= LanguageUtil.get(pageContext, "SOLR_Show") %> 
				<input dojoType="dijit.form.CheckBox" checked="checked" type="checkbox" name="showPendings" value="true" id="showPendings" onclick="doQueueFilter()" /> <label for="showPendings"><%=LanguageUtil.get(pageContext, "SOLR_Queue_Pending")%></label> 
				<input dojoType="dijit.form.CheckBox" checked="checked" type="checkbox" name="showErrors" value="true" id="showErrors"  onclick="doQueueFilter()"  /> <label for="showErrors"><%=LanguageUtil.get(pageContext, "SOLR_Queue_Error")%></label>
				<button class="solr_right" dojoType="dijit.form.Button" onClick="doQueueFilter();" iconClass="resetIcon">
					<%= LanguageUtil.get(pageContext, "SOLR_Refresh") %> 
				</button> 
			</div>			
			<hr>
			<div>&nbsp;</div>
  			<div id="queue_results">
			</div>
			<script type="text/javascript">
			dojo.ready(function(){
				doQueueFilter();
			});
			</script>
  		</div>
  		<div id="instances" dojoType="dijit.layout.ContentPane" title="<%= LanguageUtil.get(pageContext, "SOLR_Servers") %>" >
  			<div>
				<%= LanguageUtil.get(pageContext, "SOLR_Servers_Intro") %> 
			</div>			
			<hr>
			<div>&nbsp;</div>
  			<div id="solr_servers">
			</div>
			<script type="text/javascript">
			dojo.ready(function(){
				loadSolrServers();
			});
			</script>
  		</div>
	</div>
</div>