package process;

import edu.stanford.nlp.util.ArraySet;
import java.util.Set;

/**
 *
 * @author Niraj Palecha <nirajftw@gmail.com>
 */
public class Slot {

	public static void findSlot(Question question){
		Set<String> slotIndicator = new ArraySet();
		String lo = question.getStatement();
		for(String adjective:Tags.adjectives){
			if(lo.contains(adjective)){
				slotIndicator.add(adjective);
			}
		}
		question.setSlotIndicator(slotIndicator);
		if (slotIndicator.size()>0)
			question.setSlot(true);
		else
			question.setSlot(false);
	}
        
}