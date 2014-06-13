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

package com.rivetlogic.crafter.social.events;

import com.liferay.portal.kernel.events.Action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.RoleConstants;
import com.liferay.portal.model.User;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;
import com.rivetlogic.crafter.social.utils.CrafterConstants;
import com.rivetlogic.crafter.social.utils.CrafterManager;

import org.craftercms.profile.constants.ProfileConstants;
import org.craftercms.profile.exceptions.AppAuthenticationException;
import org.craftercms.profile.impl.domain.Profile;
import org.craftercms.security.api.RequestContext;
import org.craftercms.security.authentication.impl.AuthenticationCookie;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CrafterLoginPostAction extends Action {
	
	private Log LOG = LogFactoryUtil.getLog(CrafterLoginPostAction.class);
	private Random randomGenerator = new Random(Calendar.getInstance().getTimeInMillis());	
	
	/* (non-Java-doc)
	 * @see com.liferay.portal.kernel.events.Action#Action()
	 */
	public CrafterLoginPostAction() {
		super();
	}

	/* (non-Java-doc)
	 * @see com.liferay.portal.kernel.events.Action#run(HttpServletRequest arg0, HttpServletResponse arg1)
	 */
	public void run(HttpServletRequest request, HttpServletResponse response) 
			throws ActionException {
		
		try {
			User user = PortalUtil.getUser(request);
			CrafterManager.init();
			processSuccessfulAuthentication(request, user); 
			saveOrUpdateCrafterProfileCookie(request, response, 
					CrafterManager.getCrafterProfileCookie(user.getScreenName()));
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}	
	}
	
	
	/**
	 * Crafter profile authentication
	 * @param user
	 * @throws Exception
	 */
	public void processSuccessfulAuthentication(HttpServletRequest request, User user) 
			throws Exception {	
		// See if this user is in Crafter profile, if not then add
		Profile profile = getCrafterProfile(user.getScreenName());
		if (profile == null) {
			Role adminRole = RoleLocalServiceUtil.getRole(
					PortalUtil.getCompanyId(request), RoleConstants.ADMINISTRATOR);
			Boolean isAdmin = user.getRoles().contains(adminRole);
			
			profile = createProfile(user.getScreenName(), user.getEmailAddress(),
					user.getFirstName(), user.getLastName(), user.getFullName(), isAdmin);
			CrafterManager.profileClient.activeProfile(CrafterManager.appToken, profile.getId(), Boolean.TRUE);
		}
	}
	
	/**
	 * Look for Crafter Profile by username
	 * @param username
	 * @return
	 * @throws Exception
	 */
	public Profile getCrafterProfile(String username) throws Exception {
		
		LOG.info(String.format("Looking up profile for user %s.", username));
		Profile result = null;

		try {
			result = CrafterManager.profileClient.getProfileByUsernameWithAllAttributes(
					CrafterManager.appToken, username, CrafterConstants.TENANT_NAME);
		} catch (AppAuthenticationException aae) {
			LOG.error(aae.getMessage());
		}

		return result;
	}
	
	/**
	 * Create profile on Crafter Profile
	 * @param screenName
	 * @param email
	 * @param firstName
	 * @param lastName
	 * @param fullName
	 * @return
	 * @throws Exception
	 */
	public Profile createProfile(String screenName, String email, String firstName, 
			String lastName, String fullName, Boolean isAdmin) throws Exception {
		
		Profile result = null;
		
		// Create the profile with any preset attributes we need.
		LOG.info(String.format("Generating Crafter Profile for %s.", fullName));
		Map<String, Serializable> queryParams = generateInitialProfileQueryParams(isAdmin);
		queryParams.put(CrafterConstants.ATTRIBUTE_DISPLAY_NAME, firstName + " " + lastName);
		queryParams.put(CrafterConstants.ATTRIBUTE_FIRST_NAME, firstName);
		queryParams.put(CrafterConstants.ATTRIBUTE_LAST_NAME, lastName);
		queryParams.put(CrafterConstants.ATTRIBUTE_FULL_NAME, fullName);
		
		try {
			result = CrafterManager.profileClient.createProfile(CrafterManager.appToken, screenName, 
					Double.toString(randomGenerator.nextDouble()), true, 
					CrafterConstants.TENANT_NAME, email, queryParams);
		} catch (AppAuthenticationException aae) {
			LOG.error(aae.getMessage());
		}
		
		return result;
	}	

	
	/**
	 * This is to generate the default query params used in a crafter-profile being created.
	 * @return
	 */
	private Map<String, Serializable> generateInitialProfileQueryParams(Boolean isAdmin) {
		Map<String, Serializable> resultMap = new HashMap<String, Serializable>();

		// Set an initial ROLE the user will have in Social.
		ArrayList<String> rolesList = new ArrayList<String>();
		if(isAdmin){
			rolesList.add("SOCIAL_ADMIN");
		}
		else{
			rolesList.add("SOCIAL_USER");
			rolesList.add("SOCIAL_AUTHOR");
		}
		
		resultMap.put(ProfileConstants.ROLES, rolesList);
		resultMap.put(CrafterConstants.ATTRIBUTE_ANONYMOUS_POSTER, "false");

		return resultMap;
	}
    
	public void saveOrUpdateCrafterProfileCookie(HttpServletRequest request, 
			HttpServletResponse response, AuthenticationCookie cookie) {
		// Create a context.
		RequestContext context = new RequestContext();
		context.setRequest(request);
		context.setResponse(response);

		// Set the cookie to the servlet response.
		cookie.save(context, CrafterConstants.COOKIE_MAX_AGE);
	}

}