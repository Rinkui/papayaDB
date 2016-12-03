package papayaDB.structures;

public class DoubleLinkedList {
	private Link head;
	private static final int SIZE_DIFFERENCE = 5; // la difference de size
	// autorisée pour combler un
	// trou en octets
	private int size;

	public DoubleLinkedList(int firstIndex, int tableIndex, int size) {
		head = new Link(firstIndex, tableIndex, size);
	}

	public void addHole(int firstIndex, int tableIndex, int size) {
		Link newHead = new Link(firstIndex, tableIndex, size);
		newHead.next = head;
		head = newHead;
		size++;
	}

	// si la nouvelle taille est plus petite que la taille du trou alors on
	// rajoute un trou avec une taille qui fait la différence (on marquera les
	// trous au début du fichier pour ne pas lire les trous
	public Link removeHole(int size) {
		for (Link link = head; link != null; link = link.next) {
			if (link.size >= size - SIZE_DIFFERENCE && link.size <= size) {
				Link tmp = link;
				link.previous.next = link.next;
				if (size != link.size) {
					addHole(tmp.mapIndex + size,tmp.tableIndex ,link.size - size);
				} else
					size--;
				return tmp;
			}
		}
		return null;
	}

	public int getSize() {
		return size;
	}
}
