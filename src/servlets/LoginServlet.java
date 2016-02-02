package servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

import constants.DBConstants;


public class LoginServlet extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
    public LoginServlet()
    {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		// TODO Auto-generated method stub	
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		System.out.println("inside LoginServlet doPost");
		try
		{
			Context context = new InitialContext();
			BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
			Connection conn = ds.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(DBConstants.INSERT_USER_STMT);
			
			pstmt.setString(1,request.getParameter("username"));
			pstmt.setString(2,request.getParameter("password"));
			pstmt.setString(2,request.getParameter("nickName"));
			pstmt.setString(2,request.getParameter("description"));
			pstmt.setString(2,request.getParameter("photo"));
			pstmt.executeUpdate();
			
			conn.commit();
			pstmt.close();
			conn.close();
		}
		catch (SQLException | NamingException e) 
		{
			getServletContext().log("Error while closing connection", e);
			response.sendError(500);//internal server error
		}
		response.sendRedirect("index.html");
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}
}
