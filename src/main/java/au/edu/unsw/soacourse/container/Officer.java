package au.edu.unsw.soacourse.container;

public class Officer {
	private static final int MAX_STRING = 45;
	
	private int id;
	private String username;
	private String password;
	
	public Officer(int id, String username, String password) throws IllegalArgumentException {
		if (username.length() > MAX_STRING)
				throw new IllegalArgumentException("Username length too long. Must be less than " + MAX_STRING + " characters");
		if (password.length() > MAX_STRING)
			throw new IllegalArgumentException("Password length too long. Must be less than " + MAX_STRING + " characters");
		
		this.id = id;
		this.username = username;
		this.password = password;
	}

	public int getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public static int getMaxString() {
		return MAX_STRING;
	}

}
