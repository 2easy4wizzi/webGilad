package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
import constants.DBConstants;
import models.User;

/**
 * Servlet implementation class LoginServlet.
 * It deals with requests of login and registration
 * @author Gilad Eini
 * @author Ilana Veitzblit
 */

public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public LoginServlet() {
		super();
	}
	
	public int isOnDBReg(String column, String var, Connection conn) throws SQLException , NamingException
	{
			PreparedStatement ps;
			int retValue = 0;
			if(column.equals("Username"))
			{
				ps = conn.prepareStatement(DBConstants.SELECT_USER_BY_NAME_STMT);
			}
			else
			{
				ps = conn.prepareStatement(DBConstants.SELECT_USER_BY_NICKNAME_STMT);
			}
			ps.setString(1, var);
			ResultSet rs = (ResultSet) ps.executeQuery();
			if (!rs.next() ) {
			    retValue = 1;
			}
			rs.close();
			ps.close();
			return retValue;
		
	}
	


	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	}
	/**
	 * this segment get user details and see if the user can register under this details
	 * @param username the user name requested by the user
	 * @param password the password requested by the user
	 * @param nickname the nickname requested by the user
	 * @param description the description given by the user
	 * @param PhotoUrl the url photo given by the user
	 * @param username the user name given by user
	 * @param password the password given by user
	 * @return if user name and/or nickname already taken returns error / on failure return error string
	 * @throws SQLException
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException 
	{
		try
		{
			PrintWriter out = response.getWriter();
			Context context = new InitialContext();
			BasicDataSource ds = (BasicDataSource) context.lookup(DBConstants.DB_DATASOURCE);
			Connection conn = ds.getConnection();
			
			String uri = request.getRequestURI();
			uri = uri.substring(uri.indexOf("LoginServlet") + "LoginServlet".length() + 1);
			
			//Registration
			if(uri.equals("Register"))
			{
				String PhotoUrl = request.getParameter("photo");
				if(PhotoUrl == null || PhotoUrl=="" ){
					PhotoUrl = "https://en.opensuse.org/images/0/0b/Icon-user.png";
				}
				User user = new User(request.getParameter("username"),
						request.getParameter("password"),
						request.getParameter("nickname"),
						request.getParameter("description"),
						PhotoUrl,
						0,
						null);
				try
				{
					//returns error comments for user
					if(isOnDBReg("Username", user.getUserName(), conn)== 0)
					{
						if(isOnDBReg("Nickname", user.getNickname(), conn)== 0){
						out.println("4");
						}
						else 
						{
							out.println("2");
						}
					}
					else if(isOnDBReg("Nickname", user.getNickname(), conn)== 0) 
					{
						out.println("3");
					}
					else
					{	
						PreparedStatement ps = conn.prepareStatement(DBConstants.INSERT_USER_STMT);
						ps.setString(1, user.getUserName());
						ps.setString(2, user.getPassword());
						ps.setString(3, user.getNickname());
						ps.setString(4, user.getDescription());
						ps.setString(5, user.getPhotoUrl());
						ps.executeUpdate();
						
						conn.commit();
						ps.close();
						
						user.setUsername(null);
						user.setPassword(null);
						request.getSession().setAttribute("user", user);
						
					}
				}
				catch (SQLException | NamingException e) 
				{
					getServletContext().log("Error while closing connection", e);
					response.sendError(500);// internal server error
				}
				finally{
					conn.close();
					out.close();
				}
			}
			/*
			 * this segment get user name and password and checks if they exist in DB.
			 */
			
			//login
			else if(uri.equals("Login"))
			{
				try
				{
					PreparedStatement ps = conn.prepareStatement(DBConstants.SELECT_USER_BY_NAME_AND_PASSWORD_STMT);
					ps.setString(1, request.getParameter("username"));
					ps.setString(2, request.getParameter("password"));
					ResultSet rs = (ResultSet) ps.executeQuery();
					if (!rs.next()) 
					{
						out.print("details are incorrect");
					}
					else
					{
						User user = new User(null,null,rs.getString(3),rs.getString(4),rs.getString(5),rs.getDouble(6),null);
						request.getSession().setAttribute("user", user);
					}
					rs.close();
					ps.close();
					
					
				}
				catch (SQLException e) 
				{
					getServletContext().log("Error while closing connection", e);
					response.sendError(500);// internal server error
				}
				finally{
					out.close();
					conn.close();
				}
			}	
		}
		catch (SQLException | NamingException e) 
		{
			e.printStackTrace();
		}
		finally
		{
		}	
	}
}

