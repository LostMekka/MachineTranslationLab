/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lab;

import java.io.*;

/**
 *
 * @author LostMekka
 */
public abstract class Writable implements Serializable {
	
	public abstract String getFileName(String base);
	
	public boolean writeToFile(String base){
		boolean ans = true;
		try {
			//use buffering
			OutputStream file = new FileOutputStream(getFileName(base));
			OutputStream buffer = new BufferedOutputStream(file);
			try (ObjectOutput output = new ObjectOutputStream(buffer)) {
				output.writeObject(this);
			} catch (Exception e) {
				ans = false;
			}
		} catch (IOException ex) {
			ans = false;
		}
		return ans;
	}
	
}
