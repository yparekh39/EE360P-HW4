import java.net.*;
import java.io.*;
import java.util.*; 
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class Request implements Comparable<Request>{
	public int serverID;
	public int timestamp;
	public String command;

	public Request(String id, String ts, String cmd){
		timestamp = Integer.parseInt(ts);
		serverID = Integer.parseInt(id);
		command = cmd;
	}

	public Request(String id, String ts){
		serverID = Integer.parseInt(id);
		timestamp = Integer.parseInt(ts);
		command = "NOT MY CLIENT";
	}

	public boolean isMyRequest(int myID){
		return serverID == myID;
	}

	public String toString(){
		return "request " + timestamp + " " + serverID + " " + command;
	}

	@Override
	public int compareTo(Request reqB){
		if(timestamp < reqB.timestamp)
			return 1;
		else if(timestamp == reqB.timestamp){
			if(serverID < reqB.serverID)
				return 1;
			else
				return -1;
		}
		else
			return -1;
	}

}