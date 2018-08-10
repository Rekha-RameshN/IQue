package gui;

import java.io.File;
import java.util.Collection;
import java.util.Set;
import org.graphstream.graph.Edge;
import org.graphstream.graph.implementations.SingleGraph;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLRestriction;
import org.semanticweb.owlapi.search.EntitySearcher;
import process.Reader;
import static process.Reader.Id;
import static process.Reader.ontology;

/**
 *
 * @author Niraj Palecha <nirajftw@gmail.com>
 */
public class GraphHandler {

  private static Reader reader;

  /**
   * Constructor initializes Graph.
   *
   * @param owl
   * @throws OWLOntologyCreationException
   * @throws org.semanticweb.owlapi.model.OWLOntologyStorageException
   */
  public GraphHandler(File owl) throws OWLOntologyCreationException, OWLOntologyStorageException {

    reader = new Reader(owl);

  }

  /**
   * Adds all the nodes and edges in graph
   *
   * @param graph
   * @return
   * @throws OWLOntologyCreationException
   */
  public SingleGraph genGraph(SingleGraph graph) throws OWLOntologyCreationException {

    graph.setAutoCreate(true);
    graph.setStrict(false);
    Set<OWLClass> concepts = reader.getClasses();
    for (OWLClass concept : concepts) {
      //String _concept = concept.getIRI().getFragment();
      String _concept=Id(concept.getClassesInSignature().toString());
      graph.addNode(_concept);
      Collection<OWLClassExpression> subClasses = EntitySearcher.getSubClasses(concept, ontology);
      if (subClasses.size() > 0) {
        for (OWLClassExpression subClass : subClasses) {
          String _subClass = Id(subClass.getClassesInSignature().toString());
          graph.addEdge(_concept + _subClass, _concept, _subClass, true);
        }
      }
//         Relational Edges
      Collection<OWLClassExpression> related = EntitySearcher.getSuperClasses(concept, ontology);
      if (related.size() > 0) {
        for (OWLClassExpression classExp : related) {
          if (classExp instanceof OWLRestriction) {
            String _relatedTo = Id(classExp.getClassesInSignature().toString());
            graph.addEdge(_concept + _relatedTo, _concept, _relatedTo, true);
            Edge e = graph.getEdge(_concept + _relatedTo);
            String label = Id(classExp.getObjectPropertiesInSignature().toString());
            e.setAttribute("ui.label", label);
            e.addAttribute("ui.class", "relation");
          }
        }
      }

      graph.getNode(_concept).setAttribute("ui.label", reader.getLabel(concept));
    }
  return graph ;
}

//    public static void main(String[] args) throws OWLOntologyCreationException{
//      Graph g = new Graph();
//      g.genGraph();
//      graph.display();
//      
//    }
}
