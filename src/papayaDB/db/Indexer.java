package papayaDB.db;

import java.util.Arrays;
import java.util.Objects;

public class Indexer {
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
	// * seconde ligne : nombre de champs puis tous les champs avec la taille en octet devant chaque
	// champs, un index par champs qui est au niveau du int
	// * le reste des lignes : une ligne par objet avec seulement les valeurs,
	// la taille en octet devant chaque valeur, un index par champs qui se
	// trouve au niveau de l'int

	private static final int MARGIN = 100; // la marge que l'on choisi de
											// laisser pour l'ajout d'objets
	private int[] fieldsIndex; // indexs des champs
	private int[] objectsIndex; // indexs des objets (les indexs que l'on trouve
								// dans le fichier de int)

	public Indexer(int nbFields, int nbObjects) {
		this.fieldsIndex = new int[nbFields];
		this.objectsIndex = new int[nbObjects + MARGIN];
	}

	public int[] getIndex(int firstIndex) {
		// on vérifie que l'index donné est dans le tableau et qu'il reste le
		// bon nombre de champs
		Objects.checkIndex(firstIndex, objectsIndex.length - fieldsIndex.length);
		return Arrays.copyOfRange(objectsIndex, firstIndex, firstIndex + fieldsIndex.length);
	}
	
	public String getFieldValue(int firstIndex, String fieldName){
		// doit appeler le reader pour récupérer l'indice du champs dans le tableau fieldsIndex
		// puis appeler le reader avec l'index de début du champs 
		// enfin retourne la string renvoyée par le reader
		return "";
	}
}
