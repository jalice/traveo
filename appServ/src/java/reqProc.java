/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
///srv/jetty6/webapps/appServ/WEB-INF
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import org.json.simple.JSONValue;
import org.xml.sax.SAXException;
import remoteApi.*;
import mysql.DBShell;
import java.util.LinkedHashMap;
import debug.logger;
import multiThread.ResultCollector;
import city.Search;
import city.CityBase;
import java.util.Date;
import tools.advanceInfo;
import Statistics.*;
import java.util.ArrayList;
import javax.servlet.http.Cookie;
import mysql.DB;
import resource.CookieWorker;
import com.maxmind.geoip.*;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Random;
import multiThread.MTLauncher;
import resource.User;
import resource.Vars;
import resource.sysHelp;
import java.util.HashMap;

/**
 *
 * @author user
 */
public class reqProc extends HttpServlet {

	protected LookupService FindCountry;
	protected LookupService FindCity;

	@Override
	public void init() {
		LinkedHashMap dbConfig = new LinkedHashMap();
		dbConfig.put("init_str", "jdbc:mysql://localhost:3306/hotels");
		dbConfig.put("login","root");
		dbConfig.put("password","");

		DBShell dbs = new DBShell();
		dbs.Init(dbConfig);

		Search.init();

		try {
			FindCountry = new LookupService("/usr/local/share/GeoIP/GeoIP.dat", LookupService.GEOIP_MEMORY_CACHE);
			FindCity = new LookupService("/usr/local/share/GeoIP/GeoIPCity.dat", LookupService.GEOIP_MEMORY_CACHE);
		}catch(Exception Ex) {
			logger.Add("can`t load city  base");
			logger.Add(Ex.getMessage());
		}
	}

	/**
	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */

	protected Object ReadSearch(HttpServletRequest request) {
		Cookie ck = CookieWorker.getCookie("traveo_user", request.getCookies());
		DBShell dbs = new DBShell();
		DB db = dbs.getDB();

		if (ck==null) return new ArrayList();

		String user_id = ck.getValue();
		ArrayList res = db.ReadAss("select * from related_search where user_id="+ Integer.parseInt(user_id));
		dbs.releaseDB(db);

		return res;
	}

	protected Object WriteSearch(HttpServletRequest request, HttpServletResponse response) {
		String city = (String)request.getParameter("city");
		String in = (String)request.getParameter("in");
		String out = (String)request.getParameter("out");
		String people = (String)request.getParameter("people");
		String rooms = (String)request.getParameter("rooms");
		String stars = (String)request.getParameter("stars");
		String price = (String)request.getParameter("price");

		DBShell dbs = new DBShell();
		DB db = dbs.getDB();

		LinkedHashMap row;

		String user_id = User.getId();
		row = new LinkedHashMap();
		row.put("user_id", user_id);
		row.put("dest", city);
		row.put("in", in);
		row.put("out", out);
		row.put("people", people);
		row.put("room", rooms);
		row.put("stars", stars);
		row.put("price", price);

		db.InsertDB(row, "related_search");

		dbs.releaseDB(db);

		return "ok";
	}

	protected Object HotelsInfo(HttpServletRequest request) {
		
		ResultCollector Result = new ResultCollector();
		String city = (String)request.getParameter("city");
		String in = (String)request.getParameter("in");
		String out = (String)request.getParameter("out");
		String people = (String)request.getParameter("people");
		String rooms = (String)request.getParameter("rooms");
		
		String IP = request.getRemoteAddr();

		HotelCombined HC = new HotelCombined(city, in, out, people, rooms, IP, Result);
		try {

			// --------START partners
			HC.start();

			//-----------------------

			Thread.sleep(1); //small wait
			Result.waitForResults(); //collect results;
			return Result.GetResults();
		} catch (Exception ex) {
			logger.Add("error");
		} finally {
			
		}

		return null;
	}

	protected Object RPC(String Function, HttpServletRequest request, HttpServletResponse response) {
		if (Function.equals("hotelsInfo")) {
			return this.HotelsInfo(request);
		}

		if (Function.equals("writeSearch")) {
			return this.WriteSearch(request, response);
		}

		if (Function.equals("readSearch")) {
			return this.ReadSearch(request);
		}

		if (Function.equals("citySearch")) {
			return Search.SearchCity((String)request.getParameter("city"));
		}

		if (Function.equals("cityCheck")) {
			return Search.CheckCity((String)request.getParameter("city"));
		}

		if (Function.equals("hotelinfo")) {
			advanceInfo.parse("/home/victus/www/xml");

			return logger.Log;
		}

		if (Function.equals("showLog")) {

			return logger.Log;
		}

		if (Function.equals("oneHotel")) {

			return HotelCombined.oneHotel((String)request.getParameter("id"));
		}

		if (Function.equals("cityLoads")) {
			try {
				CityBase tmp = new CityBase();
				tmp.loadFromXML("/home/victus/www/xml/1.xml");
			} catch (Exception ex) {
				logger.Add(ex);
			} finally {
				return logger.Log;
			}
			//return tmp.loadFromXML();
		}


		if (Function.equals("perfomance")) {
			return logger.timePoints;
		}

		if (Function.equals("perfomance_clear")) {
			logger.timePoints.clear();

			return false;
		}

		if (Function.equals("imstat")) {
			return Images.getStatistic();
		}

		if (Function.equals("log")) {
			return logger.Log;
		}

		if (Function.equals("cache")) {
			return Search.cache;
		}

		if (Function.equals("cache_size")) {
			return Search.cache.size();
		}

		if (Function.equals("getLocation")) {
			try {
				Location tl = this.FindCity.getLocation(request.getRemoteAddr());

				return "" + tl.latitude + ";" + tl.longitude +";"+ tl.countryName + ";" + tl.city;

			} catch(Exception Ex) {
				logger.Add("error" + Ex.getMessage());
			}
		}

		return "error, func";
	}

	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		Vars.lSet("request", request);
		Vars.lSet("response", response);

		try {
			long startTime = logger.getStartTime();
			String callback = (String)request.getParameter("callback");
			String func = (String)request.getParameter("func");

			Object Out = "";
			Object res = this.RPC(func, request, response);

			if (callback == null) {
				Out = res;
			}
			else {
				response.setContentType("application/json; charset=utf-8");
				Out = callback+ "(" + JSONValue.toJSONString(res) + ");";
			}

			out.println(Out);
			logger.putTime(startTime, func);
		} finally {
			out.close();
			Vars.lClear();
		}
	}

		// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
	/**
	 * Handles the HTTP <code>GET</code> method.
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Returns a short description of the servlet.
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return "Short description";
	}// </editor-fold>
}

