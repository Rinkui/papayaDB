package papayaDB.db;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import papayaDB.structures.Tree;
import papayaDB.structures.Tuple;

public class DataBase {
	private Tree tree;
	private Reader reader;
	private int modification;
	private final static int MAX_MODIF = 5000;

	public DataBase(String type) throws IOException {
		String path = "DataBases/" + type;
		
		if(exist(type)){
			initReader(new File(path + "/dataBase"), new File(path+"/holes"));
//			this.tree = Tree.readTreeInFile(new File(path + "/tree")); //tree
			this.tree = new Tree();
			reader.readHoles(); //holes
		} else {
			createFileIfNeeded(type);
			initReader(new File(path + "/dataBase"), new File(path+"/holes"));
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

	private void initReader(File dataBase, File hole) throws IOException{
		RandomAccessFile raf = new RandomAccessFile(dataBase, "rw");
		int size = raf.length() > 0 ? (int) raf.length() * 2 : 4096;
		MappedByteBuffer map = raf.getChannel().map(MapMode.READ_WRITE, 0, size);		
		this.reader = new Reader(map, raf, hole);
	}
	
	public DataBase(String type, List<String> fields) throws IOException {
		this(type);
		reader.writeTypeAndFields(type, fields);
	}

	public int add(List<Tuple<String,String>> object) throws IOException {
		modification += 1;
		if( modification >= MAX_MODIF){
			writeAddedObjects();
			writeHole();
		}
		return reader.addObject(object);
	}

	private void writeAddedObjects() throws IOException {
		reader.writeAddedObjects();
	}
	
	private void writeHole() throws IOException{
		reader.writeHoles();
	}
	
	public List<List<Tuple<String,String>>> getAll(){
		return reader.getAll();
	}
	
	public boolean remove(int object) throws IOException{
		modification += 1;
		if( modification >= MAX_MODIF){
			writeAddedObjects();
			writeHole();
		}
		return reader.suppressObject(object);
	}
	
	public List<List<Tuple<String,String>>> get(List<Tuple<String,String>> requests){
		requests.sort(new Comparator<Tuple<String,String>>() {
			@Override
			public int compare(Tuple<String, String> t1, Tuple<String, String> t2) {
				int reqRes = t1.getKey().compareTo(t2.getKey());
				if(reqRes == 0)
					return t1.getValue().compareTo(t2.getValue());
				return reqRes;
			}
		});
		List<Integer> values = tree.get(requests);
		if(values == null ){
			List<Tuple<Tuple<String,String>, List<Integer>>> finalList = new ArrayList<>();
			List<Integer> set = new ArrayList<>();
			for(Tuple<String, String> req : requests){
				set = reader.getObjectsRequested(set, req);
				finalList.add(new Tuple<Tuple<String,String>, List<Integer>>(req, set));
			}	
			new Thread(()->{			
				tree.add(finalList);
			}).start();
			values = finalList.get(finalList.size()-1).getValue();
		}
		List<List<Tuple<String, String>>> result = new ArrayList<>();
		for(Integer i : values){
			result.add(reader.getObject(i));
		}
		return result;
	}

	public static void main(String[] args) throws IOException {
		ArrayList<String> list = new ArrayList<String>();
		list.add("author");
		list.add("price");
	
		DataBase db = new DataBase("Book", list);
		
		List<Tuple<String, String>> object = new ArrayList<>();
		object.add(new Tuple<String, String>("author", "X"));
		object.add(new Tuple<String, String>("price", "12"));
		List<Tuple<String, String>> object2 = new ArrayList<>();
		object2.add(new Tuple<String, String>("price", "52"));
		object2.add(new Tuple<String, String>("author", "Y"));
		List<Tuple<String, String>> object3 = new ArrayList<>();
		object3.add(new Tuple<String, String>("price", "0"));
		object3.add(new Tuple<String, String>("author", "a"));
		
		int index = db.add(object);
		int index2 = db.add(object2);
		int index3 = db.add(object3);
		
		db.writeAddedObjects();
		
		System.out.println("\nGetAll");
		System.out.println(db.getAll());
		
		db.remove(index2);
		
		System.out.println(db.getAll());
	
		
		List<Tuple<String, String>> requests = new ArrayList<>();
		requests.add(new Tuple<String, String>("price", "[;100]"));
		List<List<Tuple<String,String>>> resultForServer = db.get(requests);
		System.out.println("\nResult for server");
		System.out.println(resultForServer);
	}
}
