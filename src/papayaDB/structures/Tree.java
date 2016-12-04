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
		HashMap<Tuple<String, String>, Node> linkedRequests; // on fait une hashmap pour
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
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(request).append("[");
			for(Integer i : answer)
				sb.append(i).append(",");
			sb.setLength(sb.length()-1);
			sb.append("]");
			return sb.toString();
		}
	}

	private final Node head;
	private final ReentrantLock lock = new ReentrantLock();

	public Tree() {
		this.head = new Node(null, null); // la tête n'aura que des fils
		this.head.linkedRequests = new HashMap<Tuple<String, String>, Node>();
	}

	public void add(List<Tuple<String, String>> requestList) {
		Objects.requireNonNull(requestList);
		lock.lock();
		try {
			Comparator<Tuple<String, String>> comp = new Comparator<Tuple<String, String>>() {
				@Override
				public int compare(Tuple<String, String> t1, Tuple<String, String> t2) {
					int reqRes = t1.getKey().compareTo(t2.getKey());
					if( reqRes == 0)
						return t1.getValue().compareTo(t2.getValue());
					return reqRes;
				}
			};
			Collections.sort(requestList, comp);
			Node isPresent = containsRequest(requestList, head);

			if (!requestList.isEmpty()) {
				addAtNode(requestList, isPresent);
			}
		} finally {
			lock.unlock();
		}
	}

	private Node addAtNode(List<Tuple<String, String>> requestList, Node atThisNode) {
		if (requestList.isEmpty())
			return atThisNode;

		Tuple<String, String> currentReq = requestList.get(0);

		// TODO calculer les valeurs à ajouter dans new ArrayList<Integer>()
		atThisNode.linkedRequests.put(currentReq, new Node(requestList.get(0), new ArrayList<Integer>()));

		requestList.remove(0);
		return addAtNode(requestList, atThisNode.linkedRequests.get(currentReq));
	}

	private Node containsRequest(List<Tuple<String, String>> requestList, Node current) {
		if (current.linkedRequests.isEmpty() && requestList.isEmpty())
			return current;

		Node resNode = null;
		for (Tuple<String, String> request : requestList) {
			if (current.linkedRequests.containsKey(request)) {
				requestList.remove(request);
				resNode = containsRequest(requestList, current.linkedRequests.get(request));
			}
		}

		if (resNode != null)
			return resNode;

		return current;
	}

	private boolean containsRec(Integer id, Node current) {
		if (current == null)
			return false;
		if (current.answer.contains(id))
			return true;
		for (Node node : current.linkedRequests.values()) {
			containsRec(id, node);
		}
		return false;
	}

	public boolean contains(Integer id) {
		lock.lock();
		try {
			return containsRec(id, head);
		} finally {
			lock.unlock();
		}
	}

	public void addId(Integer id) {
		// lorsque l'utilisateur ajoute un quelque chose dans la bdd on doit
		// vérifier toutes les requests pour voir si elles conviennent ?
	}

	public void remvoveId(Integer id) {
		// enlève un objet de l'arbre
	}
	
	private String treeToString(String father, Node currentNode){
		if( currentNode.linkedRequests == null )
			return "";
		StringBuilder sb = new StringBuilder(father).append(":").append(currentNode);
		for( Map.Entry<Tuple<String, String>, Node> e : currentNode.linkedRequests.entrySet() ){
			sb.append(treeToString(currentNode.request.toString(), e.getValue()));
		}
		return sb.toString();
	}
	
	public void writeTreeInFile(File file) throws IOException{
		String tree = treeToString("", head);
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		out.write(tree);
		out.close();
	}
	
	public static Tree readTreeInFile(File file) throws IOException{
		Tree tree = new Tree();
		FileInputStream fis = new FileInputStream(file);
		int c;
		String parent;
		
		while((char)( c = fis.read()) != ';'){}
		
		while((c = fis.read()) != -1 ){
			
			// faire le traitement
		}
		fis.close();
		return tree;
	}
}
