/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package remoteApi;
import java.io.StringReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import network.GetWebContent;
import xml.Parse;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.*;
import mysql.*;
import debug.logger;
import multiThread.ResultCollector;
import city.Search;

import javax.servlet.http.HttpServletRequest;
import resource.User;
import resource.sysHelp;
/**
 *
 * @author user
 */
public class HotelCombined extends Thread {
	protected ResultCollector Results;

	protected String City;
	protected String In;
	protected String Out;
	protected String People;
	protected String Rooms;
	protected String IP;

	public HotelCombined(String city, String in, String out, String people, String rooms, String ip, ResultCollector Results) {
		this.Results = Results;
		this.City = city;
		this.In = in;
		this.Out = out;
		this.People = people;
		this.Rooms = rooms;
		this.IP = ip;
	}

	public void getData(ResultCollector Results) throws ParserConfigurationException, SAXException, IOException {
		DBShell dbs = new DBShell();
		ArrayList Res = new ArrayList();
		ArrayList Result = new ArrayList();

		LinkedHashMap Rows = new LinkedHashMap();
		LinkedHashMap Row;
		GetWebContent GWC = new GetWebContent();

		LinkedHashMap City = Search.CheckCity(this.City);
		Boolean error = (Boolean)(City.get("error"));

		if (error) {
			Results.Release();
			return;
		}

		String city_id =  (String)(((LinkedHashMap)(City.get("city"))).get("hc_id"));
		DB db = dbs.getDB();

		if ( this.In.equals("") || this.Out.equals("") ) {
			String sql = "select hotelId, hotelName, rating, address, Latitude, Longitude, LEFT(`desc`, 200) as description, CONCAT('http://media.hotelscombined.com/HI', imageId, '.jpg') as image from hotels_list where cityId = " + Integer.parseInt(city_id);

			Results.AddResult(db.ReadAss(sql));
			Results.Release();
			return;
		}

		LinkedHashMap Data = new LinkedHashMap();
		Data.put("CityID", DB.quote(city_id));
		Data.put("Checkin", DB.quote(this.In));
		Data.put("Checkout", DB.quote(this.Out));
		Data.put("Rooms", DB.quote(this.Rooms));
		Data.put("Guests", DB.quote(this.People));
		Data.put("UserID:", DB.quote(this.IP));
		Data.put("UserIPAddress", DB.quote(this.IP));
		Data.put("UserAgent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 1.1.4322)");
		Data.put("ApiKey", "F7538EB2-2B63-4AE1-8C43-2FAD2D83EACB");
		
		String xml = GWC.getURL("http://sandbox.hotelscombined.com/API/Search.svc/pox/CitySearch", Data);
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			Parse saxp = new Parse();
			parser.parse(new InputSource(new StringReader(xml)), saxp); //take dom

			LinkedHashMap resp = (LinkedHashMap)((ArrayList)(saxp.root.get("attr"))).get(0);
			ArrayList Hotels = (ArrayList)(((LinkedHashMap)((ArrayList)(resp.get("attr"))).get(1)).get("attr"));
			
			ArrayList r;
			String sql;

			//take hotels ids
			String ids = "0";
			String id;

			ArrayList Hotel;
			ArrayList Rates;
			LinkedHashMap RateRow;
			LinkedHashMap RoomAttr;
			ArrayList RateAttr;
			ArrayList RoomRates;

			for (int i=0; i<Hotels.size(); i++) {

				Hotel =  (ArrayList)(((LinkedHashMap)(Hotels.get(i))).get("attr"));
				Rates = (ArrayList)(((LinkedHashMap)(Hotel.get(2))).get("attr"));
				id = (String)(((LinkedHashMap)(Hotel.get(1))).get("value"));
				ids = ids + ", " + id;

				RoomRates = new ArrayList();
				RateRow = new LinkedHashMap();
				for(int j=0; j<Rates.size(); j++) {
					RateAttr = (ArrayList)(((LinkedHashMap)(Rates.get(j))).get("attr"));
					
					RateRow = new LinkedHashMap();
					for(int z=0; z<RateAttr.size(); z++){
						RoomAttr = (LinkedHashMap)RateAttr.get(z);
						RateRow.put(RoomAttr.get("name"), RoomAttr.get("value"));
					}
					RoomRates.add(RateRow);
				}

				Rows.put(id, RoomRates);
			}
			sql = "select hotelId, hotelName, rating, address, Latitude, Longitude, LEFT(`desc`, 200) as description, CONCAT('http://media.hotelscombined.com/HI', imageId, '.jpg') as image from hotels_list where hotelId in("+ids+")";
			Res = db.ReadAss(sql);

			for(int i=0; i<Res.size(); i++) {
				Row = (LinkedHashMap)(Res.get(i));
				id = (String)(Row.get("hotelId"));
				Row.put("prices", Rows.get(id));
				
			}

			dbs.releaseDB(db);
		
		} catch (Exception e) {
			logger.Add("error happens");
			logger.Add(e.getMessage());
		}

        Results.AddResult(Res);
		Results.Release();
	}

	@Override
	public void run() {
		Results.Take();
		try {
			this.getData(Results);
		} catch (ParserConfigurationException ex) {
			Logger.getLogger(HotelCombined.class.getName()).log(Level.SEVERE, null, ex);
		} catch (SAXException ex) {
			Logger.getLogger(HotelCombined.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(HotelCombined.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	public static Object oneHotel(String id) {
		try {
			DBShell dbs = new DBShell();
			DB db = dbs.getDB();
			int iid = Integer.parseInt(id);

			HttpServletRequest req = sysHelp.getRequest();
			String Uid = User.getId();

			LinkedHashMap row = new LinkedHashMap();
			row.put("hotel_id", id);
			row.put("user_id", Uid);
			row.put("_date", ""+((new Date()).getTime()/1000));

			db.InsertDB(row, "hotel_view_log");

			LinkedHashMap Hotel = db.ReadRow("select * from hotels_list where hotelId=" + iid);
			ArrayList Images = db.ReadCol("select CONCAT('http://media.hotelscombined.com/HI', id, '.jpg') from hc_images where hotel_id=" + iid);

			dbs.releaseDB(db);

			LinkedHashMap Res= new LinkedHashMap();
			Res.put("hotel", Hotel);
			Res.put("images", Images);

			return Res;
		} catch (Exception ex) {
			logger.Add(ex.getMessage());
			return logger.Log;
		}
	}
}