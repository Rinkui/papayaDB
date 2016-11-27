package papayaDB.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

import sun.java2d.ReentrantContext;

public class Tree {
	static class Node {
		final Request request;
		List<Integer> answer; // Integer car on ne stockera que l'index de début
								// de chaque objet
		HashMap<Request, Node> linkedRequests; // on fait une hashmap pour
												// trouver le request facilement

		public Node(Request request, List<Integer> answer) {
			this.request = request;
			this.answer = answer;
		}

		public void addLinkedRequest(Node node) {
			Objects.requireNonNull(node);
			linkedRequests.put(node.request, node);
		}
	}

	private final Node head;
	private final ReentrantLock lock = new ReentrantLock();

	public Tree() {
		this.head = new Node(null, null); // la tête n'aura que des fils
		this.head.linkedRequests = new HashMap<Request, Node>();
	}

	public void add(List<Request> requestList) {
		lock.lock();
		try {
			Objects.requireNonNull(requestList);
			Collections.sort(requestList);
			Node isPresent = containsRequest(requestList, head);

			if (!requestList.isEmpty()) {
				addAtNode(requestList, isPresent);
			}
		} finally {
			lock.unlock();
		}
	}

	private Node addAtNode(List<Request> requestList, Node atThisNode) {
		if (requestList.isEmpty())
			return atThisNode;

		Request currentReq = requestList.get(0);

		// TODO calculer les valeurs à ajouter dans new ArrayList<Integer>()
		atThisNode.linkedRequests.put(currentReq, new Node(requestList.get(0), new ArrayList<Integer>()));

		requestList.remove(0);
		return addAtNode(requestList, atThisNode.linkedRequests.get(currentReq));
	}

	private Node containsRequest(List<Request> requestList, Node current) {
		if (current.linkedRequests.isEmpty() && requestList.isEmpty())
			return current;

		Node resNode = null;
		for (Request request : requestList) {
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
}
