/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package multiThread;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
public class ResultCollector {
	protected ArrayList Results = new ArrayList();
	protected ConcurrentHashMap ResultsMap = new ConcurrentHashMap();
	protected int inCounter = 0;

	public synchronized void AddResult(Object obj) {
		Results.add(obj);
	}

	public synchronized void AddResult(Object obj, int n) {
		ResultsMap.put(n, obj);
	}

	public ArrayList GetResults() {
		if (ResultsMap.size()>0) {
			for (int i=0; i<ResultsMap.size(); i++) {
				if (ResultsMap.containsKey(i)) {
					Results.add(ResultsMap.get(i));
				}
			}
		}
		return Results;
	}

	public  synchronized void waitForResults() {
		try {
			this.wait();
		} catch (InterruptedException ex) {
			Logger.getLogger(ResultCollector.class.getName()).log(Level.SEVERE, null, ex);
		}
			
	}

	public synchronized void Take() {
		inCounter++;
	}

	public synchronized void Release() {
		inCounter--;

			if (inCounter<=0) this.notifyAll();
		
	}
}
