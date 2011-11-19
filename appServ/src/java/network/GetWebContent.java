/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
public class GetWebContent {

	public String GetHeader() {
		String Header = "";

		Header =
			"Accept:*/* \r\n" +
			"Accept-Language:en \r\n"+
			"User-Agent:Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 1.1.4322) \r\n" +
			"Accept-Encoding:gzip, deflate \r\n" +
			"Connection:Close \r\n\r\n";

		return Header;
	}
	
	public String GetContent(String URL_s) {
		String Result = "";
		URL url_helper;
		
		try {
			 url_helper = new URL(URL_s);
			 Socket sock = new Socket ();

             sock.connect ( new InetSocketAddress (url_helper.getHost(), 80 ), 500);
             PrintWriter out = new PrintWriter(sock.getOutputStream(), true);

			 String data = "GET "+url_helper.getPath() +"?"+ url_helper.getQuery()+" HTTP/1.0\r\n";
			 data += "Host: "+url_helper.getHost()+":80\r\n";
			 data += this.GetHeader();
			 
			 out.print(data);
             out.flush();
             BufferedReader in = new BufferedReader ( new InputStreamReader ( sock.getInputStream () ) );

             String read = "";
			 String buf;
             
			 boolean startContent = false;

			 while (true) {
				 
				 buf = in.readLine();
				 if (buf == null) break;

				 if (startContent)
					read += buf;

				 if (buf.indexOf("</CitySearchResponse>")!=-1) break;

				 if (buf.equals(""))
					 startContent = true;
			 }

             out.close();
             in.close();
			 sock.close();
             return read;
		}
		catch (Exception e) {
			Result = e.getMessage();
		}

		return Result;
	}

	public String getURL(String URL, LinkedHashMap Data) {
		URL += "?";
		String key;
		Iterator keys = Data.keySet().iterator();
		while (keys.hasNext()) {
			key = (String)(keys.next());
			try {
				URL += URLEncoder.encode(key, "utf8") + "=" + URLEncoder.encode((String)(Data.get(key)),"utf8") + "&";
			} catch(IOException e) {
				break;
			}

		}

		return this.GetContent(URL);
	}
}