package RTi.Util.Test;

import java.util.ArrayList;
import java.util.List;
import java.io.File;

public class TestCollector
{
	private List<String> tests;
	
public TestCollector()
{
	tests = new ArrayList<String>();
}
	
public void visitAllFiles(File dir) {
	
	if (dir.isDirectory()) 
	{
		String[] children = dir.list();
		for (int i = 0; i < children.length; i++) 
		{
			visitAllFiles(new File(dir, children[i]));
		}
	}
	else 
	{
		//add to list
		if(dir.toString().endsWith("Test.java"))
			tests.add(dir.toString());
		
	}
}	

//	 returns a formatted filename with the correct package
//	 and filename from a given relative path.
public String formatFileName(String testCase)
{
		String fName = "";
		testCase.trim();
		String [] fileSplit = testCase.split("\\\\");
		int flag = 0;
		
		for(int i = 2; i < fileSplit.length; i++)
		{
			if(flag == 1)
			{
				if(i == fileSplit.length - 1)
				{
					fName += (fileSplit[i].split("\\."))[0];
				}
				else
				{
					fName += ((fileSplit[i] + "."));
				}
			}
			
			if(fileSplit[i].equals("src"))
			{
				flag = 1;
			}
		}
		
		return fName;
}

public List<String> getTestList()
{
	return tests;
}

}