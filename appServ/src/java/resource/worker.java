/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package resource;

import java.lang.reflect.Method;
import java.util.*;
import debug.logger;
import mysql.*;
/**
 * @author user
 */
public class worker {
	protected LinkedList queue;
	protected String className;
	public LinkedHashMap args;

	public void createWorkers(String className, LinkedHashMap args) {
		this.createWorkers(className, args, 1);
	}

	public void createWorkers(String className, LinkedHashMap args, int count) {
		queue = new LinkedList();

		this.className = className;
		this.args = (LinkedHashMap)args.clone();

		Method init;
		Class cls;
		Object obj;

		for(int i=0; i<count; i++) {
			try {
					obj = Class.forName(className).newInstance();
					cls = obj.getClass();
					init = cls.getMethod("init", this.args.getClass());
					init.invoke(obj, this.args);
					this.queue.add(obj);
			} catch (Exception ex) {
				logger.Add("no class");
			}
		}
	}

	public synchronized Object GetWorker() {
			Method init;
			Class cls;
			Object obj;
			if (!queue.isEmpty())
				return queue.removeLast();
			try {
				obj = Class.forName(className).newInstance();
				cls = obj.getClass();
				init = cls.getMethod("init", this.args.getClass());
				init.invoke(obj, this.args);
				return obj;
			} catch (Exception ex) {
				logger.Add(ex.getMessage());
				logger.Add("error worker");
			}

			return null;
	}

	public  synchronized void ReturnWorker(Object obj) {

		this.queue.addFirst(obj);
	}
}
