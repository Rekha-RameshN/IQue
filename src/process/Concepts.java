package process;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

/**
 * Finds and Stores concepts in lists from sentences Main Concepts and Sub
 * Concepts.
 *
 * @author Niraj Palecha <nirajftw@gmail.com>
 * @author Akshen Kadakia <akshenk8@gmail.com>
 */
public class Concepts {

    public HashMap<String, List> conceptsMap = new HashMap();
    private final Reader reader;

    /**
     * Initialize Concepts object with Reader
     *
     * @param owlFile
     * @throws org.semanticweb.owlapi.model.OWLOntologyCreationException
     * @throws org.semanticweb.owlapi.model.OWLOntologyStorageException
     */
    public Concepts(File owlFile) throws OWLOntologyCreationException, OWLOntologyStorageException {
        reader = new Reader(owlFile);
        this.conceptsMap = reader.conceptsPlus;
    }

    public void addConcepts(HashMap concepts) {
        conceptsMap = concepts;
    }

    public HashMap<String, LinkedHashSet<String>> getClassification() {
        return reader.conceptsClassification;
    }

    /**
     * Finds concepts by search all explicit concepts in LO/Question
     *
     * @param question
     */
    public void findConcepts(Question question) {

        String statement = question.getClean();
        LinkedHashSet<String> concepts_lo = new LinkedHashSet();
        HashMap<String, String> con = new HashMap<>();

        LinkedList<String> lemmas = (LinkedList<String>) question.getLemma();
        String lemmaStatement = String.join(" ", lemmas);

        for (int i = 1; i <= 4; i++) {
            //ngram on lemma
            ArrayList<String> ngrams1 = ngrams(i, lemmaStatement);
            //Ngram on statement
            ArrayList<String> ngrams2 = ngrams(i, statement);

            //check for Ngrams with Annontations
            for (String key : conceptsMap.keySet()) {
                List<String> annotations = conceptsMap.get(key);

                for (int j = 0; j < ngrams1.size() || j < ngrams2.size(); j++) {
                    if (j < ngrams1.size() && j < ngrams2.size()) {
                        String n1 = ngrams1.get(j);
                        String n2 = ngrams2.get(j);

                        boolean c1 = annotations.contains(n1);
                        boolean c2 = annotations.contains(n2);
                        if (c1 || c2) {
                            if (c1) {
                                con.put(n1, key);
                            } else {
                                con.put(n2, key);
                            }
                            //remove old
                            for (int k = 1; k < i; k++) {
                                ArrayList<String> tmpN = ngrams(k, n1);
                                for (String n : tmpN) {
                                    con.remove(n);
                                }
                                tmpN = ngrams(k, n2);
                                for (String n : tmpN) {
                                    con.remove(n);
                                }
                            }
                        }
                    } else if (j < ngrams1.size()) {
                        String ngram = ngrams1.get(j);
                        if (annotations.contains(ngram)) {
                            con.put(ngram, key);
                            //remove old
                            for (int k = 1; k < i; k++) {
                                ArrayList<String> tmpN = ngrams(k, ngram);
                                for (String n : tmpN) {
                                    con.remove(n);
                                }
                            }
                        }
                    } else if (j < ngrams2.size()) {
                        String ngram = ngrams2.get(j);
                        if (annotations.contains(ngram)) {
                            con.put(ngram, key);
                            //remove old
                            for (int k = 1; k < i; k++) {
                                ArrayList<String> tmpN = ngrams(k, ngram);
                                for (String n : tmpN) {
                                    con.remove(n);
                                }
                            }
                        }
                    } else {
                        break;
                    }
                }
            }
        }

        for (String c : con.keySet()) {
            concepts_lo.add(con.get(c));
        }
        //concepts_lo = cleanConcepts(concepts_lo);
        question.setExplicitConcepts(concepts_lo);
    }

    private LinkedHashSet<String> cleanConcepts(LinkedHashSet<String> concepts_lo) {
        LinkedHashSet<String> clean = new LinkedHashSet();
        for (String concept : concepts_lo) {
            clean.add(concept);
        }
        for (String conceptA : concepts_lo) {
            for (String conceptB : concepts_lo) {
                if (conceptA.contains(conceptB) && !conceptA.equals(conceptB)) {
                    clean.remove(conceptB);
                }
            }
        }
//        System.out.println(clean);

        return clean;
    }

    public void allConcepts(Question question) {
        LinkedHashSet<String> more = new LinkedHashSet();
        //if (question.getSlot()) {
        more = reader.traverser(question);
        //System.out.println("Inside allconcepts: " +more);
        //}
            /* else {
         more = question.getExplicitConcepts();
         }*/

        more.addAll(question.getExplicitConcepts());
        question.setImplicitConcepts(more);
    }

    //N-grams Methods
    public ArrayList<String> ngrams(int n, String str) {
        ArrayList<String> ngrams = new ArrayList<>();
        String[] words = str.split(" ");
        for (int i = 0; i < words.length - n + 1; i++) {
            ngrams.add(concat(words, i, i + n));
        }
        return ngrams;
    }

    public String concat(String[] words, int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++) {
            sb.append((i > start ? " " : "") + words[i]);
        }
        return sb.toString();
    }

    //Trainer FeedBack Hierarchy finder
    HashMap<String, LinkedHashSet<String>> rc;
    HashMap<String, LinkedHashSet<String>> classify;
    LinkedHashSet<String> implicit;

    public void findClassifyConcepts(Question q) {
        classify = new HashMap();//reader.findClassifyConcepts(q);        
        rc = reader.conceptsClassification;

        LinkedHashSet<String> top = new LinkedHashSet();
        top.addAll(rc.get("Domain"));
        implicit = q.getImplicitConcepts();
        for (String base : top) {
            checkClassify(base);
        }

        q.setClassifyConcepts(classify);
        classify = null;
        rc = null;
        implicit = null;
    }

    private void checkClassify(String parent) {
        LinkedHashSet<String> more = new LinkedHashSet();
        if (implicit.contains(parent)) {
            for (String c : rc.get(parent)) {
                checkClassify(c);
                if (classify.containsKey(c)) {
                    more.add(c);
                    more.addAll(classify.remove(c));
                }
            }
            classify.put(parent, more);
        } else {
            for (String c : rc.get(parent)) {
                checkClassify(c);
            }
        }
    }

    //isA parent 
    public boolean isParent(String parent, String child) {
        return parent.equalsIgnoreCase(reader.findIsAParent(child));
    }

    public String isAParent(String child) {
        return reader.findIsAParent(child);
    }

    //isA relationship finder
    public void findIsA(Question q) {
        HashMap<String, LinkedHashSet<String>> classify = q.getClassifyConcepts();
        HashMap<String, String> extra = new HashMap<>();
        for (String key : classify.keySet()) {
            String parent = reader.findIsAParent(key);
            if (parent != null) {
                extra.put(key, parent);
            }
        }
        LinkedHashSet<String> implicit = q.getImplicitConcepts();
        rc = reader.conceptsClassification;
        for (String x : extra.keySet()) {
            String parent = extra.get(x);
            if (implicit.contains(parent)) {
                LinkedHashSet<String> tmp = new LinkedHashSet<>();
                tmp.add(x);
                tmp.addAll(classify.remove(x));
                String pc = null;
                if (implicit.contains(parent)) {
                    if (classify.keySet().contains(parent)) {
                        pc = parent;
                    } else {
                        for (String k : classify.keySet()) {
                            for (String c : classify.get(k)) {
                                if (c.equals(parent)) {
                                    pc = k;
                                    break;
                                }
                            }
                            if (pc != null) {
                                break;
                            }
                        }
                    }
                    tmp.addAll(classify.remove(pc));
                    classify.put(pc, tmp);
                } else {
                    classify.put(parent, tmp);
                }
            }
        }
        rc = null;
        q.setClassifyConcepts(classify);
    }
}
