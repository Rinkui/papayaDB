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
	private int firstObjectPos;

	private Reader(FileChannel fc) throws IOException {
		this.map = fc.map(MapMode.READ_WRITE, 0, fc.size());
	}

	public Reader getReader(File file) throws IOException {
		FileInputStream in = new FileInputStream(file);
		FileChannel fc = in.getChannel();
		Reader reader = new Reader(fc);
		in.close();
		return reader;
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

	// a nouveau on replace le curseur au bon endroit
	public int[] firstFieldsReading() {
		int i, nbFields;
		char c;
		map.position(firstFieldPos);
		nbFields = map.getInt();
		int[] fieldsIndex = new int[nbFields];
		// TODO
		// il faut alterner les getint() et getchar() quelle est la condition
		// d'arrêt sachant que get bouge le curseur ?
		return null;
	}

	public int[] firstObjectsReading() {
		return null;
	}

	// pour récuperer une donnée de map : il faut placer le curseur au bon
	// endroit index avec map.position(index), puis faire un map.get(byte[],
	// offset, length) pour
	// récuperer tout le contenu, attention, le curseur sera à la fin
}
