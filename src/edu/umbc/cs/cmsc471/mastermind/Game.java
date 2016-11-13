package edu.umbc.cs.cmsc471.mastermind;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

//a single game with a single player
//this is a threaded implementation that can be run given just the socket and a code to be guessed
public class Game extends Thread {
	public static long RUNTIME = 300000000000L;

	public Code _secretCode;
	public Generator _generator;
	public String _playerName;
	public int _port;

	public Socket _socket;
	public ServerSocket _serverSocket;
	public BufferedReader _in;
	public PrintWriter _out;

	private int _numGuesses;
	private boolean _correctlyGuessed;
	private long _startTime;
	private long _endTime;

	public Game(Code secretCode, Generator generator, String playerName, int port) {
		this._secretCode = secretCode;
		this._generator = generator;
		this._playerName = playerName;
		this._port = port;
	}

	@Override
	public void run() {
		try {
			_serverSocket = new ServerSocket(_port);
			_socket = _serverSocket.accept();
			System.out.println(_playerName + " connected!");
			_in = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
			_out = new PrintWriter(new PrintWriter(_socket.getOutputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		_numGuesses = 0;
		_correctlyGuessed = false;
		_startTime = System.nanoTime();

		//give them the numpegs and the set of colors
		sendProblemDescription(_secretCode);

		//start receiving guesses
		Code guess = null;
		//stop when they guess correctly
		while((guess == null || !guess.equals(_secretCode)) && System.nanoTime() - _startTime < RUNTIME) {
			guess = readGuess();
			_numGuesses++;
			sendResult(_secretCode.guess(guess));
		}

		_endTime = System.nanoTime();

		if(guess != null && guess.equals(_secretCode))
			_correctlyGuessed = true;

		try {
			_serverSocket.close();
			_socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int numGuesses() {
		return _numGuesses;
	}

	public boolean correctlyGuessed() {
		return _correctlyGuessed;
	}

	public long elapsedTime() {
		return _endTime - _startTime;
	}

	//4;white,black,red,green,yellow,blue
	public void sendProblemDescription(Code code) {
		String description = "";

		//write numpegs
		description += code._code.length + ";";

		//write colorspace
		for(int color_index = 0; color_index < code._colorSpace.length; color_index++) {
			description += code._colorSpace[color_index];
			if(color_index < code._colorSpace.length - 1)
				description += ",";
		}
		_out.println(description);
		_out.flush(); //THIS FLUSH IS REALLY REALLY IMPORTANT <---------------
	}

	//red,white,red,green
	public Code readGuess() {
		try {
			String guess;
			guess = _in.readLine();
			while(guess == null) {
				guess = _in.readLine();
			}

			return new Code(guess, _generator);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	//3,1
	public void sendResult(String result) {
		_out.println(result);
		_out.flush();
	}
}
