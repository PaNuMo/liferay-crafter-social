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

package com.rivetlogic.crafter.social.utils;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Http;
import com.rivetlogic.crafter.social.events.CrafterLoginPostAction;

import java.io.File;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.craftercms.profile.api.ProfileClient;
import org.craftercms.profile.exceptions.AppAuthenticationException;
import org.craftercms.profile.impl.ProfileRestClientImpl;
import org.craftercms.security.api.RequestContext;
import org.craftercms.security.authentication.impl.AuthenticationCookie;
import org.craftercms.security.authentication.impl.CipheredAuthenticationCookieFactory;
import org.craftercms.security.exception.InvalidCookieException;

/**
 * Manages encrypted crafter cookies.
 * Helps with Crafter Profile Client initialization.
 */
public final class CrafterManager {
	
	public static ProfileClient profileClient = new ProfileRestClientImpl();
	private static CipheredAuthenticationCookieFactory authCookieFactory = 
			new CipheredAuthenticationCookieFactory();
	public static String appToken;
	private static Log LOG = LogFactoryUtil.getLog(CrafterManager.class);

    
	private CrafterManager() { } 
    
	public static void init() {		
		profileClient.setHost(CrafterConstants.CRAFTER_DOMAIN);
		profileClient.setPort(CrafterConstants.CRAFTER_PORT);
		profileClient.setScheme(Http.HTTP);
		profileClient.setProfileAppPath(CrafterConstants.PROFILE_APP);
		
    	File cookieKeyFile = new File(CrafterLoginPostAction.class
    			.getResource(CrafterConstants.COOKIE_KEY_PATH).getFile());		
    	authCookieFactory.setEncryptionKeyFile(cookieKeyFile);
    	authCookieFactory.init();
		
		try {
			appToken = profileClient.getAppToken(
					CrafterConstants.PROFILE_CLIENT_LOGIN, 
					CrafterConstants.PROFILE_CLIENT_PASSWORD);
			
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}	
	}
    
    
    /**
     * This is for add an encrypted cookie to be used on Crafter Social.
     * @return 
     * @throws Exception
     */
    public static AuthenticationCookie getCrafterProfileCookie(String username) 
    		throws Exception {
		
		String userTicket = null;

		// Given the user get the ticket.
		try {
			userTicket = profileClient.getTicket(
					appToken, username, "", CrafterConstants.TENANT_NAME, true);
			
			} catch (AppAuthenticationException aae) {
			LOG.error(aae.getMessage());
		}
				
		// Given the ticket generate the cookie.
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, CrafterConstants.PROFILE_TIME_TO_OUTDATED);
		
		return authCookieFactory.getCookie(userTicket, calendar.getTime());

	}
    
    /**
     * This is for delete the crafterAuthCookie.
     * @param request
     * @param response
     */
	public static void deleteCrafterProfileCookie(HttpServletRequest request, 
			HttpServletResponse response) {
		
		// Create a context.
		RequestContext context = new RequestContext();
		context.setRequest(request);
		context.setResponse(response);

		try {
			// Get the cookie.
			AuthenticationCookie authenticationCookie = 
					authCookieFactory.getCookie(context);

			// Delete the cookie.
			if (authenticationCookie != null)
				authenticationCookie.delete(context);
		} catch (InvalidCookieException ice) {
			LOG.error(ice.getMessage());
		}
	}

}