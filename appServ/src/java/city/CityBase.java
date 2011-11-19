/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package city;

import debug.logger;
import java.io.File;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import xml.Parse;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import mysql.DB;
import mysql.DBShell;
/**
 *
 * @author user
 */
public class CityBase {

	public Object loadFromXML(String S) {
		try {
			File F = new File(S);
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			Parse saxp = new Parse();

			parser.parse(F, saxp);
			
			ArrayList locationList = (ArrayList)(((LinkedHashMap)(((ArrayList)(saxp.root.get("attr"))).get(0))).get("attr"));

			LinkedHashMap cityAttr;
			LinkedHashMap location;
			LinkedHashMap toDB = new LinkedHashMap();
			String cName;
			LinkedHashMap someData;

			DBShell dbs = new DBShell();
			DB db = dbs.getDB();

			
			for(int i=0; i<locationList.size(); i++) {
				location = (LinkedHashMap)(locationList.get(i));
				cityAttr = saxp.attrToHashMap((ArrayList)location.get("attr"));

				toDB.clear();
				cName = (String)cityAttr.get("CityFileName");
				cName = cName.replace('_', ' ');
				cName = DB.quote(cName);

				someData = db.ReadRow("select * from cities where title = '"+cName+"' limit 1");
				
				if (someData.containsKey("city_id")) {
					toDB.put("city_id", someData.get("city_id"));
					toDB.put("country_id", someData.get("country_id"));
					toDB.put("title", cityAttr.get("CityName"));
					toDB.put("search", ((String)cityAttr.get("CityName")) + ((String)cityAttr.get("CountryName")));
					db.InsertDB(toDB, "cities_ru");
					
					toDB.clear();

					toDB.put("id", someData.get("country_id"));
					toDB.put("title", cityAttr.get("CountryName"));
					db.InsertDB(toDB, "countries_ru");
				}
				
			}
			
			dbs.releaseDB(db);
		} catch (Exception ex) {
			logger.Add(ex.getMessage());
		}

		return null;
	}
}
