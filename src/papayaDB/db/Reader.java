package papayaDB.db;

import java.nio.MappedByteBuffer;

import jdk.internal.reflect.ReflectionFactory.GetReflectionFactoryAction;
import papayaDB.structures.HoleLinkedList;
import papayaDB.structures.Tuple;

// gere les index
// deux fichiers par type d'objet:
// - un avec les index
// les indexs seront stockés en int donc un index = 4 octets
// * première ligne, les index des champs
// * le reste, chaque int est un index d'un objet
// - un avec toutes les données présentées comme suit :
// seul la taille en octet est stockée en int, le reste est stocké en
// char/string
// * première ligne : nom du type des objets qui suivront
// * seconde ligne : nombre de champs puis tous les champs avec la taille en
// octet devant chaque
// champs, un index par champs qui est au niveau du int
// * le reste des lignes : une ligne par objet avec seulement les valeurs,
// la taille en octet devant chaque valeur, un index par champs qui se
// trouve au niveau de l'int

public class Reader {
	private static final int MARGIN = 100; // la marge que l'on choisi de
	// laisser pour l'ajout d'objets

	private MappedByteBuffer map;

	// pour les deux tableaux on retient l'index de la taille du champs
	// à changer : on stocke le nom des champs
	private String[] fieldsNames = null; // indexs des champs
	private int[] objectsIndex = null; // indexs des objets (les indexs que l'on
										// trouve dans le fichier de int)
	private HoleLinkedList holeList;
	private String type = null;
	private int capacity;

	public Reader(MappedByteBuffer map) {
		this.map = map;
	}

	private void firstTypeReading() {
		char c;
		StringBuilder sb = new StringBuilder();
		map.position(0);
		while ((c = map.getChar()) != '\n') {
			sb.append(c);
		}
		type = sb.toString();
	}

	private void firstFieldsReading() {
		if (type == null)
			firstTypeReading();
		int nbFields = map.getInt();
		fieldsNames = new String[nbFields];
		int nbChar;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < nbFields; i++) {
			nbChar = map.getInt();
			for (int j = 0; j < nbChar; j++) {
				map.getChar();
			}
			fieldsNames[i] = sb.toString();
			sb.setLength(0);
		}
	}

	private void firstObjectsReading(int nbFields) {
		if (fieldsNames == null)
			firstFieldsReading();
		int nbObjects = map.getInt();
		objectsIndex = new int[nbObjects + MARGIN];
		int nbChar;
		for (int i = 0; i < nbObjects; i++) {
			objectsIndex[i] = map.position();
			nbChar = map.getInt();
			for (int j = 0; j < nbChar; j++) {
				map.getChar();
			}
		}
	}

	private int getFieldIndex(String fieldName) {
		for (int i = 0; i < fieldsNames.length; i++) {
			if (fieldName.equals(fieldsNames[i]))
				return i;
		}
		return -1;
	}

	private String readFieldValue(int objectIndex, int fieldIndex) {
		map.position(objectsIndex[objectIndex + fieldIndex]);
		int size = map.getInt();
		if (size == 0)
			return "";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			sb.append(map.getChar());
		}
		return sb.toString();
	}

	public String getFieldValue(int objectIndex, String fieldName) {
		if (objectsIndex == null)
			firstObjectsReading(fieldName.length());
		int fieldIndex;
		if ((fieldIndex = getFieldIndex(fieldName)) == -1)
			throw new IllegalArgumentException("Field name doesn't exist");
		return readFieldValue(objectIndex, fieldIndex);
	}
	
	// les index fonctionnent selon les bytes, donc un int = 4 et un char = 2
	private int getObjectsSize(int objectIndex){
		map.position(objectIndex);
		int size = map.getInt();
		int byteSize = 4, nbChar;
		for(int i = 0 ; i < size ; i ++){
			nbChar = map.getInt();
			byteSize += 4;
			for (int j = 0; j < nbChar; j++) {
				byteSize += 2;
			}
		}
		return byteSize;
	}
	
	// un champs est composé de sa longueur et de sa valeur
	private int getFieldSize(String field){
		return 4+2*field.length();
	}

	public void suppressObject(int objectIndex) {
		if(holeList == null){
			new HoleLinkedList(objectIndex, getObjectsSize(objectIndex));
			return;
		}
		holeList.addHole(objectIndex, getObjectsSize(objectIndex));
		if(objectIndex == capacity-fieldsNames.length)
			capacity --;
	}
	
	private int getNewIndex(String[] objects, int size){
		int firstIndex = holeList.removeHole(size);
		if( firstIndex != -1 )
			return firstIndex;
		return objectsIndex[capacity++];
	}
	
	private void fillObjectsIndex(String[] objects, int firstIndex){
		
	}
	
	public void addObject(String[] objects){
		int size = 4;
		StringBuilder sb = new StringBuilder(objects.length);
		String val;
		int length;
		for(int i = 0 ; i < objects.length ; i ++){
			val = objects[i]; 
			length = val.length();
			size += getFieldSize(objects[i]);
			sb.append(length).append(val);
		}
		int firstIndex = getNewIndex(objects, size);
		fillObjectsIndex(objects, firstIndex);
	}

	// pour récuperer une donnée de map : il faut placer le curseur au bon
	// endroit index avec map.position(index), puis faire un map.get(byte[],
	// offset, length) pour
	// récuperer tout le contenu, attention, le curseur sera à la fin

	// pour avoir une map :
	// FileInputStream in = new FileInputStream(file);
	// FileChannel fc = in.getChannel();
	// this.map = fc.map(MapMode.READ_WRITE, 0, fc.size());
	// in.close();
}
