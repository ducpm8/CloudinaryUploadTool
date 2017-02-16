package cloudinary.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class Directory {
	public HashMap<String,HashMap<String,ArrayList<String>>> listFile = new  HashMap<String,HashMap<String,ArrayList<String>>>();
	public HashMap<String,HashMap<String,ArrayList<String>>> listf(String directoryName) {

	    File directory = new File(directoryName);
	    
	    System.out.print("1");

	    // get all the files from a directory
	    File[] fList = directory.listFiles();
	    
	    File[] fListChild;
	    boolean finalExist = false;
	    boolean transExist = false;
	    
	    String currentName = "";
	    String reason = "";
	    HashMap<String,ArrayList<String>> currFileList = new HashMap<String,ArrayList<String>>();
	    HashMap<String,ArrayList<String>> currFileListFail = new HashMap<String,ArrayList<String>>();
	    
	    ArrayList currFileCList = new ArrayList();
	    for (File file : fList) {
	    	finalExist = false;
	    	transExist = false;
	    	
	    	currentName = file.getName();
        	listFile.put(currentName, new HashMap<String,ArrayList<String>>());
        	directory = new File(file.getAbsolutePath());
	    	fListChild = directory.listFiles();
	    	if (fListChild != null) {
		    	for (File fileC : fListChild) {
		    		reason = "Do not include Photo sub folder";
		    		currFileCList = new ArrayList();
		    		try {
			    		if (fileC.isDirectory()) {
			    			//final folder
			    			if (fileC.getName().toLowerCase().contains("photo 2-4")) {
			    				currFileList = listFile.get(currentName);
			    				currFileCList = listFileInFolderString(fileC.getAbsolutePath());
			    				
			    				if (currFileCList.size() > 2) {
			    					currFileList.put("FINAL", currFileCList);
			    					finalExist = true;
			    				} else {
			    					reason = "Photo2-4 file missing";
			    					finalExist = false;
			    					break;
			    				}
			    				
			    			} else if (fileC.getName().toLowerCase().contains("photo 1")) {
			    				currFileList = listFile.get(currentName);
			    				currFileCList = listFileInFolderString(fileC.getAbsolutePath());
			    				
			    				if (currFileCList.size() == 1) {
			    					currFileList.put("TRANSPARENT", currFileCList);
			    					transExist = true;
			    				} else {
			    					reason = "Photo1 empty or has more than 1 photo";
			    					transExist = false;
			    					break;
			    				}
			    			} 
			    		}
		    		} catch (IOException ioe) {
		    			continue;
		    		}
		    	}
	    	}
	    	if (!(transExist && finalExist)) {
	    		currFileList = listFile.get(currentName);
	    		currFileList.put("FAIL", new ArrayList<String>(Arrays.asList(reason)));
	    		//listFile.remove(currentName);
	    	}

	    }
	    return listFile;
	}
	
//	public HashMap<String,ArrayList<String>> listf(String directoryName) {
//
//	    File directory = new File(directoryName);
//
//	    // get all the files from a directory
//	    File[] fList = directory.listFiles();
//	    
//	    File[] fListChild;
//	    
//	    String currentName = "";
//	    ArrayList currFileList = new ArrayList();
//	    for (File file : fList) {
//	    	currentName = file.getName();
//	    	
//        	listFile.put(currentName, new ArrayList());
//        	
//        	directory = new File(file.getAbsolutePath());
//	    	fListChild = directory.listFiles();
//	    	
//	    	for (File fileC : fListChild) { 
//	    		if (fileC.isDirectory()) {
//	    			if (fileC.getName().toLowerCase().contains("final")) {
//	    			
//	    			} else if (fileC.getName().toLowerCase().contains("transparent")) {
//	    				
//	    			} 
//	    		}
//	    	}
//	    	
////	    	if (file.isDirectory() && currentName.indexOf("_ok") >= 0) {
////	        	listFile.put(currentName, new ArrayList());
////	        	
////	        	directory = new File(file.getAbsolutePath());
////		    	fListChild = directory.listFiles();
////		    	
////		    	 for (File fileC : fListChild) { 
////		    		 if (fileC.isFile()) {
////		 	            //System.out.println(file.getAbsolutePath());
////		 	        	currFileList = listFile.get(currentName);
////		 	        	currFileList.add(fileC.getAbsolutePath());
////		 	        }
////		    	 }
////	        } else if (file.isDirectory() && currentName.indexOf("_ok") < 0) {
////	        	throw new IllegalArgumentException(currentName);
////	        }
//	    }
//	    //System.out.println(fList);
//	    return listFile;
//	}
	
	public static List<File> listFileInFolder(String directoryName) throws IOException {
        
		File dir = new File(directoryName);

		//System.out.println("Getting all files in " + dir.getCanonicalPath() + " including those in subdirectories");
		List<File> files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
//		for (File file : files) {
//			System.out.println("file: " + file.getCanonicalPath());
//		}
		
//		File directory = new File(directoryName);
//
//        List<File> resultList = new ArrayList<File>();
//
//        // get all the files from a directory
//        File[] fList = directory.listFiles();
//        resultList.addAll(Arrays.asList(fList));
//        for (File file : fList) {
//            if (file.isFile()) {
//                //System.out.println(file.getAbsolutePath());
//                resultList.addAll(listFileInFolder(file.getAbsolutePath()));
//            } else if (file.isDirectory()) {
//                //resultList.addAll(listFileInFolder(file.getAbsolutePath()));
//            }
//        }
        
        return files;
    } 
	
	public static ArrayList<String> listFileInFolderString(String directoryName) throws IOException {
        
		File dir = new File(directoryName);

		//System.out.println("Getting all files in " + dir.getCanonicalPath() + " including those in subdirectories");
		List<File> files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		
		ArrayList<String> fileListString = new ArrayList<String>(); 
		for (File fC: files) {
			fileListString.add(fC.getAbsolutePath());
		}
		
//		for (File file : files) {
//			System.out.println("file: " + file.getCanonicalPath());
//		}
		
//		File directory = new File(directoryName);
//
//        List<File> resultList = new ArrayList<File>();
//
//        // get all the files from a directory
//        File[] fList = directory.listFiles();
//        resultList.addAll(Arrays.asList(fList));
//        for (File file : fList) {
//            if (file.isFile()) {
//                //System.out.println(file.getAbsolutePath());
//                resultList.addAll(listFileInFolder(file.getAbsolutePath()));
//            } else if (file.isDirectory()) {
//                //resultList.addAll(listFileInFolder(file.getAbsolutePath()));
//            }
//        }
        
        return fileListString;
    } 
}
