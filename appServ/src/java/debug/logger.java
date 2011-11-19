/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package debug;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Date;
/**
 *
 * @author user
 */
public class logger {
	public static ArrayList Log = new ArrayList();
	public static ConcurrentHashMap timePoints = new ConcurrentHashMap();

	public static synchronized void Add(Object obj) {
		logger.Log.add(obj);
	}

	public static long getStartTime() {
		Date nd = new Date();
		
		return nd.getTime();
	}

	public static synchronized void putTime(long time, String point) {
		ConcurrentHashMap tp;
		Date nd = new Date();

		if (timePoints.containsKey(point)) {
			tp = (ConcurrentHashMap)timePoints.get(point);

		} else {
			tp = new ConcurrentHashMap();
			tp.put("count", new Long(0));
			tp.put("time", new Long(0));

			timePoints.put(point, tp);
		}

		Long count = (Long) tp.get("count");
		Long allTime = (Long) tp.get("time");

		count++;
		allTime += new Long(nd.getTime() - time);

		tp.put("count", count);
		tp.put("time", allTime);
		tp.put("ave", allTime/count);

		timePoints.put(point, tp);
	}
}
