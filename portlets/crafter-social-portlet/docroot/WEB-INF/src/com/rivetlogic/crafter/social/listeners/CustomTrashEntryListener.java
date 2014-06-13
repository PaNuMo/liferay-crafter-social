/**
 * Copyright (C) 2005-2014 Rivet Logic Corporation.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; version 3 of the License.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package com.rivetlogic.crafter.social.listeners;

import com.liferay.portal.ModelListenerException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PrefsPropsUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.model.BaseModelListener;
import com.liferay.portal.service.ClassNameLocalServiceUtil;
import com.liferay.portlet.journal.model.JournalArticle;
import com.liferay.portlet.trash.model.TrashEntry;
import com.rivetlogic.crafter.social.utils.CrafterConstants;
import com.rivetlogic.crafter.social.utils.CrafterManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.craftercms.security.authentication.impl.AuthenticationCookie;

public class CustomTrashEntryListener extends BaseModelListener<TrashEntry>{

	private Log LOG = LogFactoryUtil.getLog(CustomTrashEntryListener.class);
	private static final String USER_AGENT = "Mozilla/5.0";
	private static DefaultHttpClient client = new DefaultHttpClient();
	
	@Override
	public void onAfterRemove(TrashEntry trashEntry) throws ModelListenerException {	
		if(trashEntry.getClassNameId() == 
				ClassNameLocalServiceUtil.getClassNameId(JournalArticle.class)){
			try {
				init(trashEntry.getCompanyId());
				sendPost(sendGet(trashEntry.getTypeSettingsProperty("title")));			
			} catch (Exception e) {
				LOG.error(e.getMessage());
			}
		}
		super.onAfterRemove(trashEntry);
	}
	
	private void init(long companyId) {		
		try {
			CrafterManager.init();
			
			// add crafterAuthCookie to HTTP client
			client.getParams().setParameter(ClientPNames.COOKIE_POLICY, 
					CookiePolicy.BROWSER_COMPATIBILITY);
			client.setCookieStore(new BasicCookieStore());
			AuthenticationCookie authCookie = CrafterManager
					.getCrafterProfileCookie(PrefsPropsUtil.getString(
							companyId, CrafterConstants.PROFILE_ADMIN_KEY, 
							GetterUtil.getString(PropsUtil.get(CrafterConstants.PROFILE_ADMIN_KEY),  
									CrafterConstants.PROFILE_DEFAULT_USER)));
			
			BasicClientCookie cookie = new BasicClientCookie(
					AuthenticationCookie.COOKIE, authCookie.toCookieValue());
			cookie.setDomain(CrafterConstants.CRAFTER_DOMAIN);
			cookie.setExpiryDate(authCookie.getProfileOutdatedAfter());
			cookie.setPath(StringPool.SLASH);	
			client.getCookieStore().addCookie(cookie);
			
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}	
	}
	
	// HTTP GET request
	private JSONObject sendGet(String articleId) throws Exception {
		String url = CrafterConstants.SOCIAL_API_SEARCH_UGC_URL +
				"?regex=commentable-" + articleId;
		HttpGet request = new HttpGet(url);
 
		// add request header
		request.addHeader("User-Agent", USER_AGENT);
		
		HttpResponse response = client.execute(request);
 
		BufferedReader rd = new BufferedReader(
                       new InputStreamReader(response.getEntity().getContent()));
 
		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
				
		JSONObject jsonObject = JSONFactoryUtil.createJSONObject(result.toString()); 
		return jsonObject; 
	}
	
	// HTTP POST request
	private void sendPost(JSONObject jsonObject) throws Exception {	
		JSONArray jsonArray = jsonObject.getJSONArray("list");

		if(jsonArray.length() > 0){
			String url = CrafterConstants.SOCIAL_API_DELETE_URL;
			HttpPost post = new HttpPost(url);
	 
			// add header
			post.setHeader("User-Agent", USER_AGENT);
	 
			List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
			urlParameters.add(new BasicNameValuePair("tenant", 
					CrafterConstants.TENANT_NAME));
			
			// add ugc's ID's to delete them
			for(int i = 0; i < jsonArray.length(); i++){
				urlParameters.add(new BasicNameValuePair("ugcIds", 
						jsonArray.getJSONObject(i).getString("id")));
			}
			post.setEntity(new UrlEncodedFormEntity(urlParameters)); 
			client.execute(post);
		}
	}

}
