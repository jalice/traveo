/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package resource;

import java.util.LinkedHashMap;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mysql.DB;
import mysql.DBShell;


/**
 *
 * @author user
 */
public class User {
	public static String getId() {
		HttpServletRequest request = sysHelp.getRequest();
		HttpServletResponse response = sysHelp.getResponse();
		Cookie ck = CookieWorker.getCookie("traveo_user", request.getCookies());
		DBShell dbs = new DBShell();
		DB db = dbs.getDB();

		LinkedHashMap row;
		String user_id;
		if (ck==null) {
			row = new LinkedHashMap();
			row.put("id", "0");
			db.InsertDB(row, "user");
			user_id = (String) db.queryInfo.get("inserted_id");

			ck = new Cookie("traveo_user", user_id);
		}

		ck.setMaxAge(3600*24*30);
		response.addCookie(ck);

		dbs.releaseDB(db);

		return ck.getValue();
	}
}
