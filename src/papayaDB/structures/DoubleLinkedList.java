package papayaDB.structures;

public class DoubleLinkedList {
	private class Link{
		final int firstIndex;
		final int size;
		Link previous;
		Link next;
		
		Link(int firstIndex, int size) {
			this.firstIndex = firstIndex;
			this.size = size;
		}
	}
	
	private Link head;
	private static final int SIZE_DIFFERENCE = 5; // la difference de size
	// autorisée pour combler un
	// trou
	private int size;
	
	public DoubleLinkedList(int firstIndex, int size) {
		head = new Link(firstIndex, size);
	}	
	
	public void addHole(int firstIndex, int size){
		Link newHead = new Link(firstIndex, size);
		newHead.next = head;
		head = newHead;
		size ++;
	}
	
	public int removeHole(int size){
		for(Link link = head; link != null ; link = link.next){
			if(link.size >= size-SIZE_DIFFERENCE && link.size <= size){
				Link tmp = link;
				link.previous.next = link.next;
				size --;
				return tmp.firstIndex;
			}
		}
		return -1;
	}
	
	public int getSize() {
		return size;
	}
}
