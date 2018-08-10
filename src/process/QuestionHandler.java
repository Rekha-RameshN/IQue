/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package process;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import static process.Slot.findSlot;

/**
 *
 * @author Niraj Palecha <nirajftw@gmail.com>
 */
public class QuestionHandler {

    public final ArrayList<Question> los, questions;
    private final Pipeline pipe;
    private final File losFile, owlFile, qFile;
    public int losSize, qSize;
    private String cleanLO;
    private Concepts concepts;
    private HashMap<String, LinkedHashSet<String>> classification;

    public ArrayList<Question> outputList() {
        return los;
    }

    public HashMap<String, LinkedHashSet<String>> getClassification() {
        return concepts.getClassification();
    }

    public QuestionHandler(File Lo, File owlFile) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
        pipe = new Pipeline();
        los = new ArrayList<>();
        this.losFile = Lo;
        this.owlFile = owlFile;
        this.qFile = null;
        questions = null;
        List<String> statements = Files.readAllLines(Paths.get(losFile.toString()));
        concepts = new Concepts(owlFile);
        populate(statements, los);
    }

    public QuestionHandler(File lo, File q, File owl) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
        this.losFile = lo;
        this.owlFile = owl;
        this.qFile = q;
        questions = new ArrayList();
        los = new ArrayList();
        pipe = new Pipeline();
        concepts = new Concepts(owlFile);
        populate(losFile, los);
        populate(qFile, questions);
        losSize = los.size();
        qSize = questions.size();
    }

    public QuestionHandler(List<String> Lo, File owlFile) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
        pipe = new Pipeline();
        los = new ArrayList<>();
        questions = null;
        this.owlFile = owlFile;
        this.losFile = null;
        this.qFile = null;
        List<String> statements = Lo;
        concepts = new Concepts(owlFile);
        populate(statements, los);
    }

    private void populate(File file, ArrayList<Question> questions) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
        List<String> statements = Files.readAllLines(Paths.get(file.toString()));
        populate(statements, questions);
    }

    private void populate(List<String> statements, ArrayList<Question> questions) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
        Relation relations = new Relation();
        for (String statement : statements) {
            if (!statement.trim().equals("")) {
                Question question = new Question(statement);
                cleanLO = statement.replace("-", " ").replaceAll("[^a-zA-Z ]", " ").toLowerCase();
                question.setClean(cleanLO);
                question.setLemma(pipe.lemmatize(cleanLO));
                question.setTag((LinkedHashMap<String, String>) pipe.posTagger(cleanLO));
                question.setActionVerbs(CognitiveLevel.findVerbs(question.getTag()));
                ArrayList level = CognitiveLevel.findLevel(question);
                question.setLevel((int) level.get(0));
                question.setLevelName((String) level.get(1));
                findSlot(question);
                relations.findRelations(question);
                concepts.findConcepts(question);
                if (question.getSlot()) {
                    question.setSlotConnector(findSlotConnector(question));                    
                }
                concepts.allConcepts(question);
                calculateClassification(question);
                question.setImportantNodes(new HashMap<>());
                questions.add(question);
            }
        }
        classification = getClassification();
    }

    public void calculateClassification(Question question) {
        concepts.findClassifyConcepts(question);
        concepts.findIsA(question);
    }

    public LinkedHashSet<String> findSlotConnector(Question question) {
        if (question.getSlot()) {
            HashMap<String, String> tags = question.getTag();
            ArrayList<String> NN = new ArrayList<>();
            NN.addAll(Tags.nounTags);
            LinkedHashSet<String> slotConnector = new LinkedHashSet();
            LinkedHashSet<String> explicit = question.getExplicitConcepts();

            for (String slot : question.getSlotIndicator()) {
                if (slot.equals("such as")) {
                    int slotIndex = cleanLO.indexOf(slot);
                    int min = Integer.MIN_VALUE, size = 0;

                    for (String tag : tags.keySet()) {
                        if (NN.contains(tags.get(tag))) {
                            int nounIndex = cleanLO.indexOf(tag);
                            if (nounIndex < slotIndex && min < nounIndex) {
                                min = nounIndex;
                                size = tag.length();
                            }
                        }
                    }
                    int rangeHigh = min + size;
                    for (String concept : explicit) {
                        String normalisedConcept = concept.toLowerCase().replace("_", " ");
                        int conceptIndex = cleanLO.indexOf(normalisedConcept);
                        if (conceptIndex == -1) {
                            List<String> conceptAnnotations = concepts.conceptsMap.get(concept);
                            for (String conceptAnnotation : conceptAnnotations) {
                                conceptIndex = cleanLO.indexOf(conceptAnnotation);
                                if (conceptIndex != -1) {
                                    break;
                                }
                            }
                        }
                        if (conceptIndex < rangeHigh) {
                            slotConnector.add(concept);
                        }
                    }
                } else {
                    int slotIndex = cleanLO.indexOf(slot);
                    int min = Integer.MAX_VALUE, size = 0;

                    for (String tag : tags.keySet()) {
                        if (NN.contains(tags.get(tag))) {
                            int nounIndex = cleanLO.indexOf(tag);
                            if (nounIndex > slotIndex && min > nounIndex) {
                                min = nounIndex;
                                size = tag.length();
                            }
                        }
                    }
                    int rangeHigh = min + size;
                    for (String concept : explicit) {
                        String normalisedConcept = concept.toLowerCase().replace("_", " ");
                        int conceptIndex = cleanLO.indexOf(normalisedConcept);
                        if (conceptIndex == -1) {
                            List<String> conceptAnnotations = concepts.conceptsMap.get(concept);
                            for (String conceptAnnotation : conceptAnnotations) {
                                conceptIndex = cleanLO.indexOf(conceptAnnotation);
                                if (conceptIndex != -1) {
                                    break;
                                }
                            }
                        }
                        if (conceptIndex > slotIndex && conceptIndex < rangeHigh) {
                            slotConnector.add(concept);
                        }
                    }
                }
            }
            return slotConnector;
        } else {
            return new LinkedHashSet();
        }
    }

    public boolean qInLoHierarchy(LinkedHashSet<String> LO, LinkedHashSet<String> Q) {
        for (String k : Q) {
            if (LO.contains(k)) {
                return true;
            }
        }
        for (String l : LO) {
            LinkedHashSet<String> tmp = new LinkedHashSet<>();
            tmp.addAll(classification.get(l));
            if (qInLoHierarchy(tmp, Q)) {
                return true;
            }
        }
        return false;
    }

    public boolean isParent(String parent, String child) {
        return concepts.isParent(parent, child);
    }
}
