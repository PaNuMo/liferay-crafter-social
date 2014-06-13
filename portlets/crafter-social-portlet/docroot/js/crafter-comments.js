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

if(Liferay.ThemeDisplay.isSignedIn() && !crafterSocial_cfg){

	/*
	 * Social configuration attributes can be overwritten by
	 * declaring this object before the social javascript is
	 * imported into the page
	 */
	var crafterSocial_cfg = (function () {
	    var cfg = {

	        'url.base'                      : '/crafter-social-portlet/',
	        'url.service'                   : location.protocol + '//' + location.hostname + ':9666' + '/crafter-social/api/2/',
	        'url.templates'                 : '/crafter-social-portlet/templates/',	        
	    };
	    return cfg;
	})();
	
	
	AUI().ready('get','node', function(A) {
	
		A.Get.load(
			[
				'/crafter-social-portlet/js/social.js', 
				'/crafter-social-portlet/styles/main.css', 
				'/crafter-social-portlet/styles/editor-content.css'
			], 
			{ 
				onSuccess : function () { defineCrafterCommentableNodes(); }
			}, 
			function (err) {
			    if (err) {
		        A.Array.each(err, function (error) {
		            A.log('Error loading file: ' + error.error, 'error');
		        });
		        return;
			}
		}); 	
		
		
		function defineCrafterCommentableNodes(){
			var commentableNodes = A.all('div[id^="commentable"]');
					
			commentableNodes.each(function(item){
				var currentId = '#' + item.getAttribute('id');
				
				crafter.social.getDirector().socialise({ 
					target: currentId,
		            tenant: 'craftercms'
				});			
			});
				
			crafter.social.getDirector().setProfile({
		        displayName: 'You',
		        roles: [
		            'SOCIAL_ADVISORY',
		            'SOCIAL_ADMIN',
		            'SOCIAL_MODERATOR',
		            'SOCIAL_AUTHOR'
		        ]
		     });    
		}
		
	});
}