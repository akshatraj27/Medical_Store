package com.medicos.web.Medicos_Medical.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.medicos.web.Medicos_Medical.dbConnection.DbConnection;

public class LoginService 
{
	public static String isValidUser(String email, String pwd)
	{
		try(
				Connection con= DbConnection.getConnection();
				PreparedStatement stmt= con.prepareStatement("select role from login where email=? and password=?");
			)
		{
			stmt.setString(1, email);
			stmt.setString(2, pwd);
			ResultSet rs= stmt.executeQuery();
			if(rs.next())
				return rs.getString("role");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
			return "";
	}
}
