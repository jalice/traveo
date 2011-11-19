/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mysql;
import java.util.LinkedHashMap;
import mysql.DB;
import resource.worker;
import debug.logger;
/**
 *
 * @author user
 */
public class DBShell {
	static worker workers;

	public void Init(LinkedHashMap DB_config) {
		workers = new worker();
		workers.createWorkers("mysql.DB", DB_config, 5);
	}

	public DB getDB() {
		return (DB)(workers.GetWorker());
	}

	public void releaseDB(DB obj) {
		workers.ReturnWorker(obj);
	}
}
