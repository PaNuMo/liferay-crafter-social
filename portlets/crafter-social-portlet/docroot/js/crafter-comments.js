if(Liferay.ThemeDisplay.isSignedIn() && !crafterSocial_cfg){

	/*
	 * Social configuration attributes can be overwritten by
	 * declaring this object before the social javascript is
	 * imported into the page
	 */
	var crafterSocial_cfg = (function () {
	    var cfg = {

	        'url.base'                      : '/crafter-social-portlet/',
	        'url.service'                   : '/crafter-social/api/2/',
	        'url.templates'                 : '/crafter-social-portlet/templates/'
	        
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
				onSuccess : function () { defineCommentableNodes(); }
			}, 
			function (err) {
			    if (err) {
		        A.Array.each(err, function (error) {
		            A.log('Error loading file: ' + error.error, 'error');
		        });
		        return;
			}
		}); 	
		
		
		function defineCommentableNodes(){
			var commentableNodes = A.all('div[class^="commentable"]');
					
			commentableNodes.each(function(item){
				var currentClass = '.' + item.getAttribute('class');
				
				crafter.social.getDirector().socialise({ 
					target: currentClass,
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