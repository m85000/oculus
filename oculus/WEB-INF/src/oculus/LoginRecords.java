package oculus;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;

public class LoginRecords {

	private static final String filename = System.getenv("RED5_HOME") + "\\log\\login.txt";
	public static Vector<Record> list = new Vector<Record>();
	public static State state = State.getReference();
	public static final String PASSENGER = "passenger";
	public static final String DRIVER = "driver";
	public static final int MAX_RECORDS = 50;
	
	// TODO: only allow user to sign in once from a given ip??
	// This should trigger an admin gmail warning if ppl share passwords?
	
	public LoginRecords() {}
	
	public void beDriver(String ip) {
		
		if(isConnected(state.get(State.user), ip))
			System.out.println("user is allready connected!");
		
		list.add(new Record(state.get(State.user), DRIVER, ip));
		state.set(State.userisconnected, true);
		state.set(State.logintime, System.currentTimeMillis());
	}
	
	public void bePassenger(String ip) {
		list.add(new Record(state.get(State.user), PASSENGER, ip));
		state.set(State.userisconnected, true);
	}
	

	public void beDriver() {
		
	//	if(isConnected(state.get(State.user), ip))
		
		list.add(new Record(state.get(State.user), DRIVER, "xxx.xxx.xxx.xxx"));
		state.set(State.userisconnected, true);
		state.set(State.logintime, System.currentTimeMillis());
	}
	
	public void bePassenger() {
		list.add(new Record(state.get(State.user), PASSENGER, "xxx.xxx.xxx.xxx"));
		state.set(State.userisconnected, true);
	}

	/** @return true if this user is allready connected from this address */ 
	public boolean isConnected(final String user, final String ip){
		for (int i = 0; i < list.size(); i++){
			Record rec = list.get(i);
			if (rec.isActive()){
				if(rec.getAddress().equals(ip))
					if(rec.getUser().equals(user))
						return true;
			}
		}
		
		return false;
	}
	
	public void signout() {
		
		if(state.getBoolean(State.developer)){
			System.out.println("_logging out: " + state.get(State.user));
			System.out.println("_waiting now:" + getPassengers());
			System.out.println(toString());
		}
		
		// try all instances
		// int active = 0;
		for (int i = 0; i < list.size(); i++){
			Record rec = list.get(i);
			if (rec.isActive()){
				if (rec.getUser().equals(state.get(State.user))){
					list.get(i).logout();
				}
			}
		}
		
		// assume this gets reset as new user logs in 
		state.set(State.userisconnected, false);
		state.delete(State.user);
		
		// maintain size limit 
		if(list.size() > MAX_RECORDS) list.remove(0);
		
		if(state.getBoolean(State.developer)){
			System.out.println("_logging out: " + state.get(State.user));
			System.out.println("_waiting now:" + getPassengers());
			System.out.println(toString());
		}
	
	}

	/** @return the number of users waiting in line */
	public int getPassengers() {
		int passengers = 0;
		for (int i = 0; i < list.size(); i++){
			Record rec = list.get(i);
			if(rec.isActive() && rec.isPassenger())
				passengers++;
		}

		return passengers;
	}
	
	/** @return a list of user names waiting in line */
	public String[] getPassengerList() {
		String[] passengers = new String[getPassengers()];
		for (int i = 0; i < list.size(); i++){
			Record rec = list.get(i);
			if(rec.isActive() && rec.isPassenger())
				passengers[i] = rec.getUser();
		}

		return passengers;
	}

	public int size() {
		return list.size();
	}

	/** create snap shot of current use to disk */
	public static boolean save(){
		
		if(new File(filename).exists()) 
			new File(filename).delete();
		
		FileWriter fw = null;
		try {
			fw = new FileWriter(new File(filename));
		} catch (IOException e1) {
			return false;
		}
	
		try {
			fw.append(new LoginRecords().toString());
		} catch (IOException e) {
			return false;
		}
		
		try {
			fw.close();
		} catch (IOException e) {
			return false;
		}
		
		return true;
	}

	@Override
	public String toString() {

		if (list.isEmpty())
			return null;

		String str = "current users:\r\n";
		for (int i = 0; i < list.size(); i++)
			str += i + " " + list.get(i).toString() + "\r\n";

		return str;
	}

	/**
	 * 
	 */
	private class Record {

		private long timein = System.currentTimeMillis();
		private long timeout = 0;
		private String user = null;
		private String role = null;
		private String ip = null;
		
		Record(String usr, String role, String addr) {
			this.user = usr;
			this.role = role;
			this.ip = addr;
			
			if(state.getBoolean(State.developer))
				System.out.println("ceated login: " + toString());
		}

		public String getUser() {
			return user;
		}

		public long inTime() {
			return timein;
		}

		public long outTime() {
			return timeout;
		}
		
		public String getAddress(){
			return ip;
		}
		
		public boolean isActive(){
			return (timeout==0);
		}
		
		public boolean isPassenger(){
			return (role.equals(PASSENGER));
		}
		
		@Override
		public String toString() {
			String str = user + " " + role.toUpperCase() + " address: " + ip + " login: " + new Date(timein).toString();
			if(isActive()) str += " is ACTIVE";
			else str += " logout: " + new Date(timeout).toString();
			
			return str;
		}

		public void logout() {
			if(timeout==0){
				timeout = System.currentTimeMillis();
				///System.out.println("logged out : " + toString());
			} // else System.out.println("__error: trying to logout twice");	
		}
	}
}