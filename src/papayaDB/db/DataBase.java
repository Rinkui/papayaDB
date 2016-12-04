package papayaDB.db;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import papayaDB.structures.Tree;
import papayaDB.structures.Tuple;

public class DataBase {
	private Tree tree;
	private Reader reader;

	public DataBase(String type) throws IOException {
		String path = "DataBases/" + type;
		
		if(exist(type)){
			initReader(new File(path + "/dataBase"));
			this.tree = Tree.readTreeInFile(new File(path + "/tree")); //tree
			reader.readHoles(new File(path + "/holes")); //holes
		} else {
			createFileIfNeeded(type);
			initReader(new File(path + "/dataBase"));
			this.tree = new Tree();
		}
	}
	
	private boolean exist(String type){
		if(!Paths.get("DataBases").toFile().exists()){ return false; }
		String path = "DataBases/" + type;
		if(!Paths.get(path).toFile().exists()){ return false; }
		if(!Paths.get(path + "/tree").toFile().exists()|| 
				!Paths.get(path + "/dataBase").toFile().exists() ||
				!Paths.get(path + "/holes").toFile().exists()){ 
			return false;
		}
		return true;
	}
	
	private void createFileIfNeeded(String type) throws IOException{
		if(!Paths.get("DataBases").toFile().exists()){ 
			if(!new File("DataBases").mkdir()){
				throw new IllegalStateException("DataBases directory can't be created");
			}
		}
		String path = "DataBases/" + type;
		if(!Paths.get(path).toFile().exists()){
			if(!new File(path).mkdir()){
				throw new IllegalStateException("DataBase directory can't be created");
			}
		}
		if(!Paths.get(path + "/tree").toFile().exists() || !Paths.get(path + "/dataBase").toFile().exists() || !Paths.get(path + "/holes").toFile().exists()){
			File tree = new File(path + "/tree");
			File dataBase = new File(path + "/dataBase");
			File holes = new File(path + "/holes");
			if( !(tree.createNewFile() && dataBase.createNewFile() && holes.createNewFile()) )
				throw new IllegalStateException("One of DataBases settings file can't be created");
		}
	}

	private void initReader(File dataBase) throws IOException{
		RandomAccessFile raf = new RandomAccessFile(dataBase, "rw");
		int size = raf.length() > 0 ? (int) raf.length() * 2 : 4096;
		MappedByteBuffer map = raf.getChannel().map(MapMode.READ_WRITE, 0, size);		
		this.reader = new Reader(map, raf);
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
	
	public boolean remove(int object){
		return reader.suppressObject(object);
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
