package com.atc.osee.web.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.Layout;
import com.atc.osee.web.model.CopyNote;

public class CopyNoteServlet extends OseeGeniusServlet {
	
	private String QUERY = "SELECT NTE_TPE,NTE_TXT FROM HLDG_NTE WHERE HLDG_NBR=? AND NTE_TPE IN ('1', '5') ORDER BY NTE_TPE, NTE_SEQ ASC";
	protected DataSource dataSource;
	
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
		String copy_id = request.getParameter("bib");
		List<CopyNote> copyNotes = new ArrayList<CopyNote>();
		try{
			lookup();
			copyNotes = getCopyNote(Integer.parseInt(copy_id));			
		} catch (NamingException e){	
			e.printStackTrace();
		}		
		request.setAttribute("copyNote", copyNotes);
		
		forwardTo(request, response, "copyNote.vm", Layout.DUMMY_LAYOUT);
		
	}
	public List<CopyNote> getCopyNote(final int copy_id) {
		Connection connection = null;
		
		PreparedStatement statement = null;
		PreparedStatement holdingNotesStatement = null;		
		ResultSet rs = null;
		
		List<CopyNote> resultList = new ArrayList<CopyNote>();
		try{
			connection = dataSource.getConnection();			
			statement = connection.prepareStatement(QUERY);
			statement.setInt(1, copy_id);
			rs = statement.executeQuery();
			while (rs.next()) {
				CopyNote copyNote  = new CopyNote();
				copyNote.setNote_text(rs.getString("NTE_TXT"));
				copyNote.setNote_type(rs.getInt("NTE_TPE"));				
				resultList.add(copyNote);		
			}
		}
		 catch (Exception exception) {	
			 exception.printStackTrace();
		} finally {
			try {
				rs.close(); 
			} catch (Exception exception2) {
				// N.A.
			}
			try {
				statement.close(); 
			} catch (Exception exception3) {
				// N.A.
			}
			try {
				connection.close(); 
			} catch (Exception exception4) {
				
			}
		}
		return resultList;
	}
	
	protected void lookup() throws NamingException	{
		InitialContext ctx = new InitialContext();
		dataSource = (DataSource) ctx.lookup("java:comp/env/jdbc/amicus");
	}
	
	
}