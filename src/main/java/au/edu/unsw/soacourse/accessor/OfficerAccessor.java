package au.edu.unsw.soacourse.accessor;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import au.edu.unsw.soacourse.container.Officer;
import au.edu.unsw.soacourse.database.DatabaseConnection;

public class OfficerAccessor implements AutoCloseable{
	Connection dbConnection;
	
	public OfficerAccessor(){
		dbConnection = DatabaseConnection.getConnection();
	}
	
	public Officer getOfficer(String username) throws SQLException {
		Officer o = null;
		int id;
		String password;
		
		String sqlQuery = "SELECT * FROM officer WHERE username LIKE ?";
		PreparedStatement statement = dbConnection.prepareStatement(sqlQuery);
		
		statement.setString(1, username);
		
		ResultSet result = statement.executeQuery();
		if (result.next()){
			id = result.getInt("id");
			password = result.getString("password");
			o = new Officer(id, username, password);
			statement.close();
			result.close();
			return o;
		}
		else {
			statement.close();
			result.close();
			return o;
		}	
	}
	
	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub
		dbConnection.close();
	}

}
