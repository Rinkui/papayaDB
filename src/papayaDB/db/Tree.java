package papayaDB.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Tree {
	static class Node {
		final Request request;
		List<Integer> answer; // Integer car on ne stockera que les ids des
								// objets
		HashMap<Request, Node> linkedRequests; // on fait une hashmap pour
												// trouver le request facilement

		public Node(Request request, List<Integer> answer) {
			this.request = request;
			this.answer = answer;
		}

		public void addLinkedRequest(Node node) {
			linkedRequests.put(node.request, node);
		}
	}

	private final Node head;

	public Tree() {
		this.head = new Node(null, null); // la tête n'aura que des fils
	}

	public void add(List<Request> requestList, List<Integer> answer) {

		// /!\ trouver un moyen de renvoyer le noeud d'ajout OU le noeud après
		// lequel il doit être ajouté

		// vérifier si la requete existe

		Collections.sort(requestList);
		Node isPresent = containsRequest(requestList, head);

		// si elle existe, addId

		if (isPresent != null) {
			isPresent.answer.addAll(answer);
			return;
		}

		// sinon ajouter la requete là ou elle doit aller

		// si la requete n'est pas composée de plusieurs partie alors on
		// l'ajoute à la racine

		// sinon on l'ajoute là ou elle doit aller
	}
	
	
	private void nouvellefonction(){
		
	}
	
	private Node addAtNode(List<Request> requestList, Node atThisNode){
		if( requestList.isEmpty())
			return atThisNode;
		
		Request currentReq = requestList.get(0);
		
		atThisNode.linkedRequests.put(currentReq, new Node(requestList.get(0), new ArrayList<Integer>())); // /!\ VALEUR A MODIFIER et à CALCULER
		requestList.remove(0);
		return addAtNode(requestList, atThisNode.linkedRequests.get(currentReq));
	}
	
	private Node containsRequest(List<Request> requestList, Node current) {
		if (current.linkedRequests.isEmpty() && requestList.isEmpty())
			return current;

		boolean isContained = false;
		Node exactNode = null;
		for (Request request : requestList) {
			if (current.linkedRequests.containsKey(request)) {
				isContained = true;
				requestList.remove(request);
				exactNode = containsRequest(requestList, current.linkedRequests.get(request));
			}
		}

		// la requete existe déjà
		if (exactNode != null)
			return exactNode;

		if (!isContained) {
			// alors on a fait toute la liste et aucune requete n'est contenu
			// dans les fils, alors on ajoute toutes les requetes ici
			// on récupère alors le dernier noeud et on le retourne
			
			addAtNode(requestList, current);
		}

		// si c'est contenu mais que le noeud exacte n'existe pas c'est qu'il y
		// a un probleme puisque si c'est contenu alors le noeud a été ajouté
		// avant -> exception ???

		return exactNode;
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
		return containsRec(id, head);
	}

	public void addId(Integer id) {
		// lorsque l'utilisateur ajoute un quelque chose dans la bdd on doit
		// vérifier toutes les requests pour voir si elles conviennent ?
	}

	public void remvoveId(Integer id) {
		// enlève un objet de l'arbre
	}
}
