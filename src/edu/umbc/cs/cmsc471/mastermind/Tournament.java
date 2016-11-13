package edu.umbc.cs.cmsc471.mastermind;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Tournament {
	private int nextPort = 4444;

	public List<String> _players;						//list of player names (used as keys)
	public Generator _generator;						//a code generator
	public Map<String, Socket> _sockets;				//the list of open sockets
	public Map<String, ServerSocket> _serverSockets;	//the listening server sockets (not used much)
	public Map<String, BufferedReader> _inputStreams;	//the input streams
	public Map<String, PrintWriter> _outputStreams;		//the output streams

	//dummy constructor, init empty structs
	public Tournament() {
		_players = new ArrayList<String>();
		_sockets = new LinkedHashMap<String, Socket>();
		_serverSockets = new LinkedHashMap<String, ServerSocket>();
		_inputStreams = new LinkedHashMap<String, BufferedReader>();
		_outputStreams = new LinkedHashMap<String, PrintWriter>();
	}

	//add a player and open the various sockets for that player
	public void addPlayer(String newPlayer) {
		try {
			ServerSocket serverSocket = new ServerSocket(nextPort++);
			Socket socket = serverSocket.accept();
			_serverSockets.put(newPlayer, serverSocket);
			_sockets.put(newPlayer, socket);
			_players.add(newPlayer);
			_inputStreams.put(newPlayer, new BufferedReader(new InputStreamReader(socket.getInputStream())));
			_outputStreams.put(newPlayer, new PrintWriter(new PrintWriter(socket.getOutputStream())));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//begin running a tournament
	public void startGame(int numPegs, int numColors) {
		//make a new gen for this game, then get a target code for it
		_generator = new Generator(numPegs, numColors);
		Code secretCode = _generator.generate();

		//this just does each player one at a time, this will be threaded on tournament day
		for(String player : _players) {
			//give them the numpegs and the set of colors
			sendProblemDescription(player, secretCode);
			
			//start receiving guesses
			Code guess = null;
			int numGuesses = 0;
			//stop when they guess correctly
			while(guess == null || !guess.equals(secretCode)) {
				guess = readGuess(player);
				numGuesses++;
				System.out.println("" + numGuesses + ": " + guess);
				sendResult(player, secretCode.guess(guess));
			}
		}
		
		//just here to leave the socket open
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//4;white,black,red,green,yellow,blue
	public void sendProblemDescription(String player, Code code) {
		PrintWriter out = _outputStreams.get(player);
		String description = "";
		
		//write numpegs
		description += code._code.length + ";";
		
		//write colorspace
		for(int color_index = 0; color_index < code._colorSpace.length; color_index++) {
			description += code._colorSpace[color_index];
			if(color_index < code._colorSpace.length - 1)
				description += ",";
		}
		out.println(description);
		out.flush(); //THIS FLUSH IS REALLY REALLY IMPORTANT <---------------
	}

	//red,white,red,green
	public Code readGuess(String player) {
		try {
			BufferedReader in = _inputStreams.get(player);
			String guess;
			guess = in.readLine();
			while(guess == null) {
				guess = in.readLine();
			}

			return new Code(guess, _generator);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	//3,1
	public void sendResult(String player, String result) {
		PrintWriter out = _outputStreams.get(player);
		out.println(result);
		out.flush();
	}

	public static void main(String[] args) {
		Tournament t = new Tournament();
		t.addPlayer("testPlayer");
		t.startGame(8, 8);
	}
}
