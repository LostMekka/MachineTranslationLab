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
	
	private String base;

	public Writable(String base) {
		this.base = base;
	}

	public String getBase() {
		return base;
	}

	public boolean writeToFile(String filename){
		boolean ans = true;
		try {
			OutputStream file = new FileOutputStream(filename);
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
	
	public static Writable loadFromFile(String filename){
		try {
			//use buffering
			InputStream file = new FileInputStream(filename);
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
