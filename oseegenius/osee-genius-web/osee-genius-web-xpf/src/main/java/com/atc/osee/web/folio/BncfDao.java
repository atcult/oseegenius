package com.atc.osee.web.folio;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class BncfDao {
	
	private final static String INSERT_USER_BARCODE = "INSERT INTO user_ext (user_id, user_barcode, gdpr_consent_flag, gdpr_consent_ts, copyright_flag, copyright_ts) VALUES (?, generate_user_barcode(), true, current_timestamp, true, current_timestamp) RETURNING user_barcode";
	private final static String CHANGE_USER_CATEGORY = "UPDATE user_ext SET user_catg = ?, user_catg_expiry_ts = ? WHERE user_id = ?";
	private final static String SET_USER_CONSENT = "UPDATE user_ext SET gdpr_consent_flag = true, gdpr_consent_ts = current_timestamp, copyright_flag = true, copyright_ts = current_timestamp WHERE user_id = ?";
	private final static String SELECT_USER_DATE_EXPIRE = "SELECT user_catg_expiry_ts from user_ext WHERE user_id = ?";
	private final static String SELECT_USER = "SELECT user_id from user_ext WHERE user_id = ?";
	private final static String INSERT_OLD_USER = "INSERT INTO user_ext (user_id, user_barcode) VALUES (?, ?)";
	private final static String SELECT_USER_OLD_CAT = "SELECT b.prev_catg FROM bncf.user_ext as a JOIN bncf.user_catg_hstry as b on a.user_id = b.user_id  WHERE user_id = ? AND a.user_catg_expiry_ts = b.prev_catg_expiry_ts";
	private final static String INSERT_CATEGORY_HISTORY = "INSERT INTO user_catg_hstry (user_id, prev_catg, next_catg, prev_catg_expiry_ts, trstn_ts) VALUES (?, ?, ?, ?, current_timestamp)";
	private final static String SEARCH_USER_TO_UNBLOCK = "SELECT DISTINCT ON (a.user_id) a.user_id, b.prev_catg FROM bncf.user_ext as a " + 
			"	JOIN bncf.user_catg_hstry as b on a.user_id = b.user_id " + 
			"	WHERE a.user_catg_expiry_ts = b.prev_catg_expiry_ts AND a.user_catg_expiry_ts <= ?  " + 
			"	order by a.user_id, b.trstn_ts desc";
	private final static String RESET_USER_CATEGORY = "UPDATE user_ext SET user_catg = NULL, user_catg_expiry_ts = NULL WHERE user_id =?";
	private final static String SELECT_CITY_LIST = "SELECT city_id, city_name FROM bncf.city ORDER BY city_name";
	private final static String SELECT_MUNICIPALITY_BY_CITY_ID = "SELECT city_id, municipality_id FROM bncf.municipality WHERE city_id = ? order by municipality_id";
	private final static String SELECT_FONDO_LIST = "SELECT fondo_id, fondo_name FROM bncf.manuscript_fondo ORDER BY fondo_name";
	private final static String SELECT_COLLOCATION_LIST = "SELECT collocation_id, collocation_name FROM bncf.manuscript_collocation WHERE fondo_id = ? ORDER BY collocation_name";
	private final static String SELECT_SPECIFICATION_LIST = "SELECT spec_id, spec_name, NULLIF(regexp_replace(spec_name, '\\D','','g'), '')::numeric AS spec_number, NULLIF(regexp_replace(spec_name, '\\d','','g'), '') AS spec_name_text FROM bncf.manuscript_spec WHERE collocation_id = ? ORDER By spec_number, spec_name_text";
	private final static String SELECT_NEW_COLLOCATION = "SELECT new_collocation, volume_label, volume_type FROM bncf.manuscript_spec WHERE collocation_id = ? AND spec_id = ? ORDER By spec_name";
	private final static String CREATE_BARCODE = "{ ? = call generateCopyBarcode( ? ) }";
	
	private final static String INSERT_RESEARCH_HISTORY = "INSERT INTO research_history (search_data , terms, search_index, results, order_by, filters, searchUri, section, usr_id) VALUES ( current_timestamp, ?, ?, ? , ?, ?,?,?,?)";
	private final static String SELECT_RESEARCH_HISTORY = "SELECT *  FROM research_history WHERE section= ? AND usr_id = ? ORDER BY search_data DESC LIMIT ? OFFSET ?  ";
	private final static String NUMBER_RESEARCH_HISTORY = "SELECT COUNT(*) AS cnt  FROM research_history WHERE section= ? AND usr_id = ?";
	private final static String DELETE_ELEMENT_RESEARCH_HISTORY = "DELETE FROM research_history WHERE id_search= ?";
	
	protected DataSource datasource;
		
	public BncfDao (DataSource datasource) {
		this.datasource = datasource;
	}
	

	/**
	 * set user consent to gdpr and copyright
	 * @param userId
	 * @throws SQLException
	 */
	
	public void setUserConsent (final String userId) throws SQLException {
		Connection connection = null;
		PreparedStatement statementChangeConsent = null;
		try {
			connection = datasource.getConnection();	
			statementChangeConsent = connection.prepareStatement(SET_USER_CONSENT);
			statementChangeConsent.setObject(1, UUID.fromString(userId));
			statementChangeConsent.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw exception;
		} catch (Exception exception) {
			throw new SQLException(exception);
		} finally {
			try { 
				statementChangeConsent.close(); 
			} catch (Exception exception) {}
			try { connection.close(); } catch (Exception exception) {}
		}		
	}
	
	/**
	 * change user category to lower patron group
	 * @param newUserGroup 
	 * @param oldUserGroup
	 * @param userId
	 * @param expiringDate
	 * @throws SQLException
	 */
	public void blockUserLoan (final String newUserGroup,
								final String oldUserGroup,
								final String userId,
								final Timestamp expiringDate) throws SQLException{
		Connection connection = null;
		PreparedStatement statementChangeCategory = null;
		PreparedStatement statementHistoryCategory = null;
		
		try {
			connection = datasource.getConnection();
			connection.setAutoCommit(false);
			
			statementChangeCategory = connection.prepareStatement(CHANGE_USER_CATEGORY);
			statementChangeCategory.setString(1, newUserGroup);
			statementChangeCategory.setTimestamp(2, expiringDate);
			statementChangeCategory.setObject(3, UUID.fromString(userId));
			statementChangeCategory.executeUpdate();
			
			statementHistoryCategory = 	connection.prepareStatement(INSERT_CATEGORY_HISTORY);	
			statementHistoryCategory.setObject(1, UUID.fromString(userId));
			statementHistoryCategory.setString(2, oldUserGroup);
			statementHistoryCategory.setString(3, newUserGroup);
			statementHistoryCategory.setTimestamp(4, expiringDate);
			statementHistoryCategory.executeUpdate();
			
			connection.commit();
			
		} catch (SQLException exception) {
			exception.printStackTrace();
			connection.rollback();
			throw exception;
		} catch (Exception exception) {
			throw new SQLException(exception);
		} finally {
			try { 
				statementChangeCategory.close(); 
				statementHistoryCategory.close();
			} catch (Exception exception) {}
			try { connection.close(); } catch (Exception exception) {}
		}	
	}
	/**
	 * get user patron group code to reset
	 * @param userId
	 * @return old user patron group code 
	 * @throws SQLException
	 */
	
	public String getUserCategoryToReset(final String userId) throws SQLException {
		Connection connection = null;
		PreparedStatement statementSelectDate = null;
		ResultSet rs = null;
		String result = null;
		try {
			connection = datasource.getConnection();	
			statementSelectDate = connection.prepareStatement(SELECT_USER_OLD_CAT);
			statementSelectDate.setObject(1, UUID.fromString(userId));
			rs = statementSelectDate.executeQuery();			
			while (rs.next()) {
				if (rs.getObject("prev_catg") != null) {
					result = rs.getObject("prev_catg").toString();
				}
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
			connection.rollback();
			throw exception;
		} catch (Exception exception) {
			throw new SQLException(exception);
		} finally {
			try { 
				statementSelectDate.close(); 
			} catch (Exception exception) {}
			try { connection.close(); } catch (Exception exception) {}
		}	
		return result;
	}
	
	/**
	 * check if user is present on the database
	 * @param userId
	 * @return
	 * @throws SQLException
	 */
	
	public boolean userExist (final String userId) throws SQLException {
		Connection connection = null;
		PreparedStatement statementSelect = null;
		ResultSet rs = null;
		boolean result = false;
		try {
			connection = datasource.getConnection();	
			statementSelect = connection.prepareStatement(SELECT_USER);		
			statementSelect.setObject(1, UUID.fromString(userId));
			rs = statementSelect.executeQuery();
			while (rs.next()) {
				result = true;
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
			connection.rollback();
			throw exception;
		} catch (Exception exception) {
			throw new SQLException(exception);
		} finally {
			try { 
				statementSelect.close(); 
			} catch (Exception exception) {}
			try { connection.close(); } catch (Exception exception) {}
		}	
		return result;
	}
	
	
	
	/**
	 * get user block dateof exipration
	 * @param userId
	 * @return date 
	 * @throws SQLException
	 */
	public String getUserBlockExpireDate (final String userId) throws SQLException {
		Connection connection = null;
		PreparedStatement statementSelectDate = null;
		ResultSet rs = null;
		String result = null;
		try {
			connection = datasource.getConnection();	
			statementSelectDate = connection.prepareStatement(SELECT_USER_DATE_EXPIRE);			
			statementSelectDate.setObject(1, UUID.fromString(userId));
			rs = statementSelectDate.executeQuery();			
			while (rs.next()) {
				if (rs.getObject("user_catg_expiry_ts") != null) {
					result = rs.getObject("user_catg_expiry_ts").toString();
				}
			}			
		} catch (SQLException exception) {
			exception.printStackTrace();
			connection.rollback();
			throw exception;
		} catch (Exception exception) {
			throw new SQLException(exception);
		} finally {
			try { 
				statementSelectDate.close(); 
			} catch (Exception exception) {}
			try { connection.close(); } catch (Exception exception) {}
		}	
		return result;
	}
	
	
	/**
	 * reset user category and expiry date	 
	 * @param userId
	 * @param expiringDate
	 * @throws SQLException
	 */
	public void unblockUserLoan (final String userId) throws SQLException{
		Connection connection = null;
		PreparedStatement statementChangeCategory = null;
		
		try {
			connection = datasource.getConnection();
			connection.setAutoCommit(false);
			
			statementChangeCategory = connection.prepareStatement(RESET_USER_CATEGORY);			
			statementChangeCategory.setObject(1, UUID.fromString(userId));
			statementChangeCategory.executeUpdate();
			connection.commit();
			
		} catch (SQLException exception) {
			exception.printStackTrace();
			connection.rollback();
			throw exception;
		} catch (Exception exception) {
			throw new SQLException(exception);
		} finally {
			try { 
				statementChangeCategory.close(); 
			} catch (Exception exception) {}
			try { connection.close(); } catch (Exception exception) {}
		}	
	}
	
	/**
	 * insert a user with given userID (from Folio) and barcode
	 * @param userId
	 * @param userBarcode
	 * @throws SQLException
	 */
	
	public void inserOldUser (final String userId, final String userBarcode) throws SQLException {
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = datasource.getConnection();
			statement = connection.prepareStatement(INSERT_OLD_USER);
			statement.setObject(1, UUID.fromString(userId));
			statement.setString(2, userBarcode);
			statement.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();			
			throw exception;
		} catch (Exception exception) {
			throw new SQLException(exception);
		} finally {
			try { 
				statement.close(); 
				statement.close();
			} catch (Exception exception) {}
			try { connection.close(); } catch (Exception exception) {}
		}	
	}
	
	/**
	 * Insert a new user into the database and create a new barcode 
	 * @param folioId
	 * @return
	 * @throws SQLException
	 */
	
	public String insertBarcode(final String folioId) throws SQLException{
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		String barcode = null;
		try {
			connection = datasource.getConnection();
			statement = connection.prepareStatement(INSERT_USER_BARCODE);
			statement.setObject(1, UUID.fromString(folioId));
			rs = statement.executeQuery();
			while (rs.next()) {
				barcode = rs.getObject("user_barcode").toString();
			}
		} catch (SQLException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new SQLException(exception);
		} finally {
			try { statement.close(); } catch (Exception exception) {}
			try { connection.close(); } catch (Exception exception) {}
		}	
		return barcode;
	}
	
	
	
	/**
	 * Insert a new research into the database 
	 * 
	 * @throws SQLException
	 */

	public void insertSearch(String userId,String query, String queryType, long nrecord, String order, String url, String filteres, String section) throws SQLException{
		Connection connection = null;
		PreparedStatement statement = null;
		
		try {
			connection = datasource.getConnection();
			statement = connection.prepareStatement(INSERT_RESEARCH_HISTORY);
			statement.setObject(1, query);
			statement.setObject(2, queryType);
			statement.setObject(3, nrecord);
			statement.setObject(4, order);
			statement.setObject(5, filteres);
			statement.setObject(6, url);
			statement.setObject(7, section);
			statement.setObject(8, UUID.fromString(userId));
			
			statement.executeUpdate();
			
		} catch (Exception exception) {
			//non fare nulla : eccezione che viene scaturita quando in user_ext manca l'id dell'user che fa la ricerca
		} finally {
			try { statement.close(); } catch (Exception exception) {}
			try { connection.close(); } catch (Exception exception) {}
		}	
		
	}
	
	
	
	
	
	
	/**
	 * return history of research for logged user
	 * @param userId, type of research, limit and offser
	 * @return List of BncfElementResearchHistory that contains the elemet reserched from users
	 * @throws SQLException

	 */
	
	public List <BncfElemtResearchHistory> ResearchHistory (final String userId, final String section, final String offset, final String limit) throws SQLException {
		Connection connection = null;
		PreparedStatement statementSelect = null;
		ResultSet rs = null;
		List <BncfElemtResearchHistory>  result = new ArrayList <BncfElemtResearchHistory>();
		try {
			connection = datasource.getConnection();	
			statementSelect = connection.prepareStatement(SELECT_RESEARCH_HISTORY);		
			statementSelect.setObject(1, section);
			statementSelect.setObject(2, UUID.fromString(userId));
			int l=Integer.parseInt(limit);
			int o=Integer.parseInt(offset);
			statementSelect.setInt(3, l);
			statementSelect.setInt(4, o);
			rs = statementSelect.executeQuery();
			while (rs.next()) {
				int id_search= rs.getInt("id_search");
				String query= rs.getString("terms");
				String queryType=rs.getString("search_index");
				int howManyResults= rs.getInt("results");
				String orderBy=rs.getString("order_by");
				String searchUri=rs.getString("searchUri");
				String data = rs.getObject("search_data").toString();
				String filters=rs.getString("filters");
				
				String [] filtersList=null;
				if (filters!=null)
					filtersList=filters.split(", ");
					
				BncfElemtResearchHistory el= new BncfElemtResearchHistory(
						id_search,
						userId,
						query,
						queryType,
						howManyResults,
						orderBy,
						searchUri,
						filtersList,
						data,
						section
						);
				result.add(el);
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
			connection.rollback();
			throw exception;
		} catch (Exception exception) {
			throw new SQLException(exception);
		} finally {
			try { 
				statementSelect.close(); 
			} catch (Exception exception) {}
			try { connection.close(); } catch (Exception exception) {}
		}	
		return result;
	}
	
	
	
	
	/**
	 * 
	 * @return number of searches for the given type
	 * @param UserId and type of research
	 * @throws SQLException
	 */
	public int howManyResults (final String userId, final String section) throws SQLException {
		Connection connection = null;
		PreparedStatement statementSelect = null;
		ResultSet rs = null;
		int result=0;
		
		try {
			connection = datasource.getConnection();	
			statementSelect = connection.prepareStatement(NUMBER_RESEARCH_HISTORY);		
			statementSelect.setObject(1, section);
			statementSelect.setObject(2, UUID.fromString(userId));
			
			rs = statementSelect.executeQuery();
			while (rs.next()) {
				result=rs.getInt("cnt");
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
			connection.rollback();
			throw exception;
		} catch (Exception exception) {
			throw new SQLException(exception);
		} finally {
			try { 
				statementSelect.close(); 
			} catch (Exception exception) {}
			try { connection.close(); } catch (Exception exception) {}
		}	
		return result;
	}
	
	/**
	 * delete record in a table of research history
	 * 
	 * @param UserId
	 * @throws SQLException
	 */
	public void deleteElementResearchHistory(final int id) throws SQLException {
		Connection connection = null;
		PreparedStatement statementSelect = null;
		ResultSet rs = null;
		try {
			connection = datasource.getConnection();	
			statementSelect = connection.prepareStatement(DELETE_ELEMENT_RESEARCH_HISTORY);		
			statementSelect.setInt(1, id);
			statementSelect.executeUpdate();
			
		} catch (SQLException exception) {
			exception.printStackTrace();
			connection.rollback();
			throw exception;
		} catch (Exception exception) {
			throw new SQLException(exception);
		} finally {
			try { 
				statementSelect.close(); 
			} catch (Exception exception) {}
			try { connection.close(); } catch (Exception exception) {}
		}	
	
	}
	
	
	/**
	 * 
	 * @param today timestamp
	 * @return a list of user to unblock today
	 * @throws SQLException
	 */
	
	public List<FolioUserModel> getUserToUnblock(final Timestamp today) throws SQLException {
		List<FolioUserModel> results = new ArrayList<FolioUserModel>();
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			connection = datasource.getConnection();
			statement = connection.prepareStatement(SEARCH_USER_TO_UNBLOCK);
			statement.setTimestamp(1, today);
			rs = statement.executeQuery();
			while (rs.next()) {
				FolioUserModel user = new FolioUserModel();
				user.setId(rs.getObject("user_id").toString());
				user.setPatronGroupCode(rs.getString("prev_catg"));
				results.add(user);
			}
		} catch (SQLException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new SQLException(exception);
		} finally {
			try { statement.close(); } catch (Exception exception) {}
			try { connection.close(); } catch (Exception exception) {}
			
		}
		return results;
	}
	
	/**
	 * Gets list of city (province)
	 * 
	 * @return a map contains cities.
	 * @throws SQLException -- in case of SQL Exception.
	 */
	public Map<String, String> getCityList() throws SQLException {
		
		Map<String, String> results = new LinkedHashMap<String, String>();
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			connection = datasource.getConnection();
			statement = connection.prepareStatement(SELECT_CITY_LIST);			
			rs = statement.executeQuery();
			while (rs.next()) {
				results.put(rs.getString("city_id"), rs.getString("city_name"));				
			}
		} catch (SQLException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new SQLException(exception);
		} finally {
			try { statement.close(); } catch (Exception exception) {}
			try { connection.close(); } catch (Exception exception) {}
			
		}
		return results;
	}
	
	public String getBarcode(final String prefix) throws SQLException {		
		Connection connection = null;
		CallableStatement  statement = null;
		ResultSet rs = null;
		try {
			connection = datasource.getConnection();
			statement = connection.prepareCall(CREATE_BARCODE);
			statement.registerOutParameter(1, Types.VARCHAR);
			statement.setString(2, prefix);
			statement.execute();
			return statement.getString(1);
		} catch (SQLException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new SQLException(exception);
		} finally {
			try { statement.close(); } catch (Exception exception) {}
			try { connection.close(); } catch (Exception exception) {}
			
		}			
	}
	
	/**
	 * Gets a list of municipality by city id.
	 * 
	 * @param cityId -- the city id
	 * @return a map of municipality values.
	 * @throws SQLException -- in case of SQL Exception.
	 */
	public List<String> getMunicipalityByCityId(final String cityId) throws SQLException {
		List<String> results = new ArrayList<String>();
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			connection = datasource.getConnection();
			statement = connection.prepareStatement(SELECT_MUNICIPALITY_BY_CITY_ID);	
			statement.setString(1, cityId);			
			rs = statement.executeQuery();
			while (rs.next()) {
				results.add(rs.getString("municipality_id"));				
			}
		} catch (SQLException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new SQLException(exception);
		} finally {
			try { statement.close(); } catch (Exception exception) {}
			try { connection.close(); } catch (Exception exception) {}
			
		}
		return results;
	}
	
	/**
	 * Gets list of fondi 
	 * 
	 * @return a map contains fondi.
	 * @throws SQLException -- in case of SQL Exception.
	 */
	public Map<Integer, String> getFondoList() throws SQLException {
		
		Map<Integer, String> results = new LinkedHashMap<Integer, String>();
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			connection = datasource.getConnection();
			statement = connection.prepareStatement(SELECT_FONDO_LIST);			
			rs = statement.executeQuery();
			while (rs.next()) {
				results.put(rs.getInt("fondo_id"), rs.getString("fondo_name"));				
			}
		} catch (SQLException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new SQLException(exception);
		} finally {
			try { statement.close(); } catch (Exception exception) {}
			try { connection.close(); } catch (Exception exception) {}			
		}
		return results;
	}
	
	public Map<Integer, String> getCollocationList(final String inputId) throws SQLException {
		int id = Integer.parseInt(inputId);
		Map<Integer, String> results = new LinkedHashMap<Integer, String>();
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			connection = datasource.getConnection();
			statement = connection.prepareStatement(SELECT_COLLOCATION_LIST);			
			statement.setInt(1, id);
			rs = statement.executeQuery();
			while (rs.next()) {
				results.put(rs.getInt("collocation_id"), rs.getString("collocation_name"));				
			}
		} catch (SQLException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new SQLException(exception);
		} finally {
			try { statement.close(); } catch (Exception exception) {}
			try { connection.close(); } catch (Exception exception) {}			
		}
		return results;
	}
	
	public List<ManuscriptDataModel> getSpecificationList(final String inputId) throws SQLException {
		int id = Integer.parseInt(inputId);
		List<ManuscriptDataModel> results = new ArrayList<ManuscriptDataModel>();
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			connection = datasource.getConnection();
			statement = connection.prepareStatement(SELECT_SPECIFICATION_LIST);			
			statement.setInt(1, id);
			rs = statement.executeQuery();
			while (rs.next()) {
				ManuscriptDataModel data = new ManuscriptDataModel();
				data.setId(rs.getInt("spec_id"));
				data.setLabel(rs.getString("spec_name"));
				results.add(data);
			}
		} catch (SQLException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new SQLException(exception);
		} finally {
			try { statement.close(); } catch (Exception exception) {}
			try { connection.close(); } catch (Exception exception) {}			
		}
		return results;
	}
	
	public List<String> getVolumeList(final String specificationId) throws SQLException {
		List<String> result = new ArrayList<String>();
		int specificationIdInteger = Integer.parseInt(specificationId);
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			connection = datasource.getConnection();
			statement = connection.prepareStatement("SELECT volume_name FROM bncf.manuscript_volume WHERE spec_id = ?");
			statement.setInt(1, specificationIdInteger);
			rs = statement.executeQuery();
			while (rs.next()) {
				result.add(rs.getString("volume_name"));
			}
			return result;
		} catch (SQLException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new SQLException(exception);
		} finally {
			try { statement.close(); } catch (Exception exception) {}
			try { connection.close(); } catch (Exception exception) {}			
		}
	}
	
	public ManuscriptDataModel getNewCollocation(final String collocationId, final String specificationId) throws SQLException {
		ManuscriptDataModel result = null;
		int collocationIdInteger = Integer.parseInt(collocationId);	
		int specificationIdInteger = Integer.parseInt(specificationId);	
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			connection = datasource.getConnection();
			statement = connection.prepareStatement(SELECT_NEW_COLLOCATION);			
			statement.setInt(1, collocationIdInteger);
			statement.setInt(2, specificationIdInteger);
			rs = statement.executeQuery();
			while (rs.next()) {		
				result = new ManuscriptDataModel();
				result.setNewCollocation(rs.getString("new_collocation"));
				result.setVolumeLabel(rs.getString("volume_label"));
				result.setVolumeType(rs.getString("volume_type"));
			}
			return result;
		} catch (SQLException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new SQLException(exception);
		} finally {
			try { statement.close(); } catch (Exception exception) {}
			try { connection.close(); } catch (Exception exception) {}			
		}
	}
	
	public void insertVolumes(final String name, final int spec) throws SQLException {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		String barcode = null;
		try {
			connection = datasource.getConnection();
			statement = connection.prepareStatement("INSERT INTO bncf.manuscript_volume (volume_name, spec_id) " + 
					"VALUES " + 
					"   (?, ?)");
			statement.setString(1, name);
			statement.setInt(2, spec);
			statement.executeUpdate();
			
		} catch (SQLException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new SQLException(exception);
		} finally {
			try { statement.close(); } catch (Exception exception) {}
			try { connection.close(); } catch (Exception exception) {}
		}	
		return;
	}
	
	public int insertManuscript(final String fondo, final String collocation, final String name, final String newCollocation, final String volumeLabel, boolean areVolumePresent) throws SQLException{
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		int specId = 0;
		try {
			connection = datasource.getConnection();
			statement = connection.prepareStatement(buildManuscriptQuery(fondo, collocation, name, newCollocation, volumeLabel, areVolumePresent));
			rs = statement.executeQuery();
			while (rs.next()) {
				specId = rs.getInt("spec_id");
			}
		} catch (SQLException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new SQLException(exception);
		} finally {
			try { statement.close(); } catch (Exception exception) {}
			try { connection.close(); } catch (Exception exception) {}
		}	
		return specId;
	}
	
	private String buildManuscriptQuery(final String fondo, final String collocation, final String name, final String newCollocation, final String volumeLabel, boolean areVolumePresent) {
		StringBuilder result = new StringBuilder();
		boolean isNewCollocationPresent = (newCollocation != null && !"".equals(newCollocation)) ? true : false;
		boolean isVolumeLabelPresent = (volumeLabel != null && !"".equals(volumeLabel)) ? true : false;		
		result.append("INSERT INTO bncf.manuscript_spec ( collocation_id, fondo_id, spec_name");
		if (isNewCollocationPresent) {
			result.append(", new_collocation");
		}
		if (isVolumeLabelPresent) {
			result.append(", volume_label");
			result.append(", volume_type");
		}
		result.append(")");
		result.append(" VALUES ");
		result.append(" ( " );
		result.append(collocation);
		result.append(", " + fondo);
		result.append(", '" + name + "'");
		if (isNewCollocationPresent) {
			result.append(", '" + newCollocation + "'");
		}
		if (isVolumeLabelPresent) {
			result.append(", '" + formatVolumeLabel(volumeLabel) + "'");
			if (areVolumePresent) {
				result.append(", 'select'");
			}
			else {
				result.append(" , 'input'");
			}
		}		
		result.append(")");
		result.append(" RETURNING spec_id");
		return result.toString();	
	}
	
	private String formatVolumeLabel (final String volumeLabel) {
		String result = volumeLabel.replace(".", "_").replace(" ", "").trim();
		return result;
	}
	
}
