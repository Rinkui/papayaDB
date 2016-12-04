package papayaDB.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import papayaDB.structures.HoleList;
import papayaDB.structures.Link;
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

	private final ReentrantLock lock = new ReentrantLock();

	private MappedByteBuffer map;
	private final RandomAccessFile file;

	// pour les deux tableaux on retient l'index de la taille du champs
	// à changer : on stocke le nom des champs
	private String[] fieldsNames = null; // indexs des champs
	private int[] objectsIndex = null; // indexs des objets (les indexs que l'on
										// trouve dans le fichier de int)
	private HoleList holeList = new HoleList();
	private File holeFile = null;
	private ArrayList<Tuple<Integer, String[]>> addList; // string a écrire dans
	// le fichier
	private String type = null;
	private int indexTableCapacity;

	public Reader(MappedByteBuffer map, RandomAccessFile file, File hole) {
		this.map = map;
		this.file = file;
		this.holeFile = hole;
	}

	private void firstTypeReading() {
		StringBuilder sb = new StringBuilder();
		map.position(0);
		int typeSize = map.getInt();
		for (int i = 0; i < typeSize; i++) {
			sb.append(map.getChar());
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
		indexTableCapacity = nbObjects * nbFields;

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

	private String readFieldValue(int objectTableIndex, int fieldIndex) {
		map.position(objectsIndex[objectTableIndex + fieldIndex]);
		int size = map.getInt();
		if (size == 0)
			return "";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			sb.append(map.getChar());
		}
		return sb.toString();
	}

	public String getFieldValue(int objectTableIndex, String fieldName) {
		lock.lock();
		try {
			if (objectsIndex == null)
				firstObjectsReading(fieldName.length());
			int fieldIndex;
			if ((fieldIndex = getFieldIndex(fieldName)) == -1)
				throw new IllegalArgumentException("Field name doesn't exist");
			return readFieldValue(objectTableIndex, fieldIndex);
		} finally {
			lock.unlock();
		}
	}

	// les index fonctionnent selon les bytes, donc un int = 4 et un char = 2
	private int getObjectsSize(int objectTableIndex) {
		int size = 0;
		for (int i = objectTableIndex; i < objectTableIndex + fieldsNames.length; i++) {
			map.position(objectsIndex[i]);
			size += map.getInt() * 2 + 4;
		}
		return size;
	}

	// un champs est composé de sa longueur et de sa valeur
	private int getFieldSize(String field) {
		return 4 + 2 * field.length();
	}

	public boolean suppressObject(int objectTableIndex) {
		if (objectTableIndex < 0 || objectTableIndex > indexTableCapacity - fieldsNames.length)
			return false;
		lock.lock();
		try {
			holeList.addHole(objectTableIndex, objectsIndex[objectTableIndex], getObjectsSize(objectTableIndex));
			if (objectTableIndex == indexTableCapacity - fieldsNames.length)
				indexTableCapacity -= fieldsNames.length;
			return true;
		} finally {
			lock.unlock();
		}
	}

	private Link getNewIndex(String[] objects, int size) {
		Link link = null;
		if (!holeList.isEmpty())
			link = holeList.removeHole(size);
		if (link != null)
			return link;
		int tmpIndexTable = indexTableCapacity;
		indexTableCapacity += fieldsNames.length;
		return new Link(objectsIndex[tmpIndexTable], tmpIndexTable, size);
	}

	private void fillObjectsIndex(int[] valuesSize, int firstIndex) {
		for (int i = firstIndex, j = 0; i < firstIndex + valuesSize.length; i++, j++) {
			// on additionne l'index précédent avec la taille du champs suivant
			objectsIndex[i + 1] = objectsIndex[i] + valuesSize[j];
		}
	}

	private void addToAddList(int size, String[] object) {
		if (addList == null)
			addList = new ArrayList<Tuple<Integer, String[]>>();
		addList.add(new Tuple<Integer, String[]>(size, object));
	}

	private String[] objectToArray(List<Tuple<String, String>> object) {
		String[] modifedObj = new String[fieldsNames.length];
		for (Tuple<String, String> tuple : object) {
			modifedObj[getFieldIndex(tuple.getKey())] = tuple.getValue();
		}
		return modifedObj;
	}

	public int addObject(List<Tuple<String, String>> object) {
		String[] modifedObj = objectToArray(object);
		int size = 4; // premier int donne le nombre de champs
		int valuesSize[] = new int[modifedObj.length];
		int fieldSize;
		for (int i = 0; i < modifedObj.length; i++) {
			fieldSize = getFieldSize(modifedObj[i]);
			// on met directement les octets avec le int et la String
			valuesSize[i] = fieldSize;
			size += fieldSize;
		}
		lock.lock();
		try {
			Link link = getNewIndex(modifedObj, size);
			addToAddList(link.getMapIndex(), modifedObj);
			fillObjectsIndex(valuesSize, link.getTableIndex());
			return link.getTableIndex();
		} finally {
			lock.unlock();
		}
	}

	private void resizeMap() throws IOException {
		int newlenght = map.limit() * 2;
		file.setLength(newlenght);
		map = file.getChannel().map(MapMode.READ_WRITE, 0, newlenght);
	}

	public void writeAddedObjects() throws IOException {
		lock.lock();
		try {
			for (Tuple<Integer, String[]> tuple : addList) {
				int pos = tuple.getKey();
				if (pos > map.limit())
					resizeMap();
				map.position(pos);
				String[] haveToWrite = tuple.getValue();
				for (String s : haveToWrite) {
					map.putInt(s.length());
					for (int i = 0; i < s.length(); i++) {
						map.putChar(s.charAt(i));
					}
				}
			}
		} finally {
			lock.unlock();
		}
	}

	public String getType() {
		lock.lock();
		try {
			if (type == null)
				firstTypeReading();
			return type;
		} finally {
			lock.unlock();
		}
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
		objectsIndex[0] = map.position();
	}

	public void writeTypeAndFields(String type, List<String> fields) {
		lock.lock();
		try {
			this.type = type;
			objectsIndex = new int[map.limit() / fields.size()];
			writeType(type);
			writeFields(fields);
			indexTableCapacity = 0;
		} finally {
			lock.unlock();
		}
	}

	public List<Integer> getObjectsRequested(List<Integer> set, Tuple<String, String> request) {
		ArrayList<Integer> list = new ArrayList<>();
		lock.lock();
		try {
			if (set.isEmpty()) {
				for (int i = 0; i < indexTableCapacity; i += fieldsNames.length) {
					if (fieldCompareToValue(i, request.getKey(), request.getValue()))
						list.add(i);
				}
			} else {
				for (Integer i : set) {
					if (fieldCompareToValue(i, request.getKey(), request.getValue()))
						list.add(i);
				}
			}
			return list;
		} finally {
			lock.unlock();
		}
	}

	public boolean fieldCompareToValue(int object, String field, String fieldValue) {
		if (fieldValue.charAt(0) == '[') {
			String[] borns = fieldValue.substring(1, fieldValue.length() - 1).split(";");
			return fieldInfOrSupp(object, field, borns[0], borns[1]);
		}
		return fieldEqualTo(object, field, fieldValue);
	}

	private boolean fieldEqualTo(int object, String field, String fieldValue) {
		int fieldIndex = getFieldIndex(field);
		lock.lock();
		try {
			map.position(objectsIndex[object + fieldIndex]);
			int size = map.getInt();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < size; i++) {
				sb.append(map.getChar());
			}
			return fieldValue.equals(sb.toString());
		} finally {
			lock.unlock();
		}
	}

	public boolean fieldInfOrSupp(int object, String field, String bornInf, String bornSupp) {
		String fieldValue;
		lock.lock();
		try {
			fieldValue = getFieldValue(object, field).toLowerCase();
		} finally {
			lock.unlock();
		}

		bornInf = bornInf.toLowerCase();
		bornSupp = bornSupp.toLowerCase();
		if (bornInf.isEmpty()) {
			try {
				return Integer.parseInt(fieldValue) <= Integer.parseInt(bornSupp);
			} catch (NumberFormatException e) {
				return fieldValue.compareTo(bornSupp) <= 0 ? true : false;
			}
		}

		if (bornSupp.isEmpty()) {
			try {
				return Integer.parseInt(fieldValue) <= Integer.parseInt(bornInf);
			} catch (NumberFormatException e) {
				return fieldValue.compareTo(bornInf) >= 0 ? true : false;
			}
		}

		try {
			int fieldValueInt = Integer.parseInt(fieldValue);
			return fieldValueInt >= Integer.parseInt(bornInf) && fieldValueInt <= Integer.parseInt(bornSupp);
		} catch (NumberFormatException e) {
			return fieldValue.compareTo(bornInf) >= 0 && fieldValue.compareTo(bornSupp) <= 0 ? true : false;
		}
	}

	public List<Tuple<String, String>> getObject(int object) {
		lock.lock();
		try {
			if (!holeList.containsKey(object)) {
				ArrayList<Tuple<String, String>> oneObject = new ArrayList<>();
				oneObject.add(new Tuple<String, String>("id", String.valueOf(object)));
				for (int j = 0; j < fieldsNames.length; j++) {
					String fieldName = fieldsNames[j];
					String fieldValue = getFieldValue(object, fieldName);
					oneObject.add(new Tuple<String, String>(fieldName, fieldValue));
				}
				return oneObject;
			}
			return null;
		} finally {
			lock.unlock();
		}
	}

	public List<List<Tuple<String, String>>> getAll() {
		lock.lock();
		try {
			ArrayList<List<Tuple<String, String>>> totalList = new ArrayList<>();
			for (int i = 0; i < indexTableCapacity; i += fieldsNames.length) {
				List<Tuple<String, String>> object = getObject(i);
				if (object != null)
					totalList.add(object);
			}
			return totalList;
		} finally {
			lock.unlock();
		}
	}

	public void readHoles() throws IOException {
		lock.lock();
		try {
			long size = Files.size(holeFile.toPath());
			if (size == 0) {
				return;
			}
			FileInputStream in = new FileInputStream(holeFile);
			FileChannel fc = in.getChannel();
			MappedByteBuffer map = fc.map(MapMode.READ_WRITE, 0, fc.size());
			while (map.remaining() >= (Integer.BYTES * 3)) {
				this.holeList.addHole(map.getInt(), map.getInt(), map.getInt());
			}
			in.close();
		} finally {
			lock.unlock();
		}
	}

	public void writeHoles() throws IOException {
		lock.lock();
		try {
			RandomAccessFile raf = new RandomAccessFile(holeFile, "rw");
			int lenght = this.holeList.size();
			raf.setLength(lenght * 3 * Integer.BYTES);
			MappedByteBuffer map = raf.getChannel().map(MapMode.READ_WRITE, 0, lenght);
			Iterator<Integer> iterator = holeList.getIterator();
			while (iterator.hasNext()) {
				int value = iterator.next();
				map.putInt(value);
				Tuple<Integer, Integer> tuple = holeList.get(value);
				map.putInt(tuple.getKey());
				map.putInt(tuple.getValue());
			}
			raf.close();
		} finally {
			lock.unlock();
		}
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
