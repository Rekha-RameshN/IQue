/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package process;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Niraj Palecha <nirajftw@gmail.com>
 */
public class Question implements Serializable {

    public Question(String statement) {
        this.statement = statement;
    }

    // Question
    private String statement;
    private List<String> lemma;
    private LinkedHashMap<String, String> tag;
    private String clean;

    // Getter and Setter Methods for Question
    public void setStatement(String statement) {
        this.statement = statement;
    }

    public String getStatement() {
        return statement;
    }

    public void setLemma(List<String> lemma) {
        this.lemma = lemma;
    }

    public List<String> getLemma() {
        return lemma;
    }

    public void setTag(LinkedHashMap<String, String> tag) {
        this.tag = tag;
    }

    public LinkedHashMap<String, String> getTag() {
        return tag;
    }

    public void setClean(String clean) {
        this.clean = clean;
    }

    public String getClean() {
        return clean;
    }

    // Cognitive Level
    private int level;
    private String levelName;
    private Set<String> actionVerbs;

    // Getter and Setter Methods for Cognitive Level
    public void setLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setActionVerbs(Set<String> actionVerbs) {
        this.actionVerbs = actionVerbs;
    }

    public Set<String> getActionVerbs() {
        return actionVerbs;
    }

    // Slot
    private boolean slot;
    private Set<String> slotIndicator;
    private LinkedHashSet<String> slotConnector;

    // Getter and Setter Methods for Slot
    public void setSlot(boolean slot) {
        this.slot = slot;
    }

    public boolean getSlot() {
        return slot;
    }

    public void setSlotIndicator(Set<String> slotIndicator) {
        this.slotIndicator = slotIndicator;
    }

    public Set<String> getSlotIndicator() {
        return slotIndicator;
    }

    public LinkedHashSet<String> getSlotConnector() {
        return slotConnector;
    }

    public void setSlotConnector(LinkedHashSet<String> slotConnector) {
        this.slotConnector = slotConnector;
    }

    // Relation
    private LinkedHashMap<String, List<String>> relation;

    // Getter and Setter Methods for Relation
    public void setRelation(LinkedHashMap<String, List<String>> relation) {
        this.relation = relation;
    }

    public LinkedHashMap<String, List<String>> getRelation() {
        return relation;
    }

    // Concepts
    private LinkedHashSet<String> explicit;
    private LinkedHashSet<String> implicit;

    //Getter and Setter Methods for Concepts
    public void setExplicitConcepts(LinkedHashSet<String> explicit) {
        this.explicit = explicit;
    }

    public LinkedHashSet<String> getExplicitConcepts() {
        return explicit;
    }

    public void setImplicitConcepts(LinkedHashSet<String> implicit) {
        this.implicit = implicit;
    }

    public LinkedHashSet<String> getImplicitConcepts() {
        return implicit;
    }

    //Trainer FeedBack
    private HashMap<String, LinkedHashSet<String>> classifyConcepts;
    private HashMap<String, Double> importantNodes;
    public double defaultImpNodeValue = 2.0;

    public HashMap<String, LinkedHashSet<String>> getClassifyConcepts() {
        return classifyConcepts;
    }

    public void setClassifyConcepts(HashMap<String, LinkedHashSet<String>> classifyConcepts) {
        this.classifyConcepts = classifyConcepts;
    }

    public HashMap<String, Double> getImportantNodes() {
        return importantNodes;
    }

    public void setImportantNodes(HashMap<String, Double> importantNodes) {
        this.importantNodes = importantNodes;
    }

    @Override
    public String toString() {
        return "Question{" + "statement=" + statement + ", lemma=" + lemma + ", tag=" + tag
                + ", clean=" + clean + ", level=" + level + ", levelName=" + levelName
                + ", actionVerbs=" + actionVerbs + ", slot=" + slot + ", slotIndicator="
                + slotIndicator + ", slotConnector=" + slotConnector + ", relation=" + relation
                + ", explicit=" + explicit + ", implicit=" + implicit + ", classifyConcepts="
                + classifyConcepts + ", importantNodes=" + importantNodes
                + ", defaultImpNodeValue=" + defaultImpNodeValue + '}';
    }

}
