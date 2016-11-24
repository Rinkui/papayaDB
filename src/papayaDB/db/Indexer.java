package papayaDB.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Arrays;
import java.util.Objects;

import jdk.internal.reflect.ReflectionFactory.GetReflectionFactoryAction;

public class Indexer {


	
	
	


	public int[] getIndex(int firstIndex) {
		// on vérifie que l'index donné est dans le tableau et qu'il reste le
		// bon nombre de champs
		return Arrays.copyOfRange(objectsIndex, firstIndex, firstIndex + fieldsIndex.length);
	}

	public String getFieldValue(int firstIndex, String fieldName) {
		// doit appeler le reader pour récupérer l'indice du champs dans le
		// tableau fieldsIndex
		// puis appeler le reader avec l'index de début du champs
		// enfin retourne la string renvoyée par le reader
		return "";
	}

	// pour avoir une map :
	// FileInputStream in = new FileInputStream(file);
	// FileChannel fc = in.getChannel();
	// this.map = fc.map(MapMode.READ_WRITE, 0, fc.size());
	// in.close();
}
