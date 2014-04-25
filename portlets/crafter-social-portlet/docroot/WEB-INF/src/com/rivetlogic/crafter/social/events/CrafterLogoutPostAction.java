package com.rivetlogic.crafter.social.events;

import com.liferay.portal.kernel.events.Action;
import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import java.io.File;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.craftercms.security.api.RequestContext;
import org.craftercms.security.authentication.impl.AuthenticationCookie;
import org.craftercms.security.authentication.impl.CipheredAuthenticationCookieFactory;
import org.craftercms.security.exception.InvalidCookieException;

public class CrafterLogoutPostAction extends Action {
	
	private Log LOG = LogFactoryUtil.getLog(CrafterLoginPostAction.class);
	private static CipheredAuthenticationCookieFactory authCookieFactory = 
			new CipheredAuthenticationCookieFactory();

	@Override
	public void run(HttpServletRequest request, HttpServletResponse response)
			throws ActionException {
    	File cookieKeyFile = new File(CrafterLoginPostAction.class
    			.getResource("/auth-cookie-key").getFile());
			
    	authCookieFactory.setEncryptionKeyFile(cookieKeyFile);
    	authCookieFactory.init();
    	
    	deleteCrafterProfileCookie(request, response);
	}
	
	public void deleteCrafterProfileCookie(HttpServletRequest request, 
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
