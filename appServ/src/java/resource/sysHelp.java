/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package resource;

import debug.logger;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author user
 */
public class sysHelp {
	public static HttpServletRequest getRequest() {
		return (HttpServletRequest)Vars.lGet("request");
		
	}

	public static HttpServletResponse getResponse() {
		return (HttpServletResponse)Vars.lGet("response");
	}

	public static void Include(String page) {
		try {
			Vars.lSet("asd", "zxc");
			logger.Add(sysHelp.getRequest().getRequestDispatcher(page));
			RequestDispatcher rd = sysHelp.getRequest().getRequestDispatcher(page);
			rd.include(sysHelp.getRequest(), sysHelp.getResponse());

		} catch (Exception ex) {
			logger.Add("page-find Error");
			logger.Add(ex.getMessage());
		}
	}
	 
}
