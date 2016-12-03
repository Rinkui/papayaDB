package papayaDB.db;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import papayaDB.structures.Tree;

public class DataBase {
	private Tree tree;
	private Reader reader;
	
	public DataBase(String type) throws IOException{
		Path dataBaseDirectoryPath = Paths.get("DataBases");
		File dataBaseDirectory = dataBaseDirectoryPath.toFile();
		File[] files = dataBaseDirectory.listFiles();
		File newDataBase = null;
		Boolean exists = false;
		for( File file : files ){
			if( file.getName().equals(type) )
				newDataBase = file;
		}
		if( newDataBase == null ){
			newDataBase = new File("DataBases/" + type);
			if(!newDataBase.createNewFile())
				throw new IllegalStateException("DataBase can't be created");
		}
		else {
			exists = true;
		}
		RandomAccessFile raf = new RandomAccessFile(newDataBase, "rw");
		int size = raf.length() > 0 ? (int)raf.length()*2 : 4096;
		MappedByteBuffer map = raf.getChannel().map(MapMode.READ_WRITE, 0, size);
		this.reader = new Reader(map, raf);
		if(exists){
			this.tree = Tree.readTreeInFile(newDataBase);
		}
		else{
			this.tree = new Tree();
		}
	}
	
	public DataBase(String type, List<String> fields) throws IOException{
		this(type);
		reader.writeTypeAndFields(type, fields);
	}
	
	public int addObject(String[] object){
		return reader.addObject(object);
	}
	
	public void writeAddedObjects() throws IOException{
		reader.writeAddedObjects();
	}
	
	public static void main(String[] args) throws IOException {
		ArrayList<String> list = new ArrayList<String>();
		list.add("author");
		list.add("price");
		DataBase db = new DataBase("Book", list);
		String[] object = {"X", "12"};
		db.addObject(object);
		db.writeAddedObjects();
	}
}
