package process;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.vocab.SKOSVocabulary;

/**
 * Reads owl file, extracts concepts and relations.
 *
 * @author Niraj Palecha <nirajftw@gmail.com>
 */
public class Reader {
//    private static final Logger logger = LogManager.getLogger(GeneralParser.class);

    public static OWLOntology ontology;
    protected OWLOntologyManager ontologyManager;
    private static OWLDataFactory df;
    private static String base2;
    public HashMap<String, List> conceptsPlus;
    public HashMap<String, LinkedHashSet<String>> conceptsClassification, isAClassification;
    private static Set<OWLClass> classes;
    public static final SKOSVocabulary ALTLABEL = SKOSVocabulary.ALTLABEL;

    public Reader(File owlFile) throws OWLOntologyCreationException, OWLOntologyStorageException {
        conceptsPlus = new HashMap();
        conceptsClassification = new HashMap();
        isAClassification = new HashMap();
        ontology = load(owlFile);
        df = ontologyManager.getOWLDataFactory();

        // Outputs Ontology to console in RDF-JSON format.
//        ontologyManager.saveOntologyf(ontology, new RDFJsonDocumentFormat(),System.out); 
        classes = ontology.getClassesInSignature();
        base2 = ontology.getOntologyID().getOntologyIRI().get().toString() + "#";
        extractConcepts();

        //classify calculations
        LinkedHashSet<String> setClasses = new LinkedHashSet();
        setClasses.add("Domain");//root
        calculateClassifications(setClasses);
    }

    /**
     * Load OWLOntologyManager
     *
     * @param manager
     * @param file
     * @return
     * @throws OWLOntologyCreationException
     */
    private OWLOntology load(File file) throws OWLOntologyCreationException {
        ontologyManager = OWLManager.createOWLOntologyManager();
        return ontologyManager.loadOntologyFromOntologyDocument(file);
    }

    /**
     * Extract fragment from identifier.
     *
     * @param identifier
     * @return
     */
    public static String Id(String identifier) {
        int slash = identifier.lastIndexOf("/");
        int arrow = identifier.lastIndexOf(">");
        int doubleDot = identifier.lastIndexOf(":");
        int hash = identifier.lastIndexOf("#");

        int lastCut = -1;
        int firstCut = -1;

        if (arrow != -1) {
            lastCut = arrow;
        }

        if (slash != -1 && firstCut < slash) {
            firstCut = slash;
        }

        if (doubleDot != -1 && firstCut < doubleDot) {
            firstCut = doubleDot;
        }

        if (hash != -1 && firstCut < hash) {
            firstCut = hash;
        }

        String result;

        if (firstCut != -1 && lastCut != -1) {
            result = identifier.substring(firstCut + 1, lastCut);
        } else if (firstCut != -1) {
            result = identifier.substring(firstCut + 1);
        } else if (lastCut != -1) {
            result = identifier.substring(0, lastCut);
        } else {
            result = identifier;
        }

        return result;
    }

    private void extractConcepts() {
        for (OWLClass node : classes) {
            String concept = "";
            //String iri = node.getIRI().getFragment();
            String iri = Id(node.getClassesInSignature().toString());
//          System.out.println(Id(node.toString()) + " " + EntitySearcher.getSubClasses(node, ontology).toString());
            List<String> annotations = new ArrayList();
            for (OWLAnnotation anno : EntitySearcher.getAnnotations(node, ontology, df.getRDFSLabel())) {
                if (anno.getProperty().isLabel()) {
                    if (anno.getValue() instanceof OWLLiteral) {
                        OWLLiteral val = (OWLLiteral) anno.getValue();
                        concept = val.getLiteral().replaceAll("[^a-zA-Z ]", "").toLowerCase();
                        annotations.add(concept);
                    }
                }
//                related = EntitySearcher.getSuperClasses(node, ontology);
//
//                for (OWLClassExpression classExp : related) {
//
//                    if (classExp instanceof OWLRestriction) {
////						System.out.println(Id(node.toString()));
////						System.out.println(Id(classExp.getObjectPropertiesInSignature().toString()));
////						System.out.println(Id(classExp.getClassesInSignature().toString()));
////						System.out.println();
//                    }
//                }
            }
            for (OWLAnnotation alt : EntitySearcher.getAnnotations(node, ontology, df.getOWLAnnotationProperty(ALTLABEL.getIRI()))) {
                if (alt.getValue() instanceof OWLLiteral) {
                    OWLLiteral val = (OWLLiteral) alt.getValue();
                    String tmp = val.getLiteral().replaceAll("[^a-zA-Z ]", "").toLowerCase();
                    annotations.add(tmp);
                }
            }
            conceptsPlus.put(iri, annotations);
        }
    }

    /**
     * public method to access label of a concept(class)
     *
     * @param concept
     * @return label
     */
    public String getLabel(OWLClass concept) {
        String label = "";
        for (OWLAnnotation anno : EntitySearcher.getAnnotations(concept, ontology, df.getRDFSLabel())) {
            if (anno.getProperty().isLabel()) {
                if (anno.getValue() instanceof OWLLiteral) {
                    OWLLiteral val = (OWLLiteral) anno.getValue();
                    label = val.getLiteral();
                }
            }
        }
        return label;
    }

    /**
     * public method to access all classes(concepts) in ontology.
     *
     * @return
     */
    public Set<OWLClass> getClasses() {
        return classes;
    }

    public final LinkedHashSet<String> traverser(Question q) {
        LinkedHashSet<String> concepts = new LinkedHashSet<>();//q.getExplicitConcepts();
        LinkedHashSet<String> nextConcepts = new LinkedHashSet<>();

        for (String iri : q.getExplicitConcepts()) {
            OWLClass concept = df.getOWLClass(IRI.create(base2 + iri));
            nextConcepts.addAll(relation_traversal(concept, q.getRelation(), true));
        }

        if (q.getSlot() && q.getSlotConnector().size() > 0) {
            LinkedHashSet<String> slotConnector = q.getSlotConnector();
            concepts.addAll(slotConnector);

            //if slot add "isA" change date:29/10/2016:"to solve AI view problem"
            LinkedHashMap<String, List<String>> relations = new LinkedHashMap<>();
            relations.putAll(q.getRelation());
            relations.put("isA", new ArrayList<String>());

            concepts = traverse(concepts, new LinkedHashSet<>(), relations);
            for (String iri : concepts) {
                OWLClass concept = df.getOWLClass(IRI.create(base2 + iri));

                //if (!slotConnector.contains(iri)) {
                nextConcepts.addAll(relation_traversal(concept, relations, false));
                //}
            }            
        }
        concepts.addAll(nextConcepts);
        return concepts;
    }

    // Worst possible way to implement this.
    private LinkedHashSet<String> traverse(Set<String> base, LinkedHashSet<String> more, LinkedHashMap<String, List<String>> relation) {
        for (String iri : base) {
            OWLClass concept = df.getOWLClass(IRI.create(base2 + iri));
            Collection<OWLClassExpression> subClasses = EntitySearcher.getSubClasses(concept, ontology);
//            System.out.println("SubClasses: " + subClasses);
            LinkedHashSet<String> moreTmp = new LinkedHashSet();
            if (subClasses.size() > 0) {
                Set<String> setClasses = new LinkedHashSet();
                for (OWLClassExpression subClass : subClasses) {
                    //setClasses.add(subClass.asOWLClass().getIRI().getFragment());
                    setClasses.add(Id(subClass.getClassesInSignature().toString()));
                }
                setClasses.addAll(relation_traversal(concept, relation, false));

                moreTmp.addAll(setClasses);
                moreTmp.addAll(traverse(setClasses, moreTmp, relation));
            }
            more.add(iri);
            more.addAll(moreTmp);
        }
        return more;
    }

    private LinkedHashSet<String> relation_traversal(OWLClass concept, LinkedHashMap<String, List<String>> relation, boolean explicit) {
        LinkedHashSet<String> relatedSet = new LinkedHashSet<>();
        Collection<OWLClassExpression> related = EntitySearcher.getSuperClasses(concept, ontology);
        if (related.size() > 0) {
            for (OWLClassExpression classExp : related) {
                if (classExp instanceof OWLRestriction) {
                    String _relatedTo = Id(classExp.getClassesInSignature().toString());
                    String label = Id(classExp.getObjectPropertiesInSignature().toString());
                    if (relation.keySet().contains(label) || ("includes".equals(label) && explicit)) {
                        relatedSet.add(_relatedTo);
                    }
                }
            }
        }
        if (relation.containsKey("isA")) {    
            String con=Id(concept.getClassesInSignature().toString());
            if (isAClassification.containsKey(con)) {
                relatedSet.addAll(isAClassification.get(con));
            }
        }
        return relatedSet;
    }

    //find super-sub relation for trainer
    public HashMap<String, LinkedHashSet<String>> findClassifyConcepts(Question q) {
        LinkedHashSet<String> concepts = q.getImplicitConcepts();
        HashMap<String, LinkedHashSet<String>> classify = new HashMap<>();

        for (String subConcept : concepts) {
            String parent = null;
            OWLClass con = df.getOWLClass(IRI.create(base2 + subConcept));
            Collection<OWLClassExpression> related = EntitySearcher.getSuperClasses(con, ontology);
            if (related.size() > 0) {
                for (OWLClassExpression classExp : related) {
                    String _relatedTo = Id(classExp.getClassesInSignature().toString());
                    String label = Id(classExp.getObjectPropertiesInSignature().toString());
                    if ("[]".contains(label) && concepts.contains(_relatedTo)) {
                        parent = _relatedTo;
                        break;
                    }
                }
            }
            LinkedHashSet<String> sub = new LinkedHashSet<>();
            if (parent != null) {
                sub.add(subConcept);
                if (classify.containsKey(subConcept)) {
                    sub.addAll(classify.get(subConcept));
                    if (classify.containsKey(parent)) {
                        sub.addAll(classify.get(parent));
                    }
                    classify.remove(subConcept);
                    classify.put(parent, sub);
                } else {
                    if (classify.containsKey(parent)) {
                        sub.addAll(classify.get(parent));
                        classify.replace(parent, sub);
                    } else {
                        boolean exists = false;
                        for (String x : classify.keySet()) {
                            LinkedHashSet<String> f = classify.get(x);
                            if (f.contains(parent)) {
                                exists = true;
                                sub.addAll(f);
                                classify.replace(x, sub);
                                break;
                            }
                        }

                        if (!exists) {
                            classify.put(parent, sub);
                        }
                    }
                }
            } else {
                classify.put(subConcept, sub);
            }
        }

        return classify;
    }

    //find isA relation
    public String findIsAParent(String concept) {
        String parent = null;
        OWLClass con = df.getOWLClass(IRI.create(base2 + concept));
        Collection<OWLClassExpression> related = EntitySearcher.getSuperClasses(con, ontology);
        if (related.size() > 0) {
            for (OWLClassExpression classExp : related) {
                String _relatedTo = Id(classExp.getClassesInSignature().toString());
                String label = Id(classExp.getObjectPropertiesInSignature().toString());
                if ("isA".contains(label)) {
                    parent = _relatedTo;
                    break;
                }
            }
        }
        return parent;
    }

    /*
     * stores the classification in conceptsClassification
     */
    public void calculateClassifications(LinkedHashSet<String> base) {
        for (String b : base) {
            OWLClass concept = df.getOWLClass(IRI.create(base2 + b));
            Collection<OWLClassExpression> subClasses = EntitySearcher.getSubClasses(concept, ontology);
            if (subClasses.size() > 0) {
                LinkedHashSet<String> setClasses = new LinkedHashSet();
                for (OWLClassExpression subClass : subClasses) {
                    setClasses.add(Id(subClass.getClassesInSignature().toString()));
                }
                conceptsClassification.put(b, setClasses);
                calculateClassifications(setClasses);
            } else {
                conceptsClassification.put(b, new LinkedHashSet<>());
            }
            String p = findIsAParent(b);
            if (p != null) {
                if (isAClassification.containsKey(p)) {
                    LinkedHashSet<String> setClasses = isAClassification.get(p);
                    setClasses.add(b);
                    isAClassification.put(p, setClasses);
                } else {
                    LinkedHashSet<String> setClasses = new LinkedHashSet();
                    setClasses.add(b);
                    isAClassification.put(p, setClasses);
                }
            }
        }
    }
}
