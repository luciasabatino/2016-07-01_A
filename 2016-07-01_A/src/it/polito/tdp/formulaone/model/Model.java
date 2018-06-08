package it.polito.tdp.formulaone.model;

import java.util.*;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.formulaone.db.FormulaOneDAO;

public class Model {
	private FormulaOneDAO dao;
	private SimpleDirectedWeightedGraph<Driver, DefaultWeightedEdge> grafo;
	private DriverIdMap driverIdMap;
	private List<Driver> bestDreamTeam;
	private int bestDreamTeamValue;
	
	public Model() {
		dao = new FormulaOneDAO();
		driverIdMap = new DriverIdMap();
	}
	
	public List<Season> getAllSeasons() {
		return dao.getAllSeasons();
	}

	public void creaGrafo(Season s) {
		grafo = new SimpleDirectedWeightedGraph<Driver, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		//I vertici sono tutti i piloti che hanno gareggiato in quella stagione
		List<Driver> drivers = dao.getAllDriversBySeason(s, driverIdMap);
		Graphs.addAllVertices(grafo, drivers);
		
		//Archi tra due piloti 
		//1) itero sulle due liste di piloti (cicli annidati controllando ogni coppia di piloti e associando il peso)
		//2) query al database che restituisce direttamente come sono fatti questi archi, con il peso corretto
		
		for(DriverSeasonResult dsr : dao.getDriverSeasonResult(s, driverIdMap)) {
			Graphs.addEdgeWithVertices(grafo, dsr.getD1(), dsr.getD2(), dsr.getCounter());
		}
		System.out.format("Grafo creato : %d archi, %d vertici\n", grafo.edgeSet().size(), grafo.vertexSet().size());
	}
	
	public Driver getBestDriver() {
		if(grafo == null) {
			new RuntimeException("Creare il grafo!");
		}
		Driver bestDriver = null;
		int best = Integer.MIN_VALUE;
		for(Driver d : grafo.vertexSet()) {
			int sum=0;
			//itero sugli archi uscenti
			for(DefaultWeightedEdge e : grafo.outgoingEdgesOf(d)) {
				sum+= grafo.getEdgeWeight(e);
			}
			//itero sugli archi entranti
			for(DefaultWeightedEdge e : grafo.incomingEdgesOf(d)) {
				sum-= grafo.getEdgeWeight(e);
			}
			if(sum>best || bestDriver==null) {
				best=sum; 
				bestDriver=d;
			}
		}
		if(bestDriver == null) {
			new RuntimeException("Best driver not found");
		}
		return bestDriver;
	}

	public List<Driver> getDreamTeam(int k){
		bestDreamTeam = new ArrayList<>();
		bestDreamTeamValue=Integer.MAX_VALUE;
		recursive(0, new ArrayList<Driver>(), k);
		return bestDreamTeam;
	}

	private void recursive(int step, ArrayList<Driver> parziale, int k) {
		//condizione di terminazione
		if(step >= k) {
			if(evaluate(parziale) < bestDreamTeamValue) {
				bestDreamTeamValue = evaluate(parziale);
				bestDreamTeam = new ArrayList<>(parziale);
			}
			return;
		}
		
		for(Driver d : grafo.vertexSet()) {
			if(!parziale.contains(d)) {
				parziale.add(d);
				recursive(step+1, parziale, k);
				parziale.remove(d);
			}
		}
		
	}

	private int evaluate(ArrayList<Driver> parziale) {
		int sum=0;
		Set<Driver> parzialeSet = new HashSet<>(parziale);
		for(DefaultWeightedEdge e : grafo.edgeSet()) {
			if(parzialeSet.contains(grafo.getEdgeTarget(e))) {
				//se parziale contiene il vertice di destinazione dell'arco e
				sum+=grafo.getEdgeWeight(e);
			}
		}
		return sum;
	}
}
