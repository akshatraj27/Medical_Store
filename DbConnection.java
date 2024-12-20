package com.medicos.web.Medicos_Medical.dbConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//CODE FOR CREATE THE CONNECTION USING SINGLTON
public class DbConnection 
{
	public static Connection con = null;
	public static Connection getConnection() throws ClassNotFoundException, SQLException
	{
		if (con == null || con.isClosed()) 
		{
		Class.forName("oracle.jdbc.driver.OracleDriver");
        con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "APNAMEDICOS", "123456");
//        System.out.println(con);
		}
        return con;
	}
}
