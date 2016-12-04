package papayaDB.structures;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public class Tree {
	static class Node {
		final Tuple<String, String> request;
		List<Integer> answer; // Integer car on ne stockera que l'index de début
								// de chaque objet
		HashMap<Tuple<String, String>, Node> linkedRequests; // on fait une
																// hashmap pour
		// trouver le request facilement

		public Node(Tuple<String, String> request, List<Integer> answer) {
			this.request = request;
			this.answer = answer;
		}

		public void addLinkedRequest(Node node) {
			Objects.requireNonNull(node);
			linkedRequests.put(node.request, node);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((answer == null) ? 0 : answer.hashCode());
			result = prime * result + ((linkedRequests == null) ? 0 : linkedRequests.hashCode());
			result = prime * result + ((request == null) ? 0 : request.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Node other = (Node) obj;
			if (answer == null) {
				if (other.answer != null)
					return false;
			} else if (!answer.equals(other.answer))
				return false;
			if (linkedRequests == null) {
				if (other.linkedRequests != null)
					return false;
			} else if (!linkedRequests.equals(other.linkedRequests))
				return false;
			if (request == null) {
				if (other.request != null)
					return false;
			} else if (!request.equals(other.request))
				return false;
			return true;
		}
	}

	private final Node head;
	private final ReentrantLock lock = new ReentrantLock();

	public Tree() {
		this.head = new Node(null, null); // la tête n'aura que des fils
		this.head.linkedRequests = new HashMap<Tuple<String, String>, Node>();
	}

	public List<Integer> get(List<Tuple<String, String>> requestList) {
		lock.lock();
		try {
			Node node = containsRequest(requestList, head);
			if (node == head)
				return null;
			return node.answer;
		} finally {
			lock.unlock();
		}
	}

	private Node containsRequest(List<Tuple<String, String>> requestList, Node current) {
		if (requestList.isEmpty())
			return current;

		List<Tuple<String, String>> copy = requestList;
		for (Tuple<String, String> request : copy) {
			if (current.linkedRequests.containsKey(request)) {
				requestList.remove(request);
				return containsRequest(requestList, current.linkedRequests.get(request));
			}
		}

		return current;
	}
	
	private void addRec(List<Tuple<Tuple<String,String>, List<Integer>>> requestAndValues, int index, Node current){
		if( index >= requestAndValues.size() )
			return;
		
		Tuple<Tuple<String,String>, List<Integer>> thisRAV = requestAndValues.get(index);
		
		if( current.linkedRequests.containsKey(thisRAV.getKey()) ){
			addRec(requestAndValues, index+1, current.linkedRequests.get(thisRAV));
			return;
		}
		
		current.linkedRequests.put(thisRAV.getKey(), new Node(thisRAV.getKey(), thisRAV.getValue()));
		addRec(requestAndValues, index+1, current.linkedRequests.get(thisRAV));
	}

	public void add(List<Tuple<Tuple<String,String>, List<Integer>>> requestAndValues) {
		addRec(requestAndValues, 0, head);
	}

	public void addId(Integer id) {
		// lorsque l'utilisateur ajoute un quelque chose dans la bdd on doit
		// vérifier toutes les requests pour voir si elles conviennent ?
	}

	public void remvoveId(Integer id) {
		// enlève un objet de l'arbre
	}

	private String treeToString(String father, Node currentNode) {
		if (currentNode.linkedRequests == null)
			return "";
		StringBuilder sb = new StringBuilder(father).append(":").append(currentNode);
		for (Map.Entry<Tuple<String, String>, Node> e : currentNode.linkedRequests.entrySet()) {
			sb.append(treeToString(currentNode.request.toString(), e.getValue()));
		}
		return sb.toString();
	}

	public void writeTreeInFile(File file) throws IOException {
		String tree = treeToString("", head);
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		out.write(tree);
		out.close();
	}

	public static Tree readTreeInFile(File file) throws IOException {
		Tree tree = new Tree();
		FileInputStream fis = new FileInputStream(file);
		int c;
		String parent;

		while ((char) (c = fis.read()) != ';') {
		}

		while ((c = fis.read()) != -1) {

			// faire le traitement
		}
		fis.close();
		return tree;
	}
}
