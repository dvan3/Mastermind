package edu.umbc.cs.cmsc471.mastermind;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class Code {
	public final String[] _colorSpace;	//a set of all the possible colors for each of the pegs
	public String[] _code;				//the pegs making up this code
	//private Map<String, Integer> _colorCounts;	//count how many times each color is used in this code

	//make a new code with a random or empty peg vector
	public Code(String[] colorspace, int numPegs, boolean randomFill) {
		this._colorSpace = colorspace;
		this._code = new String[numPegs];
		//if randomFill, then randomly pick a color for each peg
		if(randomFill) {
			Random r = new Random();
			for(int color_index = 0; color_index < numPegs; color_index++)
				_code[color_index] = _colorSpace[r.nextInt(_colorSpace.length)];
		}
	}

	//make a new code with a predefined peg vector
	public Code(String[] colorspace, String[] code) {
		this._colorSpace = colorspace;
		this._code = code;
	}

	//used for the tournament to parse guesses
	public Code(String guess, Generator generator) {
		String[] colors = guess.split(",");
		this._colorSpace = generator._colorSpace;
		this._code = colors;
	}
	
	// Modifies the underlying code and return the two positions that were swapped in pos1,pos2 string form
	public String mutate(String locked){

		Random generator = new Random (System.currentTimeMillis());			
		int pos1;
		int pos2;
		do{
			pos1 = Math.abs(generator.nextInt() % _code.length);
		}while(locked.charAt(pos1) == '1');
		// randomly generate two positions that are not the same
		// and that do not hold the same color in the current guess
		// and that are not already locked
		do{
			pos2 = Math.abs(generator.nextInt() % _code.length);
		}while(pos1 == pos2 || _code[pos1].equals(_code[pos2]) || locked.charAt(pos2) == '1');

		// SWAP EM DAWG
		String temp = _code[pos1];
		_code[pos1] = _code[pos2];
		_code[pos2] = temp;

		// We want to return the swapped positions as pos1,pos2
		String newlocked;
		newlocked = "" + pos1 + "," + pos2;
		return newlocked;
	}

	public int numPegs() {
		return _code.length;
	}
	
	public boolean equals(Code rhs) {
		if(_code.length != rhs._code.length)
			return false;
		for(int code_index = 0; code_index < _code.length; code_index++)
			if(!_code[code_index].equals(rhs._code[code_index]))
				return false;
		return true;
	}
	
	public String guess(Code attempt) {
		if(_code.length != attempt._code.length)
			return null;
		
		int red_result = 0;
		int white_result = 0;
		
		Map<String, Integer> my_color_counts = new LinkedHashMap<String, Integer>();
		Map<String, Integer> att_color_counts = new LinkedHashMap<String, Integer>();
		
		//this loop will compute the red peg result, as well as counting how many of each
		//color were used in each code, which will be used to compute the white result
		for(int code_index = 0; code_index < _code.length; code_index++) {
			String my_peg = _code[code_index];
			String att_peg = attempt._code[code_index];
			
			if(my_peg.equals(att_peg))
				red_result++;
			
			Integer my_count = my_color_counts.get(my_peg);
			if(my_count == null)
				my_color_counts.put(my_peg, 1);
			else
				my_color_counts.put(my_peg, my_count + 1);
			
			Integer att_count = att_color_counts.get(att_peg);
			if(att_count == null)
				att_color_counts.put(att_peg, 1);
			else
				att_color_counts.put(att_peg, att_count + 1);
		}
		
		//compute the white result
		for(String color : my_color_counts.keySet()) {
			Integer my_count = my_color_counts.get(color);
			Integer att_count = att_color_counts.get(color);
			if(my_count != null && att_count != null)
				white_result += Math.min(my_count, att_count);
		}
		white_result -= red_result; //any red peg matches were double counted
		
		return "" + red_result + "," + white_result;
	}

	public String toString() {
		String returnval = "";
		for(int color_index = 0; color_index < _code.length; color_index++) {
			returnval += _code[color_index];
			if(color_index < _code.length - 1)
				returnval += ",";
		}
		return returnval;
	}
	
	public String toSpaceDelimitedString() {
		String returnval = "";
		for(int color_index = 0; color_index < _code.length; color_index++) {
			returnval += _code[color_index];
			if(color_index < _code.length - 1)
				returnval += " ";
		}
		return returnval;
	}
}
