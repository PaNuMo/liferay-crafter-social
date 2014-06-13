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

package com.rivetlogic.crafter.social.hook.action;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.struts.BaseStrutsPortletAction;
import com.liferay.portal.kernel.struts.StrutsPortletAction;
import com.liferay.portal.kernel.util.PropertiesParamUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.WebKeys;
import com.rivetlogic.crafter.social.utils.CrafterConstants;
import com.rivetlogic.crafter.social.utils.CrafterManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.craftercms.profile.constants.ProfileConstants;
import org.craftercms.profile.exceptions.AppAuthenticationException;
import org.craftercms.profile.impl.domain.Profile;

public class EditCompanyHookAction extends BaseStrutsPortletAction {
	
	Log LOG = LogFactoryUtil.getLog(EditCompanyHookAction.class);	

	@Override
	public void processAction(StrutsPortletAction originalStrutsPortletAction,
			PortletConfig portletConfig, ActionRequest actionRequest,
			ActionResponse actionResponse) throws Exception {
		
		UnicodeProperties properties = PropertiesParamUtil.getProperties(
				actionRequest, "settings--");
		
		String crafterProfileUsername = properties.getProperty(
					"crafter_profile_admin.username");
		
		CrafterManager.init();
		Profile profile = getCrafterProfile(crafterProfileUsername);
		
		if(profile == null){
			String crafterProfilePassword = properties.getProperty(
							"crafter_profile_admin.password");
			profile = createProfile(crafterProfileUsername, crafterProfilePassword);
			CrafterManager.profileClient.activeProfile(CrafterManager.appToken, 
					profile.getId(), Boolean.TRUE);
		}
		else if (profile.getRoles().contains("SUPERADMIN")){
			SessionErrors.add(actionRequest, "username-already-exists-on-crafter-profile");
		}
		
		originalStrutsPortletAction.processAction(originalStrutsPortletAction, 
				portletConfig, actionRequest, actionResponse);
	}
	
	@Override
	public String render(StrutsPortletAction originalStrutsPortletAction,
			PortletConfig portletConfig, RenderRequest renderRequest,
			RenderResponse renderResponse) throws Exception {
		String ret = originalStrutsPortletAction.render(null, portletConfig, renderRequest, renderResponse);
		renderRequest.setAttribute(WebKeys.PORTLET_DECORATE, Boolean.TRUE);
		return ret;
	}
	
	private Profile getCrafterProfile(String username) {		
	    Profile result = null;
		try {
			result = CrafterManager.profileClient.getProfileByUsernameWithAllAttributes(
					CrafterManager.appToken, username, CrafterConstants.TENANT_NAME);
		} catch (AppAuthenticationException aae) {
			LOG.error(aae.getLocalizedMessage());
		}
		return result;
	}
	
	
	public Profile createProfile(String username, String password) throws Exception {	
		Profile result = null;
		LOG.info(String.format("Generating Crafter Admin Profile for %s.", username));
		Map<String, Serializable> queryParams = new HashMap<String, Serializable>();
		ArrayList<String> rolesList = new ArrayList<String>();
		
		rolesList.add("SUPERADMIN");
		queryParams.put(ProfileConstants.ROLES, rolesList);
		queryParams.put(CrafterConstants.ATTRIBUTE_ANONYMOUS_POSTER, "false");
		queryParams.put(CrafterConstants.ATTRIBUTE_DISPLAY_NAME, username);
		
		try {
			result = CrafterManager.profileClient.createProfile(
					CrafterManager.appToken, username, password, true, 
					CrafterConstants.TENANT_NAME, username + "@liferay.com", queryParams);
		} catch (AppAuthenticationException aae) {
			LOG.error(aae.getMessage());
		}
		
		return result;
	}	
	
}