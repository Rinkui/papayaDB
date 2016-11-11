package papayaDB.db;

import java.util.HashMap;
import java.util.List;

public class Tree {
	static class Node {
		String request;
		List<Integer> answer; // Integer car on ne stockera que les ids des
								// objets
		HashMap<String, Node> linkedRequests; // string pour le nom de la
												// requete pour les retrouver
												// rapidement, Node pour le
												// noeud correspondant

		public Node(String request, List<Integer> answer) {
			this.request = request;
			this.answer = answer;
		}
	}

	private final Node head;

	public Tree() {
		this.head = new Node(null, null); // la tête n'aura que des fils
	}

	public void add(String request, List<Integer> answer) {
		// 1. Découper la requete
		// 2. vérifier si la requete existe
		// 3. si elle existe, addId
		// sinon ajouter la requete là ou elle doit aller

		// si la requete n'est pas composée de plusieurs partie alors on
		// l'ajoute à la racine
		// sinon on l'ajoute là ou elle doit aller
	}

	public void addId(Integer id) {
		// lorsque l'utilisateur ajoute un quelque chose dans la bdd on doit
		// vérifier toutes les requests pour voir si elles conviennent ?
	}
}
