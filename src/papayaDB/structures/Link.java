package papayaDB.structures;

public class Link {
	final int mapIndex;
	final int tableIndex;
	final int size;
	Link previous;
	Link next;

	public Link(int mapIndex, int tableIndex, int size) {
		this.mapIndex = mapIndex;
		this.tableIndex = tableIndex;
		this.size = size;
	}
	
	public int getMapIndex() {
		return mapIndex;
	}
	
	public int getTableIndex() {
		return tableIndex;
	}
	
	public int getSize() {
		return size;
	}
	
	@Override
	public String toString() {
		return "map : " + mapIndex + " table : " + tableIndex + " size : " + size;
	}
}