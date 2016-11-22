package papayaDB.db;

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
	// * seconde ligne : tous les champs avec la taille en octet devant chaque
	// champs
	// * le reste des lignes : une ligne par objet avec seulement les valeurs,
	// la taille en octet devant chaque valeur

	private int[] fieldsIndex; // indexs des champs
	private int[] objectsIndex; // indexs des objets (les indexs que l'on trouve
								// dans le fichier de int)
	
}
