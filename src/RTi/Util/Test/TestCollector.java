// TestCollector - create test suite

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

CDSS Common Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    CDSS Common Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with CDSS Common Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

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
