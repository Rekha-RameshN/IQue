package process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Niraj Palecha <nirajftw@gmail.com>
 */
public class Relation {
	private static Map<String,String[]> relationMap;
        public static HashMap<String,String> relations;
	public Relation(){
		relationMap = new HashMap();
                relations=new HashMap<>();
                relations.put("hasRepresentation","Representation");
                relations.put("hasApplication","Application");
                relations.put("hasOperation","Operation");                
                // TODO Make this dynamic(Extract this from the ontology)
		relationMap.put("hasRepresentation",new String[]{"represents","representation","represent","represented"});
		relationMap.put("hasApplication",new String[]{"apply","using","use","use of","application"});
		relationMap.put("hasOperation",new String[]{"operation","methods of"});
		
	}

	public void findRelations(Question question){
		String statement = question.getStatement();
		LinkedHashMap<String,List<String>> currentRelations = new LinkedHashMap();
		for(String key:relationMap.keySet()){
			List<String> indicators = new ArrayList();
			for (String indicator:relationMap.get(key)){
				if (statement.contains(indicator)) {
					indicators.add(indicator);
				}
			}
			if (indicators.size() > 0) {
				currentRelations.put(key,indicators);
			}
		}
		question.setRelation(currentRelations);
	}

}
