package main;


import org.apache.fop.fonts.apps.TTFReader;


public class AddFont {
    public static void main(String args[]){
        String[] parameters = {
        "-ttcname",
        "kaiti",
        "/home/zhufree/Tools/fop-2.1/conf/kaiti.ttf", "/home/zhufree/Tools/fop-2.1/conf/kaiti.xml", };
        TTFReader.main(parameters);
        }
}