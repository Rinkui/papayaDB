package papayaDB.db;

import java.nio.MappedByteBuffer;

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
	private int[] fieldsSizes = null; // taille max des champs
	private int[] objectsIndex = null; // indexs des objets (les indexs que l'on trouve
								// dans le fichier de int)
	private String type = null;

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
		if(type == null)
			firstTypeReading();
		int nbFields = map.getInt();
		fieldsNames = new String[nbFields];
		fieldsSizes = new int[nbFields];
		int nbChar;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < nbFields; i++) {
			fieldsSizes[i] = map.getInt();
			nbChar = map.getInt();
			for (int j = 0; j < nbChar; j++) {
				map.getChar();
			}
			fieldsNames[i] = sb.toString();
			sb.setLength(0);
		}
	}

	private void firstObjectsReading(int nbFields, int margin) {
		if(objectsIndex == null)
			firstFieldsReading();
		int nbObjects = map.getInt();
		objectsIndex = new int[nbObjects + margin];
		int nbChar;
		for (int i = 0; i < nbObjects; i++) {
			objectsIndex[i] = map.position();
			nbChar = map.getInt();
			for (int j = 0; j < nbChar; j++) {
				map.getChar();
			}
		}
	}
	
	private int getFieldIndex(String fieldName){
		for(int i = 0 ; i < fieldsNames.length ; i ++){
			if( fieldName.equals(fieldsNames[i]) )
				return i;
		}
		return -1;
	}
	
	public String getFieldValue(int objectIndex, String fieldName){
		if(fieldsNames == null)
			firstFieldsReading();
		int indexField;
		if( (indexField = getFieldIndex(fieldName)) == -1 )
			return "";
		
		return "";
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
