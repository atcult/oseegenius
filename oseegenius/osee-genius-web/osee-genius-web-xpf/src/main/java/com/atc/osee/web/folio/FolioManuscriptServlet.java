package com.atc.osee.web.folio;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.json.JSONArray;
import org.json.JSONObject;

import com.atc.osee.web.log.Log;

public class FolioManuscriptServlet extends FolioUserServlet {

	private static final long serialVersionUID = 756035133644L;
	
	protected static DataSource datasource;
	protected BncfDao dao;
	
	/**
	 * Load into db manuscript signatures witha .csv input file
	 */
	
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		String filePath = request.getParameter("filePath");		
		response.setContentType("application/json");
		JSONObject jsonResult = new JSONObject();
		try {			
			BufferedReader fileReader = new BufferedReader(new FileReader(filePath));
			String row;
			String fondoCSV = null;
			String collocationCSV = null;
			while ((row = fileReader.readLine()) != null) {
				String [] currentRow = row.split(",");
				String currentFondo = currentRow[0];
				String currentCollocation = currentRow[1];
				String currentSpec = currentRow[2];
				String volumeLabel = null;
				if (currentRow.length > 3) {
					volumeLabel = currentRow[3];				
				}
				String volumeValues = null;
				if (currentRow.length > 4) {
					volumeValues = currentRow[4];
				}
				String newCollocation = null;
				if (currentRow.length > 5) {
					newCollocation = currentRow[5];
				}
				
				if (currentFondo != null && !"".equals(currentFondo)) {
					fondoCSV = currentFondo;
				}
				if (currentCollocation != null && !"".equals(currentCollocation)) {
					collocationCSV = currentCollocation;
				}
				boolean areVolumesPresent = volumeValues != null && !"".equals(volumeValues);
				List<String> specNameList = getSpecNameList(currentSpec);				
				for (String currentSpecName : specNameList) {
					int specId = dao.insertManuscript(fondoCSV, collocationCSV, currentSpecName, newCollocation, volumeLabel, areVolumesPresent);
				
					if (areVolumesPresent) {
						String [] tokens = volumeValues.trim().split(" ");
						String lowerLimit = tokens[1];
						String upperLimit = tokens[3];					
						try {
							int lowerLimitInt = Integer.parseInt(lowerLimit);
							int upperLimitInt = Integer.parseInt(upperLimit);
							for (int i = lowerLimitInt; i <= ((upperLimitInt + 1) - lowerLimitInt); i ++) {
								dao.insertVolumes(String.valueOf(i), specId);
							}
						} catch (NumberFormatException e) {
							List<String> volumeValueslist = getRomanListSubset(lowerLimit, upperLimit);
							for (String correntVolume : volumeValueslist) {
								dao.insertVolumes(correntVolume, specId);
							}
						}
					}	
				}
			}
			fileReader.close();
			
			response.getWriter().println(jsonResult.toString());
			
		} catch (Exception exception) {
			exception.printStackTrace();
			Log.error(exception.getMessage());	
			StringBuilder builderResponse = new StringBuilder("{");
			builderResponse.append("\"error\" : ").append("\"").append(exception.getMessage()).append("\"");
			builderResponse.append("}");			
			response.getWriter().println(builderResponse.toString());
		}	
		
	}
	
	/**
	 * Create json to populate cascade drowpdown for manuscript signatures 
	 */
	
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		String inputId = request.getParameter("inputId");
		String inputCollocationId = request.getParameter("collocationId");
		String inputSpecificationId = request.getParameter("specificationId");
		int inputType = Integer.parseInt(request.getParameter("inputType"));
		response.setContentType("application/json");		
		
		JSONObject jsonResult = new JSONObject();
		JSONArray array = new JSONArray();
		try {			
			switch (inputType) {
				case 1:
					Map<Integer,String> results = dao.getCollocationList(inputId);					
					
					for (int collocationId : results.keySet()) {
						JSONObject o = new JSONObject();
						o.put("label", results.get(collocationId));
						o.put("id", collocationId);
						array.put(o);
					}
					jsonResult.put("results", array);
					break;
				case 2:
					List<ManuscriptDataModel> specificationResults = dao.getSpecificationList(inputId);					
					for (ManuscriptDataModel currentSpecification : specificationResults) {
						JSONObject o = new JSONObject();
						o.put("label", currentSpecification.getLabel());
						o.put("id", currentSpecification.getId());
						o.put("new_collocation", currentSpecification.getNewCollocation());
						array.put(o);
					}
					jsonResult.put("results", array);
					break;				
				case 3:
					ManuscriptDataModel manuscript = dao.getNewCollocation(inputCollocationId, inputSpecificationId);					
					if (manuscript != null) {
						if (manuscript.getNewCollocation() != null) {					
							jsonResult.put("new_collocation", manuscript.getNewCollocation());	
						}
						if (manuscript.getVolumeType() != null) {
							jsonResult.put("volume_type", manuscript.getVolumeType());
						}
						if (manuscript.getVolumeLabel() != null) {
							jsonResult.put("volume_label", manuscript.getVolumeLabel());
						}							
					}
					break;
				case 4:
					List<String> volumeOption = dao.getVolumeList(inputSpecificationId);
					if (volumeOption != null && !volumeOption.isEmpty()) {
						for (String currentVolume : volumeOption) {
							JSONObject o = new JSONObject();
							o.put("label", currentVolume);							
							array.put(o);
						}
						jsonResult.put("results", array);
					}
					break;
				default: 
			}
			
			response.getWriter().println(jsonResult.toString());
			
		} catch (Exception exception) {
			Log.error(exception.getMessage());	
			StringBuilder builderResponse = new StringBuilder("{");
			builderResponse.append("\"error\" : ").append("\"").append(exception.getMessage()).append("\"");
			builderResponse.append("}");
			
			response.getWriter().println(builderResponse.toString());
		}		
	}

	@Override
	public void init() throws ServletException {
		super.init();
		try {
			final InitialContext cxt = new InitialContext();
			datasource = (DataSource) cxt.lookup("java:/comp/env/jdbc/pg");
			dao = new BncfDao(datasource);
		} catch (Exception ignore) {
			Log.error("", ignore);
		}
	}
	
	/**
	 * Give an ordered list of roman numbers in a range given in input
	 * @param lowerLimit
	 * @param upperLimit
	 * @return ordered list of subset roman numbers
	 */
	
	public  List<String> getRomanListSubset(final String lowerLimit, final String upperLimit) {
		List<String> result = new ArrayList<String>();
		if (lowerLimit != null && upperLimit != null) {
			Map<String, Integer> romanMap = romanNumberMap();
					
			Iterator<String> iterator = romanMap.keySet().iterator();			
			boolean findLower = false;			
			String currentKey;
			while(iterator.hasNext()) {
				currentKey = iterator.next();
				if (!findLower && currentKey.equals(lowerLimit)) {
					findLower = true;
				}
				if (findLower) {
					result.add(currentKey);
				}
				if (currentKey.equals(upperLimit)) {
					return result;
				}
			}			
		}
		return result;
	}
	
	/**
	 * roman numebers mapped to arabian numbers
	 * @return
	 */
	public  Map<String, Integer> romanNumberMap () {
		Map<String, Integer> map = new LinkedHashMap<String, Integer>();
		map.put("0", 0);
		map.put("I", 1);
		map.put("II", 2);
		map.put("III", 3);
		map.put("IV", 4);
		map.put("V", 5);
		map.put("VI", 6);
		map.put("VII", 7);
		map.put("VIII", 8);
		map.put("IX", 9);
		map.put("X", 10);
		map.put("XI", 11);
		map.put("XII", 12);
		map.put("XIII", 13);
		map.put("XIV", 14);
		map.put("XV", 15);
		map.put("XVI", 16);
		map.put("XVII", 17);
		map.put("XVIII", 18);
		map.put("XIX", 19);
		map.put("XX", 20);
		map.put("XXI", 21);
		map.put("XXII", 22);
		map.put("XXIII", 23);
		map.put("XXIV", 24);
		map.put("XXV", 25);
		map.put("XXVI", 26);
		map.put("XXVII", 27);
		map.put("XXVIII", 28);
		map.put("XXIX", 29);
		map.put("XXX", 30);
		map.put("XXXI", 31);
		map.put("XXXII", 32);
		map.put("XXXIII", 33);
		map.put("XXXIV", 34);
		map.put("XXXV", 35);
		map.put("XXXVI", 36);
		map.put("XXXVII", 37);
		map.put("XXXVIII", 38);
		map.put("XXXIX", 39);
		map.put("XL", 40);
		map.put("XLI", 41);
		map.put("XLII", 42);
		map.put("XLIII", 43);
		map.put("XLIV", 44);
		map.put("XLV", 45);
		map.put("XLVI", 46);
		map.put("XLVII", 47);
		map.put("XLVIII", 48);
		map.put("XLIX", 49);
		map.put("L", 50);
		map.put("LI", 51);
		map.put("LII", 52);
		map.put("LIII", 53);
		map.put("LIV", 54);
		map.put("LV", 55);
		map.put("LVI", 56);
		map.put("LVII", 57);
		map.put("LVIII", 58);
		map.put("LIX", 59);
		map.put("LX", 60);
		map.put("LXI", 61);
		map.put("LXII", 62);
		map.put("LXIII", 63);
		map.put("LXIV", 64);
		map.put("LXV", 65);
		map.put("LXVI", 66);
		map.put("LXVII", 67);
		map.put("LXVIII", 68);
		map.put("LXIX", 69);
		map.put("LXX", 70);
		map.put("LXXI", 71);
		map.put("LXXII", 72);
		map.put("LXXIII", 73);
		map.put("LXXIV", 74);
		map.put("LXXV", 75);
		map.put("LXXVI", 76);
		map.put("LXXVII", 77);
		map.put("LXXVIII", 78);
		map.put("LXXIX", 79);
		map.put("LXXX", 80);
		map.put("LXXXI", 81);
		return map;
	}
	
	public List<String> getSpecNameList(final String currentSpecName) {
		List<String> result = new ArrayList<String>();				
		String [] specNameList = currentSpecName.split("-");
		if (specNameList.length < 2) {
			result.add(currentSpecName);
		}
		else {			
			try {
				int lowerLimitInt = Integer.parseInt(specNameList[0]);
				int upperLimitInt = Integer.parseInt(specNameList[1]);
				for (int i = lowerLimitInt; i <= upperLimitInt; i ++) {
					result.add(String.valueOf(i));
				}
			} catch (NumberFormatException e) {
				Map<String, Integer> romanMap = romanNumberMap();
				if (romanMap.containsKey(specNameList[0]) && romanMap.containsKey(specNameList[1])) {
					List<String> romanSpecValueslist = getRomanListSubset(specNameList[0], specNameList[1]);
					for (String correntSpec : romanSpecValueslist) {
						result.add(correntSpec);
					}
				}
				else {
					result = new ArrayList<String>();
					result.add(currentSpecName);
					return result;
				}
			}			
		}		
		return result;
	}
}
