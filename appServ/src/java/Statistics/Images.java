/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Statistics;
import mediaTools.bfImage;
import mysql.*;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.ArrayList;
import multiThread.MTLauncher;
/**
 *
 * @author user
 */
public class Images {
	
	public static String getStatistic() {
		DBShell dbs = new DBShell();
		DB db = dbs.getDB();

		ArrayList images = db.ReadCol("select imageId from hotels_list where imageId >0 order by rand() limit 5000");
		
		LinkedList Objects = new LinkedList();
		ArrayList methods = new ArrayList();
		ArrayList params = new ArrayList();

		LinkedHashMap param;
		bfImage obj;
		String img;

		for(int i=0; i<images.size(); i++) {
			obj = new bfImage();
			img = (String)images.get(i);
			param = new LinkedHashMap();
			param.put("url", "http://media.hotelscombined.com/HI"+img+".jpg");

			Objects.add(obj);
			methods.add("getInfo");
			params.add(param);
		}

		ArrayList Results = MTLauncher.Launch(Objects, methods, params);

		dbs.releaseDB(db);

		String Result = "";
		String tmp;
		for (int i=0; i<Results.size(); i++) {
			tmp = (String)Results.get(i);

			if (tmp.charAt(0) == '0') continue;
			Result += (tmp) + "\n";
		}

		return Result;
	}

	
}
