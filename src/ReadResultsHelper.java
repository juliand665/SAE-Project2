import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;

public class ReadResultsHelper {
	public static void main(String[] args) {
		
		File folder = new File("src/temporary");
		File[] listOfFiles = folder.listFiles();
		Arrays.sort(listOfFiles);
		int numOfFiles = listOfFiles.length;
		
		boolean solutions[][] = new boolean[numOfFiles][2];
		
		String codeLines = "";
		
		for(int i = 0; i < numOfFiles; i++){
			String name = listOfFiles[i].getName();
			solutions[i] = getExpectedSolutionFromFile("src/" + name);
			//{"Test_1", true, true},
			codeLines += "{\"" + listOfFiles[i].getName().substring(0, listOfFiles[i].getName().length()-5) + "\"," + solutions[i][0] + ", " + solutions[i][1] + "}, \n";
		}
		
		System.out.println(codeLines);

	}
	
	public static boolean[] getExpectedSolutionFromFile(String filename){
		String fileString = "";
		
		try {
			fileString = FileUtils.readFileToString(new File(filename), (Charset) null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String[] lines = fileString.split("\n");
		//remove whitespaces: 
		lines[0] = lines[0].replaceAll("\\s+","");
		lines[1] = lines[1].replaceAll("\\s+","");
		
		
		boolean[] solutions = {false, false};
		
		if(!lines[0].equals("//WELD_AT_OK") && !lines[0].equals("//WELD_AT_NOT_OK"))
			System.out.println(filename + " has to be initialized first correctly: WELD_AT");
		
		if(!lines[1].equals("//WELD_BETWEEN_OK") && !lines[1].equals("//WELD_BETWEEN_NOT_OK"))
			System.out.println(filename + " has to be initialized first correctly: WELD_BETWEEN");
		
		if(lines[0].equals("//WELD_AT_OK"))
			solutions[0] = true;
		
		if(lines[1].equals("//WELD_BETWEEN_OK"))
			solutions[1] = true;
		
		
		return solutions;
	}
	

}