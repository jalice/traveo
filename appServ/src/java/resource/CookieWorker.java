/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package resource;

import javax.servlet.http.Cookie;

/**
 *
 * @author user
 */
public class CookieWorker {
	public static Cookie getCookie(String Name, Cookie cks[]) {
		if (cks == null) return null;

		for (Cookie ck: cks) {
			if (ck.getName().equals(Name)) {
				return ck;
			}
		}

		return null;
	}
}
