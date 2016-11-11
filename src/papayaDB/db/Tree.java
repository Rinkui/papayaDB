package papayaDB.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Tree {
	static class Node {
		Request request;
		List<Integer> answer; // Integer car on ne stockera que les ids des
								// objets
		HashMap<Request, Node> linkedRequests; // string pour le nom de la
												// requete pour les retrouver
												// rapidement, Node pour le
												// noeud correspondant

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

	public void add(LinkedList<Request> request, List<Integer> answer) {

		// /!\ trouver un moyen de renvoyer le noeud d'ajout OU le noeud après
		// lequel il doit être ajouté

		// vérifier si la requete existe

		Node isPresent = containsReq(request, head);

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

	private Node containsReq(LinkedList<Request> req, Node current) {
		// trouver toutes les req dans l'arbre. si elle y est en entière alors
		// on retourne le noeud
		return null;
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
