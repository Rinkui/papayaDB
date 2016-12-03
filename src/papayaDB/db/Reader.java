package papayaDB.db;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import papayaDB.structures.DoubleLinkedList;
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
	private final RandomAccessFile file;

	// pour les deux tableaux on retient l'index de la taille du champs
	// à changer : on stocke le nom des champs
	private String[] fieldsNames = null; // indexs des champs
	private int[] objectsIndex = null; // indexs des objets (les indexs que l'on
										// trouve dans le fichier de int)
	private DoubleLinkedList holeList;
	private ArrayList<Tuple<Integer, String[]>> addList; // string a écrire dans
	// le fichier
	private String type = null;
	private int capacity;

	public Reader(MappedByteBuffer map, RandomAccessFile file) {
		this.map = map;
		this.file = file;
	}

	private void firstTypeReading() {
		StringBuilder sb = new StringBuilder();
		map.position(0);
		int typeSize = map.getInt();
		for (int i = 0; i < typeSize; i++) {
			sb.append(map.getChar());
		}
		type = sb.toString();
		System.out.println(type);
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
		capacity = nbObjects * nbFields;
	}

	private int getFieldIndex(String fieldName) {
		if (fieldsNames == null)
			firstFieldsReading();
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
	private int getObjectsSize(int objectIndex) {
		map.position(objectIndex);
		int size = map.getInt();
		int byteSize = 4, nbChar;
		for (int i = 0; i < size; i++) {
			nbChar = map.getInt();
			byteSize += 4;
			for (int j = 0; j < nbChar; j++) {
				byteSize += 2;
			}
		}
		return byteSize;
	}

	// un champs est composé de sa longueur et de sa valeur
	private int getFieldSize(String field) {
		return 4 + 2 * field.length();
	}

	public void suppressObject(int objectIndex) {
		if (holeList == null) {
			new DoubleLinkedList(objectIndex, getObjectsSize(objectIndex));
			return;
		}
		holeList.addHole(objectIndex, getObjectsSize(objectIndex));
		if (objectIndex == capacity - fieldsNames.length)
			capacity--;
	}

	private int getNewIndex(String[] objects, int size) {
		int firstIndex = -1;
		if( holeList != null)
			firstIndex = holeList.removeHole(size);
		if (firstIndex != -1)
			return firstIndex;
		firstIndex = capacity + 1;
		capacity = capacity + objects.length;
		return firstIndex;
	}

	private void fillObjectsIndex(int[] valuesSize, int firstIndex) {
		for (int i = firstIndex, j = 0; i < firstIndex + valuesSize.length - 1; i++, j++) {
			// on additionne l'index précédent avec la taille du champs suivant
			objectsIndex[i + 1] = objectsIndex[i] + valuesSize[j];
		}
	}

	private void addToAddList(int size, String[] object) {
		if (addList == null)
			addList = new ArrayList<Tuple<Integer, String[]>>();
		addList.add(new Tuple<Integer, String[]>(size, object));
	}

	public int addObject(String[] object) {
		int size = 4; // premier int donne le nombre de champs
		int valuesSize[] = new int[object.length];
		int fieldSize;
		for (int i = 0; i < object.length; i++) {
			fieldSize = getFieldSize(object[i]);
			// on met directement les octets avec le int et la String
			valuesSize[i] = fieldSize;
			size += fieldSize;
		}
		int firstIndex = getNewIndex(object, size);
		addToAddList(firstIndex, object);
		fillObjectsIndex(valuesSize, firstIndex);
		return firstIndex;
	}

	private void resizeMap() throws IOException {
		int newlenght = map.limit() * 2;
		file.setLength(newlenght);
		map = file.getChannel().map(MapMode.READ_WRITE, 0, newlenght);
	}

	public void writeAddedObjects() throws IOException {
		for (Tuple<Integer, String[]> tuple : addList) {
			int pos = tuple.getKey();
			if (pos > map.limit())
				resizeMap();
			map.position(pos);
			String[] haveToWrite = tuple.getValue();
			map.putInt(haveToWrite.length);
			for (String s : haveToWrite) {
				map.putInt(s.length());
				for (int i = 0; i < s.length(); i++) {
					map.putChar(s.charAt(i));
				}
			}
		}
	}

	public String getType() {
		if (type == null)
			firstTypeReading();
		return type;
	}

	private void writeType(String type) {
		map.position(0);
		map.putInt(type.length());
		for (int i = 0; i < type.length(); i++) {
			map.putChar(type.charAt(i));
		}
	}

	private void writeFields(List<String> fields) {
		int i = 0;
		fieldsNames = new String[fields.size()];
		for (String field : fields) {
			fieldsNames[i] = field;
			map.putInt(field.length());
			for (int j = 0; j < field.length(); j++) {
				map.putChar(field.charAt(j));
			}
			i++;
		}
	}
	
	private void calculateCapacity(){
		int cap = 4 + 2* type.length();
		for(String field : fieldsNames){
			cap += 4 + 2*field.length();
		}
		capacity = cap;
	}

	public void writeTypeAndFields(String type, List<String> fields) {
		this.type = type;
		writeType(type);
		writeFields(fields);
		calculateCapacity();
		objectsIndex = new int[map.limit()/fields.size()];
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
