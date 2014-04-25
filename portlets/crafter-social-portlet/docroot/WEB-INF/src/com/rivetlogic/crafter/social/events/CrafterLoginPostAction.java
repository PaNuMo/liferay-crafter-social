package com.rivetlogic.crafter.social.events;

import com.liferay.portal.kernel.events.Action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import org.craftercms.profile.api.ProfileClient;
import org.craftercms.profile.constants.ProfileConstants;
import org.craftercms.profile.exceptions.AppAuthenticationException;
import org.craftercms.profile.impl.ProfileRestClientImpl;
import org.craftercms.profile.impl.domain.Profile;
import org.craftercms.security.api.RequestContext;
import org.craftercms.security.authentication.impl.AuthenticationCookie;
import org.craftercms.security.authentication.impl.CipheredAuthenticationCookieFactory;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CrafterLoginPostAction extends Action {
	
	
    public static final String ATTRIBUTE_ANONYMOUS_POSTER = "anonymousPoster";
    public static final String ATTRIBUTE_PROFESSIONAL_FOCUS = "professionalFocus";
    public static final String ATTRIBUTE_PROFESSIONAL_ROLE = "professionalRole";
    public static final String ATTRIBUTE_DISPLAY_NAME = "displayName";
	public static final String ATTRIBUTE_FIRST_NAME = "first-name";
	public static final String ATTRIBUTE_LAST_NAME = "last-name";
	public static final String ATTRIBUTE_FULL_NAME = "fullName";
	
	
	private Log LOG = LogFactoryUtil.getLog(CrafterLoginPostAction.class);
	private Random randomGenerator = new Random(Calendar.getInstance().getTimeInMillis());
	private String appToken;
	private String tenantName = "craftercms";
	private ProfileClient profileClient = new ProfileRestClientImpl();
	private String profileClientLogin = "crafterengine";
	private String profileClientPassword = "crafterengine";
	protected int profileTimeToOutdated = 300;
	protected int cookieMaxAge = -1;
	private static CipheredAuthenticationCookieFactory authCookieFactory = 
			new CipheredAuthenticationCookieFactory();
	
	
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
			processSuccessfulAuthentication(user); 
	        addCrafterProfileCookie(request, response, user);
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}	

	}
	
	
	/**
	 * Crafter profile authentication
	 * @param user
	 * @throws Exception
	 */
	public void processSuccessfulAuthentication(User user) 
			throws Exception {
		
		profileClient.setHost("localhost");
		profileClient.setPort(9666);
		profileClient.setScheme("http");
		profileClient.setProfileAppPath("crafter-profile");
		
		appToken = profileClient.getAppToken(profileClientLogin, profileClientPassword);
		
		// See if this user is in Crafter profile, if not then add
		Profile profile = getCrafterProfile(user.getScreenName());
		if (profile == null) {
			LOG.info("profile IS NULL");
			profile = createProfile(user.getScreenName(), user.getEmailAddress(),
					user.getFirstName(), user.getLastName(), user.getFullName());
			profileClient.activeProfile(appToken, profile.getId(), Boolean.TRUE);
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
			result = profileClient.getProfileByUsernameWithAllAttributes(
					appToken, username, tenantName);
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
			String lastName, String fullName) throws Exception {
		
		Profile result = null;
		
		// Create the profile with any preset attributes we need.
		LOG.info(String.format("Generating Crafter Profile for %s.", fullName));
		Map<String, Serializable> queryParams = generateInitialProfileQueryParams();
		queryParams.put(ATTRIBUTE_DISPLAY_NAME, firstName + " " + lastName);
		queryParams.put(ATTRIBUTE_FIRST_NAME, firstName);
		queryParams.put(ATTRIBUTE_LAST_NAME, lastName);
		queryParams.put(ATTRIBUTE_FULL_NAME, fullName);
		
		try {
			result = profileClient.createProfile(appToken, screenName, 
					Double.toString(randomGenerator.nextDouble()), true, 
					tenantName, email, queryParams);
		} catch (AppAuthenticationException aae) {
			LOG.error(aae.getMessage());
		}
		
		return result;
	}	

	
	/**
	 * This is to generate the default query params used in a crafter-profile being created.
	 * @return
	 */
	private Map<String, Serializable> generateInitialProfileQueryParams() {
		Map<String, Serializable> resultMap = new HashMap<String, Serializable>();

		// Set an initial ROLE the user will have in Social.
		ArrayList<String> rolesList = new ArrayList<String>();
		rolesList.add("SOCIAL_USER");
		rolesList.add("SOCIAL_AUTHOR");
		
		resultMap.put(ProfileConstants.ROLES, rolesList);
		resultMap.put(ATTRIBUTE_ANONYMOUS_POSTER, "false");

		return resultMap;
	}
	
	
    /**
     * This is for add an encrypted cookie to be used on Crafter Social
     * @param request
     * @param response
     * @param user
     * @throws Exception
     */
    public void addCrafterProfileCookie(HttpServletRequest request, 
    		HttpServletResponse response, User user) throws Exception {
    	
    	File cookieKeyFile = new File(CrafterLoginPostAction.class
    			.getResource("/auth-cookie-key").getFile());
    	
    	LOG.info("FILE exists: "+ cookieKeyFile.exists()); 
			
    	authCookieFactory.setEncryptionKeyFile(cookieKeyFile);
    	authCookieFactory.init();
		
		String userTicket = null;

		// Given the user get the ticket.
		try {
			userTicket = profileClient.getTicket(
					appToken, user.getScreenName(), "", tenantName, true);
			
			} catch (AppAuthenticationException aae) {
			LOG.error(aae.getMessage());
		}
		
		LOG.info("user ticket: "+ user.getScreenName() + " - "+userTicket);
				
		// Given the ticket generate the cookie.
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, profileTimeToOutdated);
		AuthenticationCookie authCookie = authCookieFactory
				.getCookie(userTicket, calendar.getTime());

		saveOrUpdateCrafterProfileCookie(request, response, authCookie);
	}
    
    
	public void saveOrUpdateCrafterProfileCookie(HttpServletRequest request, 
			HttpServletResponse response, AuthenticationCookie cookie) {
		// Create a context.
		RequestContext context = new RequestContext();
		context.setRequest(request);
		context.setResponse(response);

		// Set the cookie to the servlet response.
		cookie.save(context, cookieMaxAge);
	}

}