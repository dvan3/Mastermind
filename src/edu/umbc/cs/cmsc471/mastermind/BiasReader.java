/* Project: AI Mastermind
 * Team Name: Artificially Unintelligent
 * Names: Dave Van, Sean Cosentino
 * Date: 12/9/12
 */

package edu.umbc.cs.cmsc471.mastermind;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.HashMap;

public class BiasReader {
	
	public static String[] ALL_COLORS =
		{"Black",		"White",		"Red",			"Blue",
		 "Green",		"Yellow",		"Brown",		"Purple",
		 "Viridian",	"Pewter",		"Cerulean",		"Vermilion",
		 "Lavender",	"Celadon",		"Fuchsia",		"Saffron",
		 "Orange",		"Apricot",		"Gold",			"Gray",
		 "Magenta",		"Salmon",		"Silver",		"Copper",
		 "Sepia",		"Teal",			"Scarlet",		"Indigo",
		 "Pine",		"Plum",			"Maroon",		"Orchid",
		 "Peach",		"Olive",		"Tan",			"Sienna",
		 "Mauvelous",	"Razzmatazz",	"Manatee",		"Beaver"	//these are real colors, according to Crayola: http://en.wikipedia.org/wiki/List_of_Crayola_crayon_colors
		};
	
	private static PrintWriter file;

	public static void main(String[] args) throws IOException {
		// Takes one, one for filename (args[0])
		String biasFileName = args[0];
		int count = 0;
		
		// Hashmap to hold the colorcounts i.e. red->400
		HashMap<String, Integer> colorCount = new HashMap<String, Integer>();

		for(String x : ALL_COLORS){
			colorCount.put(x, 0);
		}

		try {
			
			Scanner fileReader = new Scanner(new FileInputStream(biasFileName));
			String line, sign;
			String[] lineArray;
			
			while(fileReader.hasNext()){
				line = fileReader.nextLine();
				lineArray = line.split(" ");
				sign = lineArray[0];

				if(sign.equals("+")){
					// positive
					for(String color : lineArray){
						if(!color.equals("+") && !color.equals("-")){
							count++;
							colorCount.put(color, colorCount.get(color) + 1);
						}
					}
				}else if(sign.equals("-")){
					// negative
					for(String color : lineArray){
						if(!color.equals("+") && !color.equals("-")){
							count++;
							colorCount.put(color, colorCount.get(color) - 1);
						}
					}
				}else{
					System.out.println("Unexpected input for the bias!");
				}
			}
			
			calculateProbabilities(colorCount, count);
			calculateProbabilitiesToText(colorCount, count);
			
			System.out.println(count);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void calculateProbabilities(HashMap<String, Integer> colorCount, int count) {
		for(String x : ALL_COLORS){
			System.out.println("Color: " + x + "Used: " + colorCount.get(x));
			System.out.println("Color: " + x + " Probability: " + (double)colorCount.get(x)/count);
		}
		return;
	}

	private static void calculateProbabilitiesToText(HashMap<String, Integer> colorCount, int count) throws IOException {
		FileOutputStream output = new FileOutputStream("probability.txt");
		file = new PrintWriter(output);
		
		for(String x : ALL_COLORS){
			file.println(x + " " + (double)colorCount.get(x)/count);
		}
		file.close();
		return;
	}
}
