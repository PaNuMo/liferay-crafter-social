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

import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringPool;

public final class CrafterConstants {
	
	public static final String TENANT_NAME = "craftercms";
	public static final String PROFILE_CLIENT_LOGIN = "crafterengine";
	public static final String PROFILE_CLIENT_PASSWORD = "crafterengine";
	public static final String PROFILE_APP = "crafter-profile";
	public static final String PROFILE_DEFAULT_USER = "superadmin";
	public static final String PROFILE_ADMIN_KEY = "crafter_profile_admin.username";
	public static final String SOCIAL_APP = "crafter-social";
	public static final String CRAFTER_DOMAIN = "127.0.0.1";
	public static final String COOKIE_KEY_PATH = "/auth-cookie-key";
	
	public static final int COOKIE_MAX_AGE = -1;
	public static final int CRAFTER_PORT = 9666;
	public static final int PROFILE_TIME_TO_OUTDATED = 
			Integer.valueOf(PropsUtil.get("session.timeout")) * 60;
	
	public static final String CRAFTER_HOST_URL = Http.HTTP + StringPool.COLON 
			+ StringPool.SLASH + StringPool.SLASH + CRAFTER_DOMAIN 
			+ StringPool.COLON + CRAFTER_PORT + StringPool.SLASH;
	
	public static final String SOCIAL_API_DELETE_URL = 
			CRAFTER_HOST_URL + SOCIAL_APP + "/api/2/ugc/delete.json";
	
	public static final String SOCIAL_API_SEARCH_UGC_URL = 
			CRAFTER_HOST_URL + SOCIAL_APP + "/api/2/ugc/target/regex.json";
	
	
    public static final String ATTRIBUTE_ANONYMOUS_POSTER = "anonymousPoster";
    public static final String ATTRIBUTE_DISPLAY_NAME = "displayName";
	public static final String ATTRIBUTE_FIRST_NAME = "first-name";
	public static final String ATTRIBUTE_LAST_NAME = "last-name";
	public static final String ATTRIBUTE_FULL_NAME = "fullName";

}
