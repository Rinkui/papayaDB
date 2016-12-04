package papayaDB.structures;

import java.util.HashMap;
import java.util.Map.Entry;

public class HoleList {
	private static final int SIZE_DIFFERENCE = 5; // la difference de size
	// autorisée pour combler un
	// trou en octets
	// index du tableau : [indexMap : size]
	private HashMap<Integer, Tuple<Integer, Integer>> holeMap;
	
	public HoleList(){
		holeMap = new HashMap<>();
	}

	public void addHole(int tableIndex, int mapIndex, int size) {
		holeMap.put(tableIndex, new Tuple<>(mapIndex, size));
	}

	// si la nouvelle taille est plus petite que la taille du trou alors on
	// rajoute un trou avec une taille qui fait la différence (on marquera les
	// trous au début du fichier pour ne pas lire les trous
	public Link removeHole(int size) {

		Entry<Integer, Tuple<Integer, Integer>> nearestEntry = null;

		for (Entry<Integer, Tuple<Integer, Integer>> entry : holeMap.entrySet()) {
			if ((entry.getValue().getValue() > size
					&& entry.getValue().getValue() - size < nearestEntry.getValue().getKey()) || nearestEntry == null) {
				nearestEntry = entry;
			}
		}

		if (size >= nearestEntry.getValue().getKey() - SIZE_DIFFERENCE && size <= nearestEntry.getValue().getKey())
			return new Link(nearestEntry.getValue().getKey(), nearestEntry.getKey(),
					nearestEntry.getValue().getValue());

		return null;
	}
	
	public boolean containsKey(int tableIntex){
		return holeMap.containsKey(tableIntex);
	}
	
	public boolean isEmpty(){
		return holeMap.isEmpty();
	}

	@Override
	public String toString() {
		return holeMap.toString();
	}
}
