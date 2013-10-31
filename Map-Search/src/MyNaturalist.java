import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;


public class MyNaturalist extends Naturalist{
	// index for the found Nodes
	public int currentIndex = 0; 
	
	// a list of all the nodes found where each index within the list is same as the node's index
	public ArrayList<Node> Nodes = new ArrayList<Node>(); 
	
	// index of the nodes that contain animals
	public ArrayList<Integer> indicesAnimals = new ArrayList<Integer>(); 
	
	// a neighbor map where the key is node index and the returned value is the node's neighbors
	public HashMap<Integer, HashSet<Integer>> neighborMap = new HashMap<Integer, HashSet<Integer>>(); 
	
	// weight Matrix
	// cell[i][j] equals:
	// 0, if i = j
	// 1, if i and j are neighbors of each other
	// Integer.MAX_VALUE/2 - 1
	int[][] weightMatrix; 
	
	// distance Matrix
	// cell[i][j] equals the shortest distance between node i and node j
	int[][] distanceMatrix; 
	
	// intermediate values for path
	// cell[i][j] represents the intermediate node in the shortest path from node i to j
	Integer[][] pathMatrix; 
	
	@Override
	public void run() {
		// obtain the location of the current node (you start at the ship)
		// and perform dfs on the ship node to obtain adjacency list of the graph
		Node start = getLocation();
		dfs(start);
		
		
		
		// perform floyd-warhshall algorithm
		// so that you know the shortest path for each pair
		weightMatrix = wMatrix(neighborMap);
		floydW(weightMatrix);
		
		System.out.println(indicesAnimals);
		// collect animals and drop them off at the ship
		mission();
		
	}
	
	/** collect all the animals and drop them off at the ship
	 * 
	 * While animals remain on the map:
	 * (1) find the index of the farthest animal and collect it
	 * (2) While the inventory/Max Capacity is not full:
	 * 		(1) find the index of the next closest animal
	 * 		(2) repeat the process of going to the next closest animal and collecting 
	 * 			until the capacity is full or the ship is closer than the animal
	 * (3) Drop the animals in the ship
	 * 
	 * */
	public void mission(){
		while (!indicesAnimals.isEmpty()){
			// find the farthest node with an animal and go to that node.
			int indexFurthestAnimal = farthestAnimal();
			moveFromTo(0, indexFurthestAnimal);
			
			// collect as many animals as possible from that node
			collectAnimals();
			
			// while the inventory is not full and there are more animals to pick up, 
			// go to the closest node with an animal and collect it
			// or if the ship is closer to the current node, go to the ship and drop the animals.
			while (getInventory().size() < MAX_ANIMAL_CAPACITY && !indicesAnimals.isEmpty() && closerToAnimal()){
				int indexClosestAnimal = closestAnimal();
				moveFromTo(currentIndex(), indexClosestAnimal);
				collectAnimals();
			}
			
			// go back to the ship and drop all your animals
			moveFromTo(currentIndex(), 0);

			dropAll();
		}
		
	}
	
	/** return true if the the current location is closer to an animal than the ship
	 * false otherwise */
	public boolean closerToAnimal(){
		// index of the current node
		int currIndex = currentIndex();
		
		// index of the closest animal
		int indexClosestAnimal = closestAnimal();
		
		// distance to the closest Animal
		int distanceClosestAnimal = distanceMatrix[currIndex][indexClosestAnimal];
		
		// distance to the ship
		int distanceShip = distanceMatrix[currIndex][0];
		

		return distanceClosestAnimal < distanceShip;
	}
	
	
	/** collect as many animals as possible on the current node */
	public void collectAnimals(){
		// obtain a collection of all the animals present on the current node
		Collection<String> animalPresent = listAnimalsPresent(); 
				
		Data data = getLocation().getUserData();

		for (String animal: animalPresent){
			// if there's a space to add more animals,
			// (1) collect the animal
			// (2) decrease the number of animals at the node
			if (getInventory().size() < MAX_ANIMAL_CAPACITY ){
				collect(animal);
				data.animalHere--;
			}
		}

		// if no animal present here anymore, remove the node from the indicesAnimal
		if (data.animalHere == 0){
			indicesAnimals.remove((Integer)data.index);
		}
	}
	
	
	/** get the index of the node of the current location*/
	public int currentIndex(){
		Node currentLoc = getLocation();
		
		Data data = currentLoc.getUserData();
		int indexCurrent = data.index;
		return indexCurrent;
	}
	
	/** find the index of the closest node that contains an animal from the current location */
	public int closestAnimal(){
		// obtain the current index
		int indexCurrent = currentIndex();
		
		// assume that the very first animal in the animalindices is the closest animal
		int indexClosestAnimal = 0; 
		
		// compare its distance to the rest of the animals and update it
		for (int k=1; k<indicesAnimals.size(); k++){
			if(distanceMatrix[indexCurrent][indicesAnimals.get(indexClosestAnimal)] > distanceMatrix[indexCurrent][indicesAnimals.get(k)]){
				indexClosestAnimal = k;
			}
		}
		
		return indicesAnimals.get(indexClosestAnimal);
	}
	
	/** find the index of the farthest node that contains an animal from the ship*/
	public int farthestAnimal(){
		// assume that the very first animal in the animalindices is the farthest animal
		int indexFurthestAnimal = 0;
		
		// compare its distance to the rest of the animals and update it
		for (int k=1; k<indicesAnimals.size(); k++){
			if(distanceMatrix[0][indicesAnimals.get(indexFurthestAnimal)] < distanceMatrix[0][indicesAnimals.get(k)]){
				indexFurthestAnimal = k;
			}
		}
		
		return indicesAnimals.get(indexFurthestAnimal);
	}
	
	
	/** move the Naturalist from i to j using the shortestPath*/
	public void moveFromTo(int i, int j){
		// all the intermediates in the path from i to j
		ArrayList<Integer> pathList = path(i,j);
		
		// move from i to the last intermediate in the path
		for(Integer k: pathList){
			moveTo(Nodes.get(k));
		}
		
		// move to the j from the last intermediate
		moveTo(Nodes.get(j));
	}
	
	/** returns an arrayList of intermediates in the shortest path from i to j*/
	public ArrayList<Integer> path(int i, int j){
		Integer k = pathMatrix[i][j];
		
		// if the intermediate is null, i and j are connected.
		if (k == null){
			return new ArrayList<Integer>();
		}
		
		// Else,
		// obtain the intermediates from i to k
		// add k 
		// add the intermediates from k to j
		ArrayList<Integer> intermediates = path(i, k);
		intermediates.add(k);
		intermediates.addAll(path(k, j));
		
		return intermediates;
	}
	
	/** returns a copy of a 2-dimensional array*/
	public int[][] copy(int[][] a){
		int[][] b = new int[a.length][a.length];
		for (int i = 0; i < a.length; i++){
			for (int j = 0; j < a.length; j++){
				b[i][j] = a[i][j];
			}
		}
		return b;
	}
	
	/** Perform Floyd-Warshall Algorithm:
	 * (1) obtain the shortest distance matrix between all pairs
	 * (2) obtain the path matrix
	 * */
	public void floydW(int[][] wMatrix){
		// create a copy of weight matrix
		int[][] distance = copy(wMatrix);
		pathMatrix = new Integer[currentIndex][currentIndex];
		
		// initialize all the values to null for path matrix
		for (int a = 0; a < currentIndex; a++){
			for (int b = 0; b < currentIndex; b++){
				pathMatrix[a][b] = null;
			}
		}
		
		// By incrementing the number of intermediates you can use in the path,
		// (1) find the shortest distance between i and j
		// (2) update the path Matrix accordingly
		for (int k = 0; k < currentIndex; k++){
			for (int i = 0; i < currentIndex; i++){
				for (int j = 0; j < currentIndex; j++){
					if (distance[i][k] + distance[k][j] < distance[i][j]){
						distance[i][j] = distance[i][k] + distance[k][j];
						pathMatrix[i][j] = k;
					}
				}
			}
		}
		
		// return the shortest distance matrix
		distanceMatrix = distance;
	}
	
	/** Given a neighbor dictionary, return an equivalent weight matrix
	 * 
	 * 
	 * cell[i][j] equals
	 * (1) 1, if node i and j are connected.
	 * (2) 0, if i = j
	 * (3) Integer.MAX_VALUE/2 -1, if not connected
	 * 
	 * */
	public int[][] wMatrix(HashMap<Integer, HashSet<Integer>> adjList){
		// create a 2-dimensional matrix whether number of rows and columns equals the number of nodes on the map
		int [][] weighMatrix = new int[currentIndex][currentIndex];
		
		// set all the values to Integer.MAX_VALUE/2 - 1
		for (int i = 0; i < weighMatrix.length; i++){
			for (int j = 0; j < weighMatrix.length; j++){
				weighMatrix[i][j] = Integer.MAX_VALUE/2 - 1; 
			}
		}
		
		// (1) if node i and j are connected, change the cell value to 1
		// (2) if node i = node j, change the cell value to 0
		for (int i = 0; i < currentIndex; i++){
			HashSet<Integer> set = adjList.get(i);
			weighMatrix[i][i] = 0;
			for (Integer j: set){
				weighMatrix[i][j] = 1;
				weighMatrix[j][i] = 1;
			}
		}
		
		return weighMatrix;
	}
	
	
	/** Perform DFS recursively in order to explores all the nodes on the graph
	 *  I. INFORMATION REGARDING THE CURRENT NODE
	 *  Given Node n,
	 *  (1) save how many animals there are on node n
	 *  (2) give the node an index
	 *  
	 *  II. RECURSIVE CALLS ON ITS NEIGHBORS
	 *  And recursively call the function on its neighbors
	 * 	For each neighbor,
	 *  (1) move to its neighbor node
	 *  (2) perform DFS
	 *  (3) come back to the original node n. 
	 *  
	 *  III. UPDATE THE NEIGHBOR DICTIONARY
	 *  (1) create a set of its neighbors
	 *  (2) save the set in the neighborMap with its node index as the key
	 *  
	 */
	public void dfs(Node n){
		 // I. INFORMATION REGARDING THE CURRENT NODE
		// create an instance of Data that contains information regarding
		// (1) number of animals presents on n
		// (2) the index of the node
		Data data = new Data();
		data.animalHere = listAnimalsPresent().size();
		data.index = currentIndex++;
		
		n.setUserData(data);

		// insert the current node into the Nodes (its node index = index within the arrayList for Nodes)
		Nodes.add(data.index, n);
		
		// If there's an animal on the current node, add its index to the indicesAnimals
		if (data.animalHere > 0) {
			indicesAnimals.add(data.index);
		}
		
		
		// II. RECURSIVE CALLS ON ITS NEIGHBORS
		// for each neighbor node that's not traveled yet,
		// (1) move to that neighbor node
		// (2) perform dfs
		// (3) come back to the original node
		Node[] nearby = getExits();
		for (Node neighbor: nearby){
			Data neighborData = neighbor.getUserData();
			
			// whether or not the node has been traveled is determined by
			// whether or not the user data is null
			if (neighborData == null){
				moveTo(neighbor);
				dfs(neighbor);
				moveTo(n);
			}
		}
		
		
		// III. UPDATE THE NEIGHBOR DICTIONARY
		// (1) create a set of node n's neighbors
		HashSet<Integer> neighbors = new HashSet<Integer>();
		for (Node neighbor : nearby) {
			Data neighborData = neighbor.getUserData();
			neighbors.add(neighborData.index);
		}
				

		// (2) add the set of its neighbors into the neighborMap with its node index as the key
		neighborMap.put(data.index, neighbors);
	}
	

}

/** The data that's saved in each node
 * it contains 
 * (1) the index of the node
 * (2) whether the node has be found previously or not
 * (3) the number of animals present on the node
 */
class Data {
	public int index; // index of the node
	public boolean found; // whether or not the node is found or not
	public int animalHere; // the number of animals present in the node
	

	
	/** Constructor: uninitialized Data*/
	public Data(){}
	
	/** Constructor: an instance of Data
	 * 
	 * Given 
	 * (1) the index of the node,
	 * (2) the number of animals on the node,
	 * 
	 * set the fields accordingly
	 */
	public Data(int index, int animalHere){
		this.index = index;
		this.animalHere = animalHere;
	}
	
	
	
}