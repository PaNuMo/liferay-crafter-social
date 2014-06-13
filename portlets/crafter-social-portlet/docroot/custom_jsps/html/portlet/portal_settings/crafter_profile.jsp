<%--
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
 */
--%>

<%@ include file="/html/portlet/portal_settings/init.jsp" %>

<% 
final String CRAFTER_PROFILE_ADMIN_KEY = "crafter_profile_admin.username";
final String CRAFTER_PROFILE_ADMIN_PAS = "crafter_profile_admin.password";

String crafterProfileUsername = PrefsPropsUtil.getString(
		company.getCompanyId(), CRAFTER_PROFILE_ADMIN_KEY, 
		GetterUtil.getString(PropsUtil.get(CRAFTER_PROFILE_ADMIN_KEY), "superadmin"));

String crafterProfilePassword = PrefsPropsUtil.getString(
	company.getCompanyId(), CRAFTER_PROFILE_ADMIN_PAS, 
	GetterUtil.getString(PropsUtil.get(CRAFTER_PROFILE_ADMIN_PAS), "superadmin"));
%>

<liferay-ui:error key="username-already-exists-on-crafter-profile" message="username-already-exists-on-crafter-profile" />

<h3><liferay-ui:message key="crafter-profile" /></h3>

<aui:fieldset>
	<aui:input autocomplete="off" cssClass="lfr-input-text-container" 
		label="crafter-profile-admin-username" name='<%="settings--"+CRAFTER_PROFILE_ADMIN_KEY+"--"%>' 
		type="text" value="<%= crafterProfileUsername %>" />	
		
	<aui:input autocomplete="off" cssClass="lfr-input-text-container" 
		label="crafter-profile-admin-password" name='<%="settings--"+CRAFTER_PROFILE_ADMIN_PAS+"--"%>' 
		type="password" value="<%= crafterProfilePassword %>" />	
</aui:fieldset>


