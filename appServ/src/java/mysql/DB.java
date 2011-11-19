/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mysql;

import java.sql.ResultSetMetaData;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import debug.logger;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author user
 */
public class DB {
	protected Connection connect = null;
	protected LinkedHashMap Config;
	protected Properties properties=new Properties();
	public LinkedHashMap queryInfo;

	public static String quote(String str) {
		String tmp;
		tmp = str.replace("\\", "\\\\");
		tmp = tmp.replace("\"", "\\\"");

		return tmp;
	}

	public void init(LinkedHashMap dbConfig) {
		this.Config = (LinkedHashMap)dbConfig.clone();
		try {
                String url = (String)(Config.get("init_str"));

				try {
					
                    Class.forName("com.mysql.jdbc.Driver").newInstance();
                } catch (InstantiationException e) {
                    // TODO Автоматически созданный блок catch
                    e.printStackTrace();
					logger.Add("DB_error2");
                } catch (IllegalAccessException e) {
                    // TODO Автоматически созданный блок catch
                    e.printStackTrace();
					logger.Add("DB_error1");
                } catch (ClassNotFoundException e) {
                    // TODO Автоматически созданный блок catch
                    logger.Add("DB_error");
					System.err.println("Can't find JDBC driver");
                    e.printStackTrace();
                }

				properties.setProperty("user",(String)(Config.get("login")));
				properties.setProperty("password",(String)(Config.get("password")));
				properties.setProperty("useUnicode","true");
				properties.setProperty("characterEncoding","UTF-8");

				connect = DriverManager.getConnection(url, properties);

            } catch (SQLException e) {
				logger.Add("DB connection error");
                System.err.println("DB connection error");
            } 
	}

	public ArrayList ReadCol(String query) {
		ArrayList Answer = new ArrayList();
		try {
			ResultSet Result = this.Query(query);

			while(Result.next()) {
				Answer.add((String)Result.getString(1));
			}
			Result.close();

		} catch (Exception ex) {
			logger.Add(ex.getMessage());
			logger.Add("ass error");
		}


		return Answer;
	}

	public ArrayList mtReadAss(LinkedHashMap params) {
		String query = (String)params.get("query");

		return this.ReadAss(query);
	}

	public ArrayList ReadAss(String query) {
		ArrayList Answer = new ArrayList();
		try {
			ResultSet Result = this.Query(query);
			ArrayList columnNames = new ArrayList();
			ResultSetMetaData  rsmd = Result.getMetaData();
			LinkedHashMap Row;
		
			int columnCount = rsmd.getColumnCount();
			int i;

			for (i=1; i<=columnCount; i++) {
				
				columnNames.add(rsmd.getColumnName(i));
			}
			while(Result.next()) {
				Row = new LinkedHashMap();
				for(i=0; i<columnNames.size(); i++) {
					Row.put((String)columnNames.get(i), Result.getString(i+1));
				}
				Answer.add(Row);
			}
			Result.close();

		} catch (Exception ex) {
			logger.Add(ex.getMessage());
			logger.Add("ass error");
		}


		return Answer;
	}

	public LinkedHashMap ReadRow(String sql) {
		ArrayList Res = this.ReadAss(sql);
		if (Res.size()>0)
			return (LinkedHashMap)(Res.get(0));
		else return new LinkedHashMap();
	}

	public LinkedHashMap InsertDB(LinkedHashMap Vars, String table) {
		String queryStr;
		String name;
		String value;
		queryStr = "insert into "+table+" set ";

		Iterator keys = Vars.keySet().iterator();

		while (keys.hasNext()) {
			name = (String)(keys.next());
			value = (String)(Vars.get(name));
			if (value == null)
				value = "";
			
			value.replace("\\", "\\\\");
			value.replace("\"", "\\\"");

			queryStr += " `"+name+"` =\""+value+"\"";
			if (keys.hasNext()) queryStr += ", ";
		}
		LinkedHashMap result = new LinkedHashMap();
		ResultSet Answer = this.exQuery(queryStr);

		this.queryInfo = new LinkedHashMap();

		try {
			while (Answer.next())
				this.queryInfo.put("inserted_id", Answer.getString(1));
		} catch (Exception ex) {
			logger.Add(ex.getMessage());
		}

		return result;

	}

	public LinkedHashMap EditDB(LinkedHashMap Vars, String table, String condition) {
		String queryStr;
		String name;
		String value;
		queryStr = "update "+table+" set ";

		Iterator keys = Vars.keySet().iterator();
		
		while (keys.hasNext()) {
			name = (String)(keys.next());
			value = (String)(Vars.get(name));

			value = value.replace("\\", "\\\\");
			value = value.replace("\"", "\\\"");
			
			queryStr += "`"+name+"`"+"=\""+value+"\"";
			if (keys.hasNext()) queryStr += ", ";
		}

		queryStr += " where "+condition;

		LinkedHashMap result = new LinkedHashMap();
		ResultSet Answer = this.exQuery(queryStr);

		this.queryInfo = new LinkedHashMap();

		try {
			while (Answer.next())
				this.queryInfo.put("inserted_id", Answer.getString(1));
		} catch (Exception ex) {
			logger.Add(ex.getMessage());
		}

		return result;
	}

	public ResultSet exQuery(String query) {
		ResultSet Answer = null;
		
			try {

				if (!connect.isValid(10)) {
					connect = DriverManager.getConnection((String)(Config.get("init_str")), (String)(Config.get("login")), (String)(Config.get("password")));
				}

                PreparedStatement stmt = connect.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
				stmt.executeUpdate();
				Answer = stmt.getGeneratedKeys();

            } catch (Exception e) {
				logger.Add(query);
				logger.Add(e.getMessage());
				logger.Add("DB connection error");
            } finally {
				return Answer;
			}

    }

	public ResultSet Query(String query) {
		ResultSet Answer = null;
			try {
				
				if (!connect.isValid(10)) {
					connect = DriverManager.getConnection((String)(Config.get("init_str")), properties);
				}
                Answer =  connect.createStatement().executeQuery(query);
				
            } catch (Exception e) {
				logger.Add(query);
				logger.Add(e.getMessage());
				logger.Add("DB connection error");
            } finally {
				return Answer;
			}
			
    }

}
