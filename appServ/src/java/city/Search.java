/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package city;
import debug.logger;
import mysql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import multiThread.MTLauncher;

/**
 *
 * @author user
 */
public class Search {
	static public ConcurrentHashMap cache = new ConcurrentHashMap();

	static public LinkedHashMap CheckCity(String City) {
		ArrayList Res = Search.SearchCity(City, 1);
		LinkedHashMap Result = new LinkedHashMap();
		
		if (Res.size()>0) {	
			Result.put("error", false);
			Result.put("city", Res.get(0));

			return Result;
		}
		Result.put("error", true);
		return Result;
	}

	static public ArrayList EnSearch(LinkedHashMap params) {

		DBShell dbs = new DBShell();
		DB db = dbs.getDB();

		String Search_str = (String)params.get("search_str");
		String limit = (String)params.get("limit");

		ArrayList Result = db.ReadAss("select CONCAT(c.title, ', ', cn.title) as name, hc.hc_id from cities c, countries cn, hc_cities hc"
				+" where cn.id = c.country_id AND hc.id = c.city_id AND " + Search_str +" limit "+ limit);

		dbs.releaseDB(db);

		return Result;
	}

	static public ArrayList RuSearch(LinkedHashMap params) {

		DBShell dbs = new DBShell();
		DB db = dbs.getDB();

		String Search_str = (String)params.get("search_str");
		String limit = (String)params.get("limit");

		ArrayList Result = db.ReadAss("select CONCAT(c.title, ', ', cn.title) as name, hc.hc_id from cities_ru c, countries_ru cn, hc_cities hc"
				+" where cn.id = c.country_id AND hc.id = c.city_id AND " + Search_str +" limit "+ limit);

		dbs.releaseDB(db);

		return Result;
	}

	static public int whatLang(String Str) {
		char tmp[] = Str.toCharArray();
		if (tmp[0]<123) return 1;
		else return 2;
	}

	static public ArrayList SearchCity(String City, int limit) {
		City.toLowerCase();
		
		//--- from cache
		ArrayList Result = (ArrayList)cache.get(City);
		if (Result != null) return Result;
		//-------------

		StringTokenizer StrExplode = new StringTokenizer(City, " ,-\t");
		String Search_str = "1";
		String Order_str = "1";

		String Search_str_ru = "1";
		String Order_str_ru = "1";

		int Lang;

		String Word, Word_ru, wWord, Word_en;
		while (StrExplode.hasMoreTokens()) {
			Word = StrExplode.nextToken();
			if (Word.equals("")) continue;

			Lang = whatLang(City);

			if (Lang==1) {
				Word_en = DB.quote(Word);
				Search_str += " AND c.search LIKE '%"+ Word_en +"%'";
			    Order_str += " + (case when position('"+Word_en+"' in c.search)>0 THEN position('"+Word_en+"' in c.search) else 1000  end)";

				Word_ru = DB.quote(Search.Translit(Word));
				wWord = DB.quote(Search.WrongLang(Word));
				Search_str_ru += " AND (c.search LIKE '%"+ Word_ru +"%' or c.search LIKE '%"+ wWord +"%')";
				Order_str_ru += " + (case when (position('"+Word_ru+"' in c.search) + position('"+wWord+"' in c.search)) >0 then (position('"+Word_ru+"' in c.search) + position('"+wWord+"' in c.search)) else 1000 end)";
			} else {
				Word_ru = DB.quote(Word);
				Search_str_ru += " AND c.search LIKE '%"+ Word_ru +"%'";
			    Order_str_ru += " + (case when position('"+Word_ru+"' in c.search)>0 THEN position('"+Word_ru+"' in c.search) else 1000 end)";

				wWord = DB.quote(Search.WrongLang(Word));
				Search_str += " AND (c.search LIKE '%"+ wWord +"%')";
				Order_str += " + (case when position('"+wWord+"' in c.search)>0 then position('"+wWord+"' in c.search) else 1000 end)";
			}
		}

		Search_str += " order by "+Order_str + " ASC, c.hotel_count DESC";
		Search_str_ru += " order by "+Order_str_ru + " ASC, c.hotel_count DESC";

		LinkedHashMap param = new LinkedHashMap();
		param.put("search_str", Search_str);
		param.put("limit", ""+limit);

		LinkedList objects = new LinkedList();

		Search tmpObj = new Search();

		objects.add(tmpObj);
		objects.add(tmpObj);

		ArrayList methods = new ArrayList();
		methods.add("EnSearch");
		methods.add("RuSearch");

		
		ArrayList params = new ArrayList();
		params.add(param);

		param = new LinkedHashMap();
		param.put("search_str", Search_str_ru);
		param.put("limit", ""+limit);
		params.add(param);

		ArrayList tmpResult = MTLauncher.Launch(objects, methods, params);

		Result = new ArrayList();
		if (Search.whatLang(City)==1)
		{
			if (tmpResult.size()>0) Result.addAll((ArrayList)tmpResult.get(0));
			if (tmpResult.size()>1) Result.addAll((ArrayList)tmpResult.get(1));
		} else {
			if (tmpResult.size()>1) Result.addAll((ArrayList)tmpResult.get(1));
			if (tmpResult.size()>0) Result.addAll((ArrayList)tmpResult.get(0));
		}

		cache.put(City.toLowerCase(), Result);
		return Result;
	}

	static public ArrayList SearchCity(String City) {
		return Search.SearchCity(City, 10);
	}

	static public String WrongLang(String Str) {
		char symb[] = Str.toCharArray();

		int strLen = Str.length();
		for (int i=0; i<strLen; i++) {
			if (wrong[symb[i]]>0) {
				symb[i] = wrong[symb[i]];
			}
		}

		return new String(symb);
	}

	static public String Translit(String Str) {
		String tmp;
		tmp = Str.replaceAll("zh", "ж");
		tmp = tmp.replaceAll("ch", "ч");
		tmp = tmp.replaceAll("ya", "я");
		tmp = tmp.replaceAll("ye", "е");
		tmp = tmp.replaceAll("yi", "и");
		tmp = tmp.replaceAll("yu", "ю");

		char symb[] = tmp.toCharArray();

		int strLen = tmp.length();
		for (int i=0; i<strLen; i++) {
			if (translit[symb[i]]>0) {
				symb[i] = translit[symb[i]];
			}
		}

		return new String(symb);
	}

	static public synchronized void init() {
		translit = new char[1200];
		translit['w']='в';
		translit['e']='е';
		translit['r']='р';
		translit['t']='т';
		translit['y']='ы';
		translit['u']='у';
		translit['i']='и';
		translit['o']='о';
		translit['p']='п';
		translit['a']='а';
		translit['s']='с';
		translit['d']='д';
		translit['f']='ф';
		translit['g']='г';
		translit['h']='х';
		translit['j']='й';
		translit['k']='к';
		translit['l']='л';
		translit['z']='з';
		translit['x']='х';
		translit['c']='ц';
		translit['v']='в';
		translit['b']='б';
		translit['n']='н';
		translit['m']='м';
		translit['\'']='ь';

		wrong = new char[1200];
		wrong['q'] = 'й';
		wrong['w'] = 'ц';
		wrong['e'] = 'у';
		wrong['r'] = 'к';
		wrong['t'] = 'е';
		wrong['y'] = 'н';
		wrong['u'] = 'г';
		wrong['i'] = 'ш';
		wrong['o'] = 'щ';
		wrong['p'] = 'з';
		wrong['['] = 'х';
		wrong[']'] = 'ъ';
		wrong['{'] = 'х';
		wrong['}'] = 'ъ';
		wrong['a'] = 'ф';
		wrong['s'] = 'ы';
		wrong['d'] = 'в';
		wrong['f'] = 'а';
		wrong['g'] = 'п';
		wrong['h'] = 'р';
		wrong['j'] = 'о';
		wrong['k'] = 'л';
		wrong['l'] = 'д';
		wrong[';'] = 'ж';
		wrong['\''] = 'э';
		wrong['z'] = 'я';
		wrong['x'] = 'ч';
		wrong['c'] = 'с';
		wrong['v'] = 'м';
		wrong['b'] = 'и';
		wrong['n'] = 'т';
		wrong['m'] = 'ь';
		wrong[','] = 'б';
		wrong['.'] = 'ю';

		wrong['й'] = 'q';
		wrong['ц'] = 'w';
		wrong['у'] = 'e';
		wrong['к'] = 'r';
		wrong['е'] = 't';
		wrong['н'] = 'y';
		wrong['г'] = 'u';
		wrong['ш'] = 'i';
		wrong['щ'] = 'o';
		wrong['з'] = 'p';
		wrong['х'] = '[';
		wrong['ъ'] = ']';
		wrong['ф'] = 'a';
		wrong['ы'] = 's';
		wrong['в'] = 'd';
		wrong['а'] = 'f';
		wrong['п'] = 'g';
		wrong['р'] = 'h';
		wrong['о'] = 'j';
		wrong['л'] = 'k';
		wrong['д'] = 'l';
		wrong['ж'] = ';';
		wrong['э'] = '\'';
		wrong['я'] = 'z';
		wrong['ч'] = 'x';
		wrong['с'] = 'c';
		wrong['м'] = 'v';
		wrong['и'] = 'b';
		wrong['т'] = 'n';
		wrong['ь'] = 'm';
		wrong['б'] = ',';
		wrong['ю'] = '.';

	}

	static private char[] translit;
	static private char[] wrong;
}
