/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package resource;

import debug.logger;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author user
 */
public class Vars {
	static protected ConcurrentHashMap Global = new ConcurrentHashMap();
	static protected ThreadLocal <LinkedHashMap> Local = new ThreadLocal <LinkedHashMap> (){
             @Override protected LinkedHashMap initialValue() {
                 return new LinkedHashMap();
			 }
	};

	//get global var
	static public Object gGet (Object point) {
		return Global.get(point);
	}

	//set global var
	static public void gSet (Object point, Object Value) {
		Global.put(point, Value);
	}

	//get local var
	static public Object lGet (Object point) {
		LinkedHashMap tmp = Local.get();
		
		return tmp.get(point);
	}

	//set local var
	static public void lSet (Object point, Object Value) {
		LinkedHashMap tmp = Local.get();
		tmp.put(point, Value);
	}

	static public void lClear () {
		LinkedHashMap tmp = Local.get();
		tmp.clear();
	}
	
}
