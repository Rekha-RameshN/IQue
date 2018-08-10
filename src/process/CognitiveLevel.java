package process;

import edu.stanford.nlp.util.ArraySet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Niraj Palecha <nirajftw@gmail.com>
 * Stores cognitive level and verbs for LOs and questions in the Bucket object.
 */
public class CognitiveLevel {

    //not found
    public static final String noMatchString = "No Match";
    public static final int noMatchInt = 100;

    public static final String[] levelsName = {"Recall", "Understand", "Apply", "Analyze", "Evaluate", "Create"};

    //Bloom's Taxonomy Identifiers(put only single verbs)
    private static final String[] recall = {"choose", "define", "how", "label", "list", "match", "name", "omit", "recall", "relate", "select", "show", "spell", "tell", "what", "when", "where", "which", "who", "why"};
    private static final String[] understand = {"classify", "contrast", "describe", "demonstrate", "discuss", "explain", "extend", "illustrate", "infer", "interpret", "outline", "relate", "rephrase", "show", "summarize", "translate", "understand"};
    private static final String[] apply = {"apply", "append", "build", "choose", "construct", "create", "delete", "develop", "display", "experiment with", "identify", "implement", "insert", "interview", "print", "model", "merge", "organize", "plan","perform", "program", "select", "solve", "sort", "traverse", "use", "utilize", "find", "calculate", "write"};
    private static final String[] analyze = {"analyze", "assume", "categorize", "check", "classify", "compare", "conclusion", "contrast", "discover", "dissect", "distinguish", "divide", "determine", "differentiate", "examine", "inference", "inspect", "motive", "relationships", "simplify", "survey", "theme"};
    private static final String[] evaluate = {"agree", "appraise", "assess", "award", "choose", "conclude", "criteria", "criticize", "decide", "deduct", "defend", "disprove", "estimate", "evaluate",  "importance", "influence", "interpret", "judge", "justify", "mark", "measure", "opinion", "perceive", "prioritize", "prove", "rate", "recommend", "select", "support", "suggest"};
    private static final String[] create = {"adapt", "build", "combine", "compile", "compose", "design", "develop", "elaborate", "estimate", "formulate", "happen", "imagine", "improve", "invent", "maximize", "minimize", "original", "originate", "plan", "predict", "propose", "solution", "test", "theory", "maximize"};


    private static final String[][] Blooms = {recall, understand, apply, analyze, evaluate, create};

    //Non-pos tagging action verbs for direct matching
    private static final String[] recallQI = {};
    private static final String[] understandQI = {};
    private static final String[] applyQI = {"make use of"};
    private static final String[] analyzeQI = {"in what order","how many", "explain why","take part in", "test for"};
    private static final String[] evaluateQI = {"what is the possibility", "under what circumstances","select"};
    private static final String[] createQI = {};

    private static final String[][] questionIndicators = {recallQI, understandQI, applyQI, analyzeQI, evaluateQI, createQI};

    /**
     * Identifies verbs from map using PennTreeBank definition and identifiers
     * of verbs.
     *
     * @param map
     * @return verbs
     */
    public static Set<String> findVerbs(Map<String, String> map) {

        Set<String> lo_verbs = new ArraySet();
        map.entrySet().stream().forEach((entry) -> {
            String tag = entry.getValue();
            if (Tags.verbsTags.contains(tag)) {
                lo_verbs.add(entry.getKey());
            }
        });
        return lo_verbs;
    }

    /**
     * Returns Int value which denotes location of level object in array Blooms.
     * Checks each verb from List verbs in Lists of blooms Taxonomy.
     *
     * @param q
     * @return
     */
    public static ArrayList findLevel(Question q) {
        //boolean notfound = true;
        int currentLevel = -1;
        Set<String> verbs = q.getActionVerbs();
        //Blooms matching
        for (String verb : verbs) {
            for (int i = Blooms.length - 1; i > currentLevel; i--) {
                if (Arrays.asList(Blooms[i]).contains(verb) && i > currentLevel) {
                    currentLevel = i;
                    //notfound = false;
                }
            }
//            System.out.println(verb+" "+levelsName[currentLevel]);
        }
        String stClean = q.getClean();
        if (currentLevel < 2) {
            if (stClean.contains("model")) {
                currentLevel = 2;
                //notfound=false;
            }
        }
        if (currentLevel < 2) {
            String stN = q.getStatement();
            String[] search = {"program", "algorithm", "function", "adt"};
            int next = 0;
            boolean found = true;
            while (found) {
                int w = stClean.indexOf("write", next);
                if (w != -1) {
                    for (int i = 0; i < search.length; i++) {
                        //check from lowerboundary
                        int x = stClean.indexOf(search[i], w);

                        if (x != -1) {
                            int y = stN.length();
                            //upper boundaries [: ; , .] 
                            String breaks[] = {";", ":", ",", "."};
                            for (String k : breaks) {
                                int j = stN.indexOf(k, w);
                                if (j != -1) {
                                    y = Math.min(y, j);
                                }
                            }
                            if (y == -1 || x < y) {
                                currentLevel = 2;
                                found = false;
                                //notfound=false;
                                break;
                            }
                        }
                    }
                    next = w + 1;
                } else {
                    break;
                }
            }
        }

        //direct matching
        for (int i = questionIndicators.length - 1; i > currentLevel; i--) {
            for (String verb : questionIndicators[i]) {
                if (stClean.contains(verb) && i > currentLevel) {
                    currentLevel = i;
                    //notfound = false;
                }
            }
        }

        ArrayList level = new ArrayList();
        if (currentLevel == -1) {
            level.add(noMatchInt);
            level.add(noMatchString);
        } else {
            level.add(currentLevel + 1);
            level.add(levelsName[currentLevel]);
        }
        return level;
    }

    public static int getLevelNumber(String levelName) {
        int level = 0;
        for (int i = 0; i < levelsName.length; i++) {
            if (levelsName[i].equals(levelName)) {
                level = i + 1;
            }
        }
        return level;
    }
}
