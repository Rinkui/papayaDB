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
import papayaDB.structures.Tuple;

public class DataBase {
	private Tree tree;
	private Reader reader;

	public DataBase(String type) throws IOException {
		Path dataBasesDirectoryPath = Paths.get("DataBases");
		File dataBasesDirectory = dataBasesDirectoryPath.toFile();
		File[] files = dataBasesDirectory.listFiles();
		File myDataBaseDir = null, tree = null, dataBase = null, holes = null;
		Boolean exists = false;
		String myDataBasePath = "DataBases/" + type;
		
		for (File file : files) {
			if (file.getName().equals(type)){
				myDataBaseDir = file;
				for(File settings : myDataBaseDir.listFiles()){
					String fileName = settings.getName();
					if(fileName.equals("tree"))
						tree = settings;
					if(fileName.equals("holes"))
						holes = settings;
					if(fileName.equals("dataBase"))
						dataBase = settings;
				}
			}
		}
		
		if (myDataBaseDir == null) {
			myDataBaseDir = new File(myDataBasePath);
			if (!myDataBaseDir.mkdir())
				throw new IllegalStateException("DataBase directory can't be created");
			tree = new File(myDataBasePath + "/tree");
			dataBase = new File(myDataBasePath + "/dataBase");
			holes = new File(myDataBasePath + "/holes");
			if( !(tree.createNewFile() && dataBase.createNewFile() && holes.createNewFile()) )
				throw new IllegalStateException("One of DataBases settings file can't be created");
		} else {
			exists = true;
		}
		RandomAccessFile raf = new RandomAccessFile(dataBase, "rw");
		int size = raf.length() > 0 ? (int) raf.length() * 2 : 4096;
		MappedByteBuffer map = raf.getChannel().map(MapMode.READ_WRITE, 0, size);		
		this.reader = new Reader(map, raf);
		
		if (exists) {
			this.tree = Tree.readTreeInFile(tree);
			reader.readHoles(holes);
		} else {
			this.tree = new Tree();
		}
	}

	public DataBase(String type, List<String> fields) throws IOException {
		this(type);
		reader.writeTypeAndFields(type, fields);
	}

	public int addObject(String[] object) {
		return reader.addObject(object);
	}

	public void writeAddedObjects() throws IOException {
		reader.writeAddedObjects();
	}

	public boolean fieldCompareToValue(int object, String field, String fieldValue) {
		if (fieldValue.charAt(0) == '[') {
			String[] borns = fieldValue.substring(1, fieldValue.length() - 1).split(";");
			return reader.fieldInfOrSupp(object, field, borns[0], borns[1]);
		}
		return reader.fieldEqualTo(object, field, fieldValue);
	}
	
	public List<List<Tuple<String,String>>> getAll(){
		return reader.getAll();
	}

	public static void main(String[] args) throws IOException {
		ArrayList<String> list = new ArrayList<String>();
		list.add("author");
		list.add("price");
	
		DataBase db = new DataBase("Book", list);
		
		String[] object = { "X", "12" };
		String[] object2 = { "Y", "52" };
		String[] object3 = { "a", "0" };
		
		int index = db.addObject(object);
		int index2 = db.addObject(object2);
		int index3 = db.addObject(object3);
		
		db.writeAddedObjects();
		
		System.out.println(db.fieldCompareToValue(index, "price", "12"));
		System.out.println(db.fieldCompareToValue(index, "price", "[;40]"));
		System.out.println(db.fieldCompareToValue(index, "price", "40"));
		System.out.println(db.fieldCompareToValue(index, "author", "[a;z]"));
		
		System.out.println(db.fieldCompareToValue(index2, "price", "12"));
		System.out.println(db.fieldCompareToValue(index2, "price", "[;40]"));
		System.out.println(db.fieldCompareToValue(index2, "price", "40"));
		System.out.println(db.fieldCompareToValue(index2, "author", "[a;z]"));
		
		System.out.println(db.getAll());
		
		db.reader.suppressObject(index2);
		
		System.out.println(db.getAll());
	}
}
