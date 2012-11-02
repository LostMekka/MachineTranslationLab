/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lab;

/**
 *
 * @author LostMekka
 */
public class LabControl {
	
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		if(args.length < 3){
			System.err.println("wrong parameter count!");
			System.exit(1);
		}
		LabControl control = new LabControl(args[0], args[0], args[0]);
	}
	
	private String base, sourceLocale, targetLocale;
	private Dictionary dictionary;
	private LanguageModel languageModel;
	private LengthModel lengthModel;

	public LabControl(String base, String sourceLocale, String targetLocale) {
		this.base = base;
		this.sourceLocale = sourceLocale;
		this.targetLocale = targetLocale;
	}
	
	public void train(){
		
	}
	
	public void resumeTrain(){
		
	}
	
	public void lookup(){
		
	}
		
}
