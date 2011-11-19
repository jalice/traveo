/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mediaTools;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import debug.*;
import java.util.LinkedHashMap;
/**
 *
 * @author user
 */
public class bfImage {

	protected BufferedImage buf;

	public void loadImage(String url) {
		try {
			buf = ImageIO.read(new URL(url));
			
		} catch (Exception ex) {
			logger.Add(ex.getMessage());
		}

	}

	public int getWidth() {
		try {
			return buf.getWidth();
		} catch(Exception ex) {
			return 0;
		} 
	}

	public int getHeight() {
		try {
			return buf.getHeight();
		} catch(Exception ex) {
			return 0;
		} 
	}

	public String getInfo(LinkedHashMap param) {
		this.loadImage((String) param.get("url"));

		return "" + this.getWidth() +";" + this.getHeight() + ";" +  ((String) param.get("url")) +";" ;
	}
	
}
