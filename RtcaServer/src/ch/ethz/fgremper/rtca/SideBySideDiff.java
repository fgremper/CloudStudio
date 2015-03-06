package ch.ethz.fgremper.rtca;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import difflib.Delta.TYPE;

public class SideBySideDiff {

	private static final Logger log = LogManager.getLogger(SideBySideDiff.class);
	
	@SuppressWarnings("unchecked")
	public static JSONArray diff(List<String> original, List<String> revised) throws Exception {
		
	    Patch patch = DiffUtils.diff(original, revised);
	    
	    List<String> myContent = new LinkedList<String>(original);
	    List<String> theirContent = new LinkedList<String>(original);
	    List<String> myType = new LinkedList<String>();
	    List<String> theirType = new LinkedList<String>();
	    
	    for (int i = 0; i < myContent.size(); i++) {
	    	myType.add("unchanged");
	    	theirType.add("unchanged");
	    }
	    
	    int offset = 0;
	    for (Delta delta : patch.getDeltas()) {
	    	if (delta.getType() == TYPE.CHANGE) {
	    		int pos = delta.getOriginal().getPosition() + offset;
	    		List<String> myLines = (List<String>) delta.getOriginal().getLines();
	    		List<String> theirLines = (List<String>) delta.getRevised().getLines();
	    		int maxLines = Math.max(myLines.size(), theirLines.size());
	    	
	    		for (int i = 0; i < maxLines; i++) {
	    			if (i < myLines.size()) {
	    				// my content is already right, no need to change anything, just set it as 'modified'
		    			myType.set(pos, "modified");
		    			// if we have more lines than them, set their content to empty strings
		    			if (i < theirLines.size()) {
			    			theirContent.set(pos, theirLines.get(i));
			    			theirType.set(pos, "modified");
		    			}
		    			else {
			    			theirContent.set(pos, "");
			    			theirType.set(pos, "modifiedpad");
		    			}
	    			}
	    			// they have more lines than me, we need to add new empty lines for me and their lines
	    			else {
		    			myType.add(pos, "modifiedpad");
		    			myContent.add(pos, "");
		    			theirType.add(pos, "modified");
		    			theirContent.add(pos, theirLines.get(i));
	    			}
	    			pos++;
	    		}
	    	}
	    	if (delta.getType() == TYPE.DELETE) {
	    		int pos = delta.getOriginal().getPosition() + offset;
	    		for (String line : (List<String>) delta.getOriginal().getLines()) {
	    			//their.(pos, "+" + line);
	    			myContent.set(pos, line);
	    			myType.set(pos, "add");
	    			theirContent.set(pos, "");
	    			theirType.set(pos, "pad");
	    			pos++;
	    		}
	    	}
	    	if (delta.getType() == TYPE.INSERT) {
	    		int pos = delta.getOriginal().getPosition() + offset;
	    		for (String line : (List<String>) delta.getRevised().getLines()) {
	    			theirContent.add(pos, line);
	    			theirType.add(pos, "add");
	    			myContent.add(pos, "");
	    			myType.add(pos, "pad");
	    			pos++;
	    			offset++;
	    		}
	    	}
	    	
	    }
	    
	    JSONArray lineArray = new JSONArray();
	    for (int i = 0; i < myContent.size(); i++) {
	    	JSONObject lineObject = new JSONObject();
	    	lineObject.put("myContent", myContent.get(i));
	    	lineObject.put("myType", myType.get(i));
	    	lineObject.put("theirContent", theirContent.get(i));
	    	lineObject.put("theirType", theirType.get(i));
	    	lineArray.put(lineObject);
	    }
    
    	return lineArray;
    	
	}

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
