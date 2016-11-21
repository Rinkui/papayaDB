package papayaDB.db;

import java.util.Objects;

public class Optimisateur {
	private Node root;
	
	
	
	
	class Node{
		final String filterName;
		final String filterValue;
		Node[] link;
		
		Node(String name, String value){
			Objects.requireNonNull(name);
			Objects.requireNonNull(value);
			this.filterName = name;
			this.filterValue = value;
		}
		
//		Node(){
//			
//		}
		
		@Override
		public int hashCode() {
			return filterName.hashCode()^filterValue.hashCode();
		}
		
		public boolean equals(Object obj){
			if(obj == null){
				return false;
			}
			if(this == obj){
				return true;
			}
			if(!(obj instanceof Node)){
				return false;
			}
			Node n = (Node)obj;
			return filterName.equals(n.filterName) && filterValue.equals(n.filterValue);
		}
		
	}
}
