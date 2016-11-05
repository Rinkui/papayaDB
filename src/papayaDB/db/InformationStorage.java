package papayaDB.db;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

public class InformationStorage<T> {
	private T[] data;
	private int size;
	private int nbModification;
	private int nextPosition;
	
	@SuppressWarnings("unchecked")
	public InformationStorage(){
		this.data = (T[]) new Object[2];
	}
	
	public void readFile(Path file){
		//TODO a voir si on le place ici
	}
	
	public void add(T value){
		Objects.requireNonNull(value);
		if(size == data.length){
			increaseCapacity();
		}
		data[nextPosition] = value;
		setNextPosition();
		nbModification++;
		size++;
	}
	
	private void increaseCapacity() {
		data = Arrays.copyOf(data, data.length*2);
	}
	
	private void setNextPosition(){
		for (int i = nextPosition; i < data.length; i++) {
			if(data[i]==null){
				nextPosition = i;
				return;
			}
		}
		nextPosition = data.length;
	}

	public void remove(int index){
		data[index] = null;
		if(nextPosition > index){
			nextPosition = index;
		}
		nbModification++;
		size--;
	}
	
	public T get(int index){
		return data[index];
	}
}
