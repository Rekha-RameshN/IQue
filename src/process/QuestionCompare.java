/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package process;

import java.util.HashMap;
import java.util.LinkedHashSet;

/**
 * Used to return program value calculated for alignment of Learning Objective
 * and Question
 *
 * Generates feedback for Trainer Stages
 *
 * @author Akshen Kadakia <akshenk8@gmail.com>
 */
public class QuestionCompare {

    private float ALPHA = 1.0f, BETA = 1.0f;
    private QuestionHandler qh;
    public static String L_Q = "L|Q",
            Q_L = "Q|L",
            LNQ = "LNQ",
            VAL = "val",
            L = "L",
            QU = "Q",
            FEEDBACK = "feedback",
            MODIFY_NEXT = "modify";

    public QuestionCompare(QuestionHandler qh) {
        this.qh = qh;
    }

    public QuestionCompare(float ALPHA, float BETA, QuestionHandler qh) {
        this.ALPHA = ALPHA;
        this.BETA = BETA;
        this.qh = qh;
    }

    /*
     *  Generate feedback for stages.
     */
    public HashMap<String, Object> feedback(Question LO, Question Q, boolean displayModifyText) {
        HashMap<String, Object> res = compare(LO, Q);
        HashMap<String, LinkedHashSet<String>> lnqMapping
                = (HashMap<String, LinkedHashSet<String>>) res.get(QuestionCompare.LNQ);
        String feed = "";
        int cogDiff = LO.getLevel() - Q.getLevel();
        int count = 1;
        Double per = ((Double) res.get(QuestionCompare.VAL)) * 100;
        String t = "";
        if (per >= 0 && per <= 35) {
            t = "low";
        } else if (per > 35 && per <= 70) {
            t = "medium";
        } else if (per > 70 && per <= 100) {
            t = "high";
        }
        feed += (count++) + ")There is " + t + " level content alignment.";

        boolean modifyNext = true;
        //cognitive level
        if (Math.abs(cogDiff) >= 1) {
            if (LO.getLevel() == CognitiveLevel.noMatchInt) {
                feed += "\n" + (count++) + ")Cognitive Level of LO cannot be determined";
            } else if (Q.getLevel() == CognitiveLevel.noMatchInt) {
                feed += "\n" + (count++) + ")Cognitive Level of Question cannot be determined";
            } else {
                feed += "\n" + (count++) + ")Cognitive Level of LO:\"" + LO.getLevelName() + "\""
                        + ",You have entered \"" + Q.getLevelName() + "\" level question";
            }
        } else {
            feed += "\n" + (count++) + ")Cognitive Level of LO and question is matching";
        }

        if (Math.abs(cogDiff) <= 1) {
            modifyNext = false;
        }

        //concepts
        if (per >= 0.0 && per <= 70.0) {
            LinkedHashSet<String> addToFeed = new LinkedHashSet<>();
            for (String c : LO.getClassifyConcepts().keySet()) {
                if (!lnqMapping.containsKey(c)) {
                    addToFeed.add(c);
                }
            }
            if (addToFeed.size() > 0) {
                feed += "\n" + (count++) + ")There are concepts from LO that are not covered in your Question:" + addToFeed.toString();
            }
            addToFeed.clear();
            for (String c : Q.getClassifyConcepts().keySet()) {
                boolean has = false;
                for (String l : lnqMapping.keySet()) {
                    if (lnqMapping.get(l).contains(c)) {
                        has = true;
                        break;
                    }
                }
                if (!has) {
                    addToFeed.add(c);
                }
            }
            if (addToFeed.size() > 0) {
                feed += "\n" + (count++) + ")There are concepts in your question that are "
                        + "outside the scope of LO:" + addToFeed.toString();
            }
            modifyNext = true;
        }

        if (per > 70.0 && per <= 100.0) {
            LinkedHashSet<String> addToFeed = new LinkedHashSet<>();
            for (String c : LO.getClassifyConcepts().keySet()) {
                if (!lnqMapping.containsKey(c)) {
                    addToFeed.add(c);
                }
            }
            if (addToFeed.size() > 0) {
                feed += "\n" + (count++) + ")Concepts not covered in Question:" + addToFeed.toString();
            } else if (!modifyNext && Math.abs(cogDiff) == 1) {
                feed += "\n" + (count++) + ")Question is almost aligned to the LO.";
            } else if (!modifyNext) {
                feed += "\n" + (count++) + ")Question is completely aligned to the LO.";
            }
        }

        if (modifyNext && displayModifyText) {
            feed += "\n" + (count++) + ")Modify the Question.";
        }
        HashMap<String, Object> res1 = new HashMap<>();
        res1.put(FEEDBACK, feed);
        res1.put(MODIFY_NEXT, modifyNext);
        return res1;
    }

    /*
     *  Calculate formula for stages
     */
    public HashMap<String, Object> compare(Question LO, Question Q) {
        LinkedHashSet<String> l = LO.getImplicitConcepts(),
                l_q = new LinkedHashSet<>(), q_l = new LinkedHashSet<>(),
                q = Q.getImplicitConcepts(), tmp;

        HashMap<String, LinkedHashSet<String>> lnqMapping = new HashMap<>(),
                lsuper = LO.getClassifyConcepts(),
                qsuper = Q.getClassifyConcepts();

        //lnq L|Q
        for (String s : lsuper.keySet()) {
            tmp = lsuper.get(s);
            LinkedHashSet<String> t = new LinkedHashSet<>(),
                    y = new LinkedHashSet<>(),
                    isP = new LinkedHashSet<>();
            y.add(s);
            isP.addAll(tmp);
            isP.add(s);

            for (String qConcept : qsuper.keySet()) {
                LinkedHashSet<String> t1 = new LinkedHashSet<>();
                t1.add(qConcept);
                t1.addAll(qsuper.get(qConcept));
                if (qh.qInLoHierarchy(y, t1)) {
                    t.add(qConcept);
                } else if (checkIsParent(qConcept, isP, qh)) {
                    t.add(qConcept);
                } else {
                    for (String x : t1) {
                        if (tmp.contains(x)) {
                            t.add(qConcept);
                            break;
                        }
                    }
                }
            }
            if (t.size() > 0) {
                lnqMapping.put(s, t);
            }
        }
        q_l.addAll(qsuper.keySet());
        l_q.addAll(lsuper.keySet());
        for (String h : lnqMapping.keySet()) {
            l_q.remove(h);
            for (String g : lnqMapping.get(h)) {
                q_l.remove(g);
            }
        }

        double lnqTotal = 0.0;
        HashMap<String, Double> impLoNodes = LO.getImportantNodes();
        for (String x : lnqMapping.keySet()) {
            if (impLoNodes.containsKey(x)) {
                lnqTotal += impLoNodes.get(x);
            } else {
                lnqTotal += 1;
            }
        }

        double l_qTotal = 0.0;
        for (String x : l_q) {
            if (impLoNodes.containsKey(x)) {
                l_qTotal += impLoNodes.get(x);
            } else {
                l_qTotal += 1;
            }
        }

        double q_lTotal = 0.0;
        for (String x : q_l) {
            if (impLoNodes.containsKey(x)) {
                q_lTotal += impLoNodes.get(x);
            } else {
                q_lTotal += 1;
            }
        }

        double num = lnqTotal;
        double deno = lnqTotal + ALPHA * q_lTotal + BETA * l_qTotal;
        double val = num / deno;
        if(Double.isNaN(val)){
            val=0.0;
        }
        HashMap<String, Object> res = new HashMap<>();
        res.put(LNQ, lnqMapping);
        res.put(L_Q, l_q);
        res.put(Q_L, q_l);
        res.put(L, l);
        res.put(QU, q);
        res.put(VAL, val);
        return res;
    }

    boolean checkIsParent(String qConcept, LinkedHashSet<String> los, QuestionHandler qh) {
        for (String l : los) {
            if (qh.isParent(l, qConcept)) {
                return true;
            }
        }
        return false;
    }
}
