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
	
	public Writable loadFromFile(String base){
		try {
			//use buffering
			InputStream file = new FileInputStream(getFileName(base));
			InputStream buffer = new BufferedInputStream(file);
			try (ObjectInput input = new ObjectInputStream(buffer)) {
				return (Writable)input.readObject();
			} catch (Exception e) {
				return null;
			}
		} catch (IOException ex) {
			return null;
		}
	}
	
}
