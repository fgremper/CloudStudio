package ch.ethz.fgremper.rtca;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

public class JSONHelper {
	public static List<String> jsonArrayToArray(JSONArray jsonArray) {
		ArrayList<String> list = new ArrayList<String>();
		try {
			int l = jsonArray.length();
			for (int i = 0; i < l; i++) {
				list.add(jsonArray.get(i).toString());
			}
		}
		catch (Exception e) {
			
		}
		return list;
	}
}
