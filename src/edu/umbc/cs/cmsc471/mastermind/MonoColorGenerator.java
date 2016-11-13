package edu.umbc.cs.cmsc471.mastermind;

import java.util.Random;

public class MonoColorGenerator extends Generator {
	public MonoColorGenerator(int numPegs, int numColors) {
		super(numPegs, numColors);
	}

	public MonoColorGenerator(int numPegs, String[] colorSpace) {
		super(numPegs, colorSpace);
	}

	@Override
	public Code generate() {
		Code code = new Code(_colorSpace, _numPegs, false);
		String color = _colorSpace[(new Random()).nextInt(_colorSpace.length)];
		for(int pegIndex = 0; pegIndex < _numPegs; pegIndex++)
			code._code[pegIndex] = color;
		return code;
	}

	@Override
	public Code generateImpossibleCode() {
		for(int numAttempts = 0; numAttempts < 100; numAttempts++) {
			Code code = new Code(_colorSpace, _numPegs, true);
			String color = code._code[0];
			for(int pegIndex = 0; pegIndex < code.numPegs(); pegIndex++)
				if(!code._code[pegIndex].equals(color))
					return code;
		}
		return null;
	}
	
	public static void main(String[] args) {
		Generator test = new MonoColorGenerator(20,20);
		test.printCorpus(test.generateCorpus(40, 10), "monoColorCorpus.txt");
	}
}
