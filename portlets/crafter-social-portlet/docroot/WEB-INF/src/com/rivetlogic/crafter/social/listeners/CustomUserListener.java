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
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.model.BaseModelListener;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.rivetlogic.crafter.social.utils.CrafterConstants;
import com.rivetlogic.crafter.social.utils.CrafterManager;

import org.craftercms.profile.exceptions.AppAuthenticationException;
import org.craftercms.profile.impl.domain.Profile;

public class CustomUserListener extends BaseModelListener<User>{

	private Log LOG = LogFactoryUtil.getLog(CustomUserListener.class);
	
	@Override
	public void onAfterUpdate(User user) throws ModelListenerException {	
		try {
			if(UserLocalServiceUtil.getDefaultUserId(user.getCompanyId()) != user.getUserId()){
				CrafterManager.init();
				Profile profile = getCrafterProfile(user.getScreenName());
				if(profile != null){
					crafterProfileActivateDeactivate(user, profile);
				}
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
		super.onAfterUpdate(user);
	}
	
	private Profile getCrafterProfile(String username) {		
		Profile result = null;
		try {
			result = CrafterManager.profileClient.getProfileByUsernameWithAllAttributes(
					CrafterManager.appToken, username, CrafterConstants.TENANT_NAME);
		} catch (AppAuthenticationException aae) {
			LOG.error(aae.getMessage());
		}
		return result;
	}
	
	private void crafterProfileActivateDeactivate(User user, Profile profile){
		if(profile.getActive() && !user.isActive()){
			CrafterManager.profileClient.activeProfile(
					CrafterManager.appToken, profile.getId(), Boolean.FALSE);
		}
		else if(!profile.getActive() && user.isActive()){
			CrafterManager.profileClient.activeProfile(
					CrafterManager.appToken, profile.getId(), Boolean.TRUE);
		}
	}

}