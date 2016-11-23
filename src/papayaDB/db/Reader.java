package papayaDB.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

public class Reader {
	private MappedByteBuffer map;
	private int firstFieldPos;
	private int nbObjectsPos;

	public Reader(MappedByteBuffer map){
		this.map = map;
	}

	// on doit lire une fois en entier la première fois pour retenir les index

	// on remet la position à 0 au cas ou quelqu'un d'intelligent essaye de
	// faire plusieurs fois cette fonction
	public String firstTypeReading() {
		char c;
		StringBuilder sb = new StringBuilder();
		map.position(0);
		while ((c = map.getChar()) != '\n') {
			sb.append(c);
		}
		firstFieldPos = map.position();
		return sb.toString();
	}
	
	private int[] loopReading(int[] index){
		int nbChar;
		for (int i = 0; i < index.length; i++) {
			index[i] = map.position();
			nbChar = map.getInt();
			for (int j = 0; j < nbChar; j++) {
				map.getChar();
			}
		}
		return index;
	}

	// a nouveau on replace le curseur au bon endroit
	public int[] firstFieldsReading() {
		if (firstFieldPos == 0)
			throw new IllegalStateException("Type non initialisé");
		map.position(firstFieldPos);
		int nbFields = map.getInt();
		int[] fieldsIndex = new int[nbFields];
		fieldsIndex = loopReading(fieldsIndex);
		nbObjectsPos = map.position();
		return fieldsIndex;
	}

	public int[] firstObjectsReading(int nbFields) {
		if (nbObjectsPos == 0)
			throw new IllegalStateException("Champs non initialisés");
		map.position(nbObjectsPos);
		int nbObjects = map.getInt();
		int[] objectsIndex = new int[nbObjects];
		return loopReading(objectsIndex);
	}
	
	public String getFieldValue(int firstIndex){
		StringBuilder sb = new StringBuilder();
		map.position(firstIndex);
		int size = map.getInt();
		for(int i = 0 ; i < size ; i ++ ){
			sb.append(map.getChar());
		}
		return sb.toString();
	}

	// pour récuperer une donnée de map : il faut placer le curseur au bon
	// endroit index avec map.position(index), puis faire un map.get(byte[],
	// offset, length) pour
	// récuperer tout le contenu, attention, le curseur sera à la fin
}
