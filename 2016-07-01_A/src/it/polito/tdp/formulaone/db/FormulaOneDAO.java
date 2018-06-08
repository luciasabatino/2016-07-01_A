package it.polito.tdp.formulaone.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import it.polito.tdp.formulaone.model.Circuit;
import it.polito.tdp.formulaone.model.Constructor;
import it.polito.tdp.formulaone.model.Driver;
import it.polito.tdp.formulaone.model.DriverIdMap;
import it.polito.tdp.formulaone.model.DriverSeasonResult;
import it.polito.tdp.formulaone.model.Season;


public class FormulaOneDAO {

	public List<Integer> getAllYearsOfRace() {
		
		String sql = "SELECT year FROM races ORDER BY year" ;
		
		try {
			Connection conn = ConnectDB.getConnection() ;

			PreparedStatement st = conn.prepareStatement(sql) ;
			
			ResultSet rs = st.executeQuery() ;
			
			List<Integer> list = new ArrayList<>() ;
			while(rs.next()) {
				list.add(rs.getInt("year"));
			}
			
			conn.close();
			return list ;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("SQL Query Error");
		}
	}
	
	public List<Season> getAllSeasons() {
		
		String sql = "SELECT year, url FROM seasons ORDER BY year" ;
		
		try {
			Connection conn = ConnectDB.getConnection() ;

			PreparedStatement st = conn.prepareStatement(sql) ;
			
			ResultSet rs = st.executeQuery() ;
			
			List<Season> list = new ArrayList<>() ;
			while(rs.next()) {
				list.add(new Season(rs.getInt("year"), rs.getString("url"))) ;
			}
			
			conn.close();
			return list ;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null ;
		}
	}
	
	public List<Circuit> getAllCircuits() {

		String sql = "SELECT circuitId, name FROM circuits ORDER BY name";

		try {
			Connection conn = ConnectDB.getConnection();

			PreparedStatement st = conn.prepareStatement(sql);

			ResultSet rs = st.executeQuery();

			List<Circuit> list = new ArrayList<>();
			while (rs.next()) {
				list.add(new Circuit(rs.getInt("circuitId"), rs.getString("name")));
			}

			conn.close();
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("SQL Query Error");
		}
	}
	
	public List<Constructor> getAllConstructors() {

		String sql = "SELECT constructorId, name FROM constructors ORDER BY name";

		try {
			Connection conn = ConnectDB.getConnection();

			PreparedStatement st = conn.prepareStatement(sql);

			ResultSet rs = st.executeQuery();

			List<Constructor> constructors = new ArrayList<>();
			while (rs.next()) {
				constructors.add(new Constructor(rs.getInt("constructorId"), rs.getString("name")));
			}

			conn.close();
			return constructors;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("SQL Query Error");
		}
	}

	public List<Driver> getAllDriversBySeason(Season s, DriverIdMap driverIdMap) {

		String sql = "SELECT DISTINCT drivers.driverId, drivers.forename, drivers.surname "+
					"FROM drivers, results, races WHERE drivers.driverId=results.driverId "+
					"AND races.raceId=results.raceId AND races.year=? "+
					"AND results.position is not null ";

		try {
			Connection conn = ConnectDB.getConnection();

			PreparedStatement st = conn.prepareStatement(sql);
			st.setInt(1, s.getYear());

			ResultSet rs = st.executeQuery();

			List<Driver> list = new ArrayList<>();
			while (rs.next()) {
				Driver d = new Driver(rs.getInt("driverId"), rs.getString("forename"), rs.getString("surname")); 
				list.add(driverIdMap.get(d));
			}

			conn.close();
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("SQL Query Error");
		}
	}
	

	public List<DriverSeasonResult> getDriverSeasonResult(Season s, DriverIdMap driverIdMap) {

		String sql = "SELECT r1.driverId as d1, r2.driverId as d2, COUNT(*) as cnt "+
				"FROM results as r1, results as r2, races "+
				"WHERE r1.raceId=r2.raceId AND races.raceId=r1.raceId "+
				"AND races.year=? AND r1.position<r2.position "+ 
				"AND r1.position is not null "+
				"AND r2.position is not null "+
				"GROUP BY d1,d2 ";

		try {
			Connection conn = ConnectDB.getConnection();

			PreparedStatement st = conn.prepareStatement(sql);
			st.setInt(1, s.getYear());

			ResultSet rs = st.executeQuery();

			List<DriverSeasonResult> list = new ArrayList<>();
			while (rs.next()) {
				Driver d1 = driverIdMap.get(rs.getInt("d1"));
				Driver d2= driverIdMap.get(rs.getInt("d2"));
				if(d1==null || d2==null) {
					System.err.format("Skipping %d %d\n", rs.getInt("d1"), rs.getInt("d2"));
				}
				list.add(new DriverSeasonResult(d1, d2, rs.getInt("cnt")));
			}

			conn.close();
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("SQL Query Error");
		}
	}
}
