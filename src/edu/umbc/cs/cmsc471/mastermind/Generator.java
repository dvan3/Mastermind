package edu.umbc.cs.cmsc471.mastermind;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Generator {
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
	
	public int _numPegs, _numColors;
	public String[] _colorSpace;
	
	public Generator(int numPegs, int numColors) {
		this._numPegs = numPegs;
		this._numColors = numColors;
		this._colorSpace = new String[numColors];
		for(int color_index = 0; color_index < numColors; color_index++)
			this._colorSpace[color_index] = ALL_COLORS[color_index];
	}
	
	public Generator(int numPegs, String[] colorSpace) {
		this._numPegs = numPegs;
		this._numColors = colorSpace.length;
		this._colorSpace = colorSpace;
	}
	
	//just generates a dumb, all-random code
	public Code generate() {
		Code returnval = new Code(_colorSpace, _numPegs, true);
		return returnval;
	}
	
	public Code generateImpossibleCode() {
		return generate();
	}
	
	public Map<Code, Boolean> generateCorpus(int numPositive, int numNegative) {
		Map<Code, Boolean> corpus = new LinkedHashMap<Code, Boolean>();
		for(int codeIndex = 0; codeIndex < numPositive; codeIndex++)
			corpus.put(generate(), true);
		for(int codeIndex = 0; codeIndex < numNegative; codeIndex++)
			corpus.put(generateImpossibleCode(), false);
		return corpus;
	}
	
	public void printCorpus(Map<Code, Boolean> corpus, String fileName) {
		try {
			PrintWriter out = new PrintWriter(new File(fileName));
			for(Code code : corpus.keySet()) {
				if(corpus.get(code))
					out.print("+ ");
				else
					out.print("- ");
				out.println(code.toSpaceDelimitedString());
				out.flush();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		Generator test = new Generator(8,8);
		test.printCorpus(test.generateCorpus(40, 10), "randomCorpus.txt");
	}
}
