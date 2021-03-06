package ch.ethz.fgremper.cloudstudio.server;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import difflib.Delta;
import difflib.Delta.TYPE;
import difflib.DiffUtils;
import difflib.Patch;

/**
 * 
 * Utility class to provide side-by-side comparison of two files
 * 
 * @author Fabian Gremper
 * 
 */
public class SideBySideDiff {

	/**
	 * Side by side diff of two files
	 * 
	 * @param original line array of original file
	 * @param revised line array of revised file
	 * 
	 * @return JSON array which for every line has an object with the keys myContent, myType, theirContent, theirType
	 *
	 */
	@SuppressWarnings("unchecked")
	public static JSONArray diff(List<String> original, List<String> revised) throws Exception {
		
		// Using DiffUtils to create a diff patch
	    Patch patch = DiffUtils.diff(original, revised);
	    
	    // Initializing myContent, theirContent (copy of myContent), mytype, theirType
	    List<String> myContent = new LinkedList<String>(original);
	    List<String> theirContent = new LinkedList<String>(original);
	    List<String> myType = new LinkedList<String>();
	    List<String> theirType = new LinkedList<String>();
	    for (int i = 0; i < myContent.size(); i++) {
	    	myType.add("UNCHANGED");
	    	theirType.add("UNCHANGED");
	    }
	    
	    // After adding lines, position references from the patch shift by some offset
	    int offset = 0;
	    
	    // Go through all patches and create a nice side-by-side displayable version
	    for (Delta delta : patch.getDeltas()) {
	    	
	    	// What type of patch is it?
	    	if (delta.getType() == TYPE.CHANGE) {
	    		
	    		// Position
	    		int pos = delta.getOriginal().getPosition() + offset;
	    		
	    		// Get my and their lines
	    		List<String> myLines = (List<String>) delta.getOriginal().getLines();
	    		List<String> theirLines = (List<String>) delta.getRevised().getLines();
	    		
	    		// Max lines so we pad modified-blocks to be of the same size
	    		int maxLines = Math.max(myLines.size(), theirLines.size());
	    	
	    		// Do all the necessary changes to content and type
	    		for (int i = 0; i < maxLines; i++) {
	    			if (i < myLines.size()) {
	    				// My content is already right, no need to change anything, just set it as 'modified'
		    			myType.set(pos, "MODIFIED");
		    			// If we have more lines than them, set their content to empty strings
		    			if (i < theirLines.size()) {
			    			theirContent.set(pos, theirLines.get(i));
			    			theirType.set(pos, "MODIFIED");
		    			}
		    			else {
			    			theirContent.set(pos, "");
			    			theirType.set(pos, "MODIFIED_PAD");
		    			}
	    			}
	    			// They have more lines than me, we need to add new empty lines for me and their lines
	    			else {
		    			myType.add(pos, "MODIFIED_PAD");
		    			myContent.add(pos, "");
		    			theirType.add(pos, "MODIFIED");
		    			theirContent.add(pos, theirLines.get(i));
	    			}
	    			pos++;
	    		}
	    	}
	    	else if (delta.getType() == TYPE.DELETE) {
	    		
	    		// Position
	    		int pos = delta.getOriginal().getPosition() + offset;

	    		// Do all the necessary changes to content and type
	    		for (String line : (List<String>) delta.getOriginal().getLines()) {
	    			myContent.set(pos, line);
	    			myType.set(pos, "INSERT");
	    			theirContent.set(pos, "");
	    			theirType.set(pos, "PAD");
	    			pos++;
	    		}
	    		
	    	}
	    	else if (delta.getType() == TYPE.INSERT) {

	    		// Position
	    		int pos = delta.getOriginal().getPosition() + offset;

	    		// Do all the necessary changes to content and type
	    		for (String line : (List<String>) delta.getRevised().getLines()) {
	    			theirContent.add(pos, line);
	    			theirType.add(pos, "INSERT");
	    			myContent.add(pos, "");
	    			myType.add(pos, "PAD");
	    			pos++;
	    			offset++;
	    		}
	    	}
	    	
	    }
	    
	    // Build a JSON array from the side-by-side information we just created
	    JSONArray lineArray = new JSONArray();
	    for (int i = 0; i < myContent.size(); i++) {
	    	JSONObject lineObject = new JSONObject();
	    	lineObject.put("myContent", myContent.get(i));
	    	lineObject.put("myType", myType.get(i));
	    	lineObject.put("theirContent", theirContent.get(i));
	    	lineObject.put("theirType", theirType.get(i));
	    	lineArray.put(lineObject);
	    }
    
	    // Return it
    	return lineArray;
    	
	}

	/**
	 * Reads a file into a list of lines
	 * @param filename filename of the file to read
	 * @return list of lines
	 */
	public static List<String> fileToLines(String filename) {
        List<String> lines = new LinkedList<String>();
        String line = "";
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(
                new FileInputStream(filename), "UTF8"));
            while ((line = in.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // ignore ... any errors should already have been
                    // reported via an IOException from the final flush.
                }
            }
        }
        return lines;
	}
	
}
