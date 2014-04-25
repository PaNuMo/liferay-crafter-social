package com.rivetlogic.crafter.social.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Manages single sign-on cookies.
 */
public class SsoCookieManager {

    private String cookieName;
    private String cookieDomain;
    private String cookiePath;
    private int cookieMaxAge;

    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }

    public void setCookieDomain(String cookieDomain) {
        this.cookieDomain = cookieDomain;
    }

    public void setCookiePath(String cookiePath) {
        this.cookiePath = cookiePath;
    }

    public void setCookieMaxAge(int cookieMaxAge) {
        this.cookieMaxAge = cookieMaxAge;
    }

    public String getCookie(HttpServletRequest request) {
        Cookie cookie = null;
        
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
        	for (Cookie coo : cookies) {
        		if (coo.getName().equals(cookieName)) {
        			cookie = coo;
                }
            }
        }
        
        if (cookie != null) {
            return cookie.getValue();
        } else {
            return null;
        }
    }

    public void addCookie(HttpServletResponse response, String value) {
        Cookie cookie = new Cookie(cookieName, value);
        cookie.setDomain(cookieDomain);
        cookie.setPath(cookiePath);
        cookie.setMaxAge(cookieMaxAge);

        response.addCookie(cookie);
    }

    public void deleteCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setDomain(cookieDomain);
        cookie.setPath(cookiePath);
        cookie.setMaxAge(0);

        response.addCookie(cookie);
    }

}