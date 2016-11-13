/* Project: AI Mastermind
 * Team Name: Artificially Unintelligent
 * Names: Dave Van, Sean Cosentino
 * Date: 11/24/12
 */

package edu.umbc.cs.cmsc471.mastermind;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeMap;

public class Player {
	public Socket _socket;
	public BufferedReader in;
	public PrintWriter out;

	public int count;

	public int _numPegs;
	public String[] _colorSpace;

	public final static String IP = "127.0.0.1"; //this should be the IP of the tournament
	public final static int DEFAULTPORT = 4444;  //this should be the port of the tournament

	public Player(int port, boolean bias, String biasFile) throws FileNotFoundException {
		boolean RANDOM_GUESSES = false;
		//open the various streams
		try {
			_socket = new Socket(IP, port);
			in = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
			out = new PrintWriter(_socket.getOutputStream());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//read the basic info
		readProblemDescription();

		if(RANDOM_GUESSES){
			//start writing guesses and getting back results
			String response = null;
			while(response == null || Integer.parseInt(response.split(",")[0]) != _numPegs) {
				sendGuess();
				response = receiveResponse();
				System.out.println(response);
			}
		}else{
			//start writing NONRANDOM guesses and getting back results
			//debugging print out
			//System.out.println(RANDOM_GUESSES);

			String response = null;
			int pegsRemaining = _numPegs;
			int redPegs, whitePegs = 0;
			boolean doneColorDist = false;

			//colorDistribution holds the colors used in the sercret code
			HashMap<String, Integer> colorDistribution = new HashMap<String, Integer>();

			// previous, current are guesses in color,color,color,color,... form
			String[] current = new String[_numPegs];
			String[] redWhite = new String[2];

			// usedGuesses holds the list of sent guesses
			HashMap<String, Integer> usedGuesses = new HashMap<String, Integer>();

			// holds the gain value for a mutation
			int gain = 0;

			int numberOfThisColor = 0;
			ArrayList<String> organism = new ArrayList<String>();

			// holds the two swapped pegs for one mutation
			String swapped = "";

			HashMap<String, Double> colorProb = new HashMap<String, Double>();
			ValueComparator bvc =  new ValueComparator(colorProb);
			TreeMap<String,Double> sorted_map = new TreeMap<String,Double>(bvc);
			ArrayList<String> newColorSpace = new ArrayList<String>();
			if(bias){
			
			Scanner fileReader = new Scanner(new FileInputStream(biasFile));
			String line;
			String[] lineArray;


			//reading file
				while(fileReader.hasNext()){
					line = fileReader.nextLine();
					lineArray = line.split(" ");

					//extracting probabilities from text file into hashmap
					for(int i = 0; i < _colorSpace.length; i++)
					{
						if(lineArray[0].equals(_colorSpace[i]))
						{
							colorProb.put(lineArray[0], Double.parseDouble(lineArray[1]));
						}
					}
				}
			}
			//sorting the colors by frequency
			sorted_map.putAll(colorProb);

			//putting the colors into a newColorSpace
			for(String key : sorted_map.keySet())
			{
				newColorSpace.add(key);
			}

			//USE THIS FOR BIAS TOURNAMENT RUN!!!!!!
			//FALSE = NO BIAS READER!!
			//TRUE = USE BIAS READER!!


			if(bias)
			{
				//overriding the old color space with the new color space based on frequency
				_colorSpace = newColorSpace.toArray(new String[newColorSpace.size()]);
			}

			// the locked peg string 0000101 format
			String locked = "";
			for(int i = 0; i < _numPegs; i++){
				locked += '0';
			}

			//debugging printout
			//			System.out.println("locked = " + locked);

			//zeroing all colors in the colorSpace first
			for(int k = 0; k <= _colorSpace.length - 1; k++){
				colorDistribution.put(_colorSpace[k], 0);
			}

			//***********************MONOCHROMATIC GUESSING *************************
			while((response == null || Integer.parseInt(response.split(",")[0]) != _numPegs) && !doneColorDist)  {			
				String[] pegVec = new String[_numPegs];

				//this is the n colors - 1 guesses to find the color dist. 
				for(int i = 0; i <= _colorSpace.length - 2; i++){

					// intialize a monochromatic guess peg vector
					for(int y = 0; y <= _numPegs - 1; y++){
						pegVec[y] = _colorSpace[i];
					}

					if(pegsRemaining > 0){
						Code ourGuess = new Code(_colorSpace, pegVec);
						sendGuess(ourGuess);
						response = receiveResponse();
						redWhite = response.split(",");
						redPegs = Integer.parseInt(redWhite[0]);
						colorDistribution.put(_colorSpace[i], redPegs);
						pegsRemaining -= redPegs;
						System.out.println(count + ": " + response + " Guess : " + ourGuess.toString());
					}else if(pegsRemaining == 0){
						// here we have determined the color distribution before color-1 guesses
						doneColorDist = true;
						break;
					}

					// after color - 1th guess, pegs still remaining, we know how many
					// of the last color there will be
					if(i == _colorSpace.length - 2 && pegsRemaining != 0){
						colorDistribution.put(_colorSpace[_colorSpace.length - 1], pegsRemaining);
						pegsRemaining = 0;
						doneColorDist = true;
						break;
					}
				} // end color iteration loop
			} // ******************END MONOCHROMATIC********************

			// here we are making the original member of the population
			for(int i = 0; i <= _colorSpace.length - 1; i++){
				numberOfThisColor = colorDistribution.get(_colorSpace[i]);

				// append color to end of organism
				for(int y = 0; y < numberOfThisColor; y++)
					organism.add(_colorSpace[i]);
			}

			int index = 0;
			for(String color : organism){
				current[index] = color;
				index++;
			}	

			Code parent = new Code(_colorSpace, current);
			response = null;

			//*******************FIRST INITIAL GUESS***********************
			while(response == null){
				// Send our initial guess
				sendGuess(parent);
				// Parse the response
				response = receiveResponse();
				redWhite = response.split(",");
				redPegs = Integer.parseInt(redWhite[0]);
				whitePegs = Integer.parseInt(redWhite[1]);
				System.out.println(count + ": " + response + " Guess : " + parent.toString());
			}
			// add the original organism to the usedGuesses
			usedGuesses.put(parent.toString(), whitePegs);

			//*****************MUTATION GUESSING********************
			while(response == null || Integer.parseInt(response.split(",")[0]) != _numPegs) {

				Code child = new Code(_colorSpace, copyArray(parent._code));

				//mutating the child
				do{
					swapped = child.mutate(locked);
					if(usedGuesses.containsKey(child.toString())){
						child = new Code(_colorSpace, copyArray(parent._code));
					}
				}while(usedGuesses.containsKey(child.toString()));

				sendGuess(child);

				// Parse the response
				response = receiveResponse();
				redWhite = response.split(",");
				redPegs = Integer.parseInt(redWhite[0]);
				whitePegs = Integer.parseInt(redWhite[1]);

				//Debugging print out
				System.out.println(count + ": " + response + " Guess : " + child.toString());

				// add the new organism to the usedGuesses
				usedGuesses.put(child.toString(), whitePegs);

				gain = -1 * (usedGuesses.get(child.toString()) - usedGuesses.get(parent.toString()));

				// child better than parent
				if(gain > 0){
					//set previous to current
					if(gain == 2){
						locked = updateLocked(locked, swapped);
					}

					parent = new Code(_colorSpace, copyArray(child._code));

					//Debugging print out
					//					System.out.println("Swapped = " + swapped);
					//					System.out.println("Locked = " + locked);
					//					System.out.println("Gain = " + gain);
					//					System.out.println("Child > Parent");
					//					System.out.println("Parent now " + parent.toString());
					//					System.out.println("Child now " + child.toString());

				}else if(gain < 0){
					// old one is better
					// set current to previous
					//parent = new Code(_colorSpace, temp);
					if(gain == -2){
						locked = updateLocked(locked, swapped);
					}

					//Debugging print out
					//					System.out.println("Swapped = " + swapped);
					//					System.out.println("Locked = " + locked);
					//					System.out.println("Gain = " + gain);
					//					System.out.println("Child < Parent");
					//					System.out.println("Parent now " + parent.toString());
					//					System.out.println("Child now " + child.toString());

				}else{
					// the solutions were of equal fitness, do nothing

					//Debugging print out
					//					System.out.println("Child = Parent");
					//					System.out.println("Gain = " + gain);
					//					System.out.println("Parent now " + parent.toString());
					//					System.out.println("Child now " + child.toString());

				}
			} // end mutation guessing response while loop

			//debugging print out of color amount
			for(int i = 0; i <= _colorSpace.length - 1; i++)
				System.out.println("Color: " + _colorSpace[i] + " Amt: " + colorDistribution.get(_colorSpace[i]));
		}
	}

	//parse the numPegs and set of colors for guessing
	public void readProblemDescription() {
		String description;
		try {
			description = in.readLine();
			while(description == null)
				description = in.readLine();
			String[] split1 = description.split(";");
			_numPegs = Integer.parseInt(split1[0]);
			_colorSpace = split1[1].split(",");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	//make a random guess
	public void sendGuess() {
		out.println((new Generator(_numPegs, _colorSpace)).generate());
		out.flush(); //REALLY IMPORTANT TO FLUSH
	}

	// make a non random guess
	public void sendGuess(Code ourGuess){
		count++;
		out.println(ourGuess.toString());
		out.flush();
	}

	//parse a response (I don't actually do any parsing here, you need to do that)
	public String receiveResponse() {
		String response;
		try {
			response = in.readLine();
			while(response == null)
				response = in.readLine();
			return response;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	// thanks for working with string[]'s Kevin, they are 
	// a wonderful creation...not.
	public String[] copyArray(String[] array){
		String[] newArray = new String[array.length];
		for(int i = 0; i < array.length; i++){
			newArray[i] = array[i];
		}
		return newArray;
	}

	// update the locked positions
	public String updateLocked(String locked, String swapped){
		String newlocked = "";
		String[] split = swapped.split(",");
		char[] lockedtemp = locked.toCharArray();

		lockedtemp[Integer.parseInt(split[0])] = '1';
		lockedtemp[Integer.parseInt(split[1])] = '1';

		for(char peg : lockedtemp){
			newlocked += peg;
		}

		return newlocked;
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("args length: " + args.length + "\n");
		if(args.length == 0){
			System.out.println("No BIAS file included.");
			Player player = new Player(Player.DEFAULTPORT, false, "");
			return;
		}else{
			String biasFile = args[0];
			Player player = new Player(Player.DEFAULTPORT, true, biasFile);
			return;
		}
	}	
}
