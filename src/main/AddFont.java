package main;


import org.apache.fop.fonts.apps.TTFReader;


public class AddFont {
    public static void main(String args[]){
        String[] parameters = {
	        "-ttcname",
	        "micro",
	        "F:\\code\\libs\\fop-2.1\\conf\\micro.ttf", 
	        "F:\\code\\libs\\fop-2.1\\conf\\micro.xml",
        };
        TTFReader.main(parameters);
    }
}