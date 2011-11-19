/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import xml.Parse;
import org.xml.sax.helpers.DefaultHandler;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import mysql.*;
import debug.logger;
/**
 *
 * @author user
 */
public class advanceInfo {
	static ArrayList threads = new ArrayList();

	static String GetTableName(String field) {
		if (field.equalsIgnoreCase("HotelName")) return "hotelName";
		if (field.equalsIgnoreCase("StarRating")) return "StarRating";
		if (field.equalsIgnoreCase("Popularity")) return "Popularity";
		if (field.equalsIgnoreCase("OverallRating")) return "OverallRating";
		if (field.equalsIgnoreCase("Checkin")) return "CheckIn";
		if (field.equalsIgnoreCase("Checkout")) return "CheckOut";
		if (field.equalsIgnoreCase("HotelID")) return "hotelid";

		return null;
	}

	static ArrayList getImages(ArrayList ImageList) {

		LinkedHashMap buf;
		ArrayList result = new ArrayList();

		for(int i=0; i<ImageList.size(); i++) {
			buf = (LinkedHashMap)(ImageList.get(i));
			result.add(buf.get("value"));
		}

		return result;
	}

	static LinkedHashMap dataToDB(ArrayList options) {
		LinkedHashMap buf;
		LinkedHashMap result = new LinkedHashMap();
		ArrayList images = new ArrayList();
		LinkedHashMap hotel = new LinkedHashMap();
		String Name;

		for (int i=0; i<options.size(); i++) {
			buf = (LinkedHashMap)options.get(i);

			Name = (String)(buf.get("name"));
			if (Name.equals("Descriptions")) {
				ArrayList Descs = (ArrayList)(buf.get("attr"));
				if (Descs.size()>0) {
					buf = (LinkedHashMap)(((ArrayList)(((LinkedHashMap)(Descs.get(0))).get("attr"))).get(1));
					hotel.put("desc", buf.get("value"));
				}
				
			} else {
				if (Name.equals("Images")) {
					
					images = advanceInfo.getImages((ArrayList)(buf.get("attr")));
				} else {
					Name = advanceInfo.GetTableName(Name);
					if (Name != null) {
						hotel.put(Name, buf.get("value"));
					}
				}

			}
		}

		result.put("images", images);
		result.put("hotel", hotel);

		return result;
	}

	static public void parse(String dir) {
		try {
			File folder = new File(dir);
			
			thParse tp;
			advanceInfo.threads.clear();

			for (File subFolder : folder.listFiles()) {
				
					tp = new thParse(subFolder);
					tp.start();
					advanceInfo.threads.add(tp);
					
				
			}
		} catch (Exception ex) {
			logger.Add("error");
			logger.Add(ex.getMessage());

		}
	}
}


class thParse extends Thread {

	File file;

	public thParse(File f) {
		this.file = f;
	}

	@Override
	public void run() {
		for(File f : this.file.listFiles()) {
			try {
				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser parser = factory.newSAXParser();
				Parse saxp = new Parse();


				ArrayList options;
				ArrayList images;
				LinkedHashMap toDB;
				LinkedHashMap row;
				DBShell dbs = new DBShell();
				DB db = dbs.getDB();
				String id;

						parser.parse(f, saxp); //take dom
						options = (ArrayList)(((LinkedHashMap)(((ArrayList)saxp.root.get("attr")).get(0))).get("attr"));

						toDB = advanceInfo.dataToDB(options);
						id = (String)(((LinkedHashMap)(toDB.get("hotel"))).get("hotelid"));
						db.EditDB((LinkedHashMap)(toDB.get("hotel")), "hotels_list", "hotelid="+id);
						images = (ArrayList)(toDB.get("images"));

						for(int i=0; i<images.size(); i++) {
							row = new LinkedHashMap();
							row.put("id", images.get(i));
							row.put("hotel_id", id);
							db.InsertDB(row, "hc_images");
						}

						dbs.releaseDB(db);
						logger.Add(f.delete());
						this.sleep(1000);
			} catch(Exception ex) {
				logger.Add("error");
				logger.Add(ex.getMessage());
			}
		}
	}

}