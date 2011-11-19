/*
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package multiThread;

import debug.logger;
import java.lang.reflect.Method;
import multiThread.ResultCollector;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.ArrayList;

/**
 *
 * @author user
 */
public class MTLauncher {
	public static ArrayList Launch(LinkedList Objects, ArrayList Methods, ArrayList Params) {

		ResultCollector results = new ResultCollector();
		ArrayList Threads = new ArrayList();
		ThreadStarter TS;

		try {
			for (int i=0; i<Objects.size(); i++) {
				TS = new ThreadStarter(Objects.get(i),(String)Methods.get(i), (LinkedHashMap)Params.get(i), results, i);
				TS.start();
				Threads.add(TS);
			}

			results.waitForResults();

		} catch (Exception ex) {
			logger.Add(ex.getMessage());
			
		} finally {
			return results.GetResults();
		}
	}
}

class ThreadStarter extends Thread {
	private Object obj;
	private String method;
	private LinkedHashMap params;
	private ResultCollector results;
	private int number;

	ThreadStarter(Object obj, String method, LinkedHashMap params, ResultCollector results, int number) {
		this.obj = obj;
		this.params = params;
		this.method = method;
		this.results = results;
		this.number = number;
	}

	@Override
	public void run() {
		Method init;
		Class cls;
		Object result = new Object();

		results.Take();

		try {
				cls = this.obj.getClass();
				init = cls.getMethod(method, params.getClass());
				result = init.invoke(this.obj, params);

		} catch (Exception ex) {
			logger.Add("no class");
		} finally {
			results.AddResult(result, number);
			results.Release();
		}
	}
}
