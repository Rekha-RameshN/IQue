package gui.controllers;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.Initializable;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import javax.swing.JLabel;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.ProxyPipe;
import org.graphstream.ui.layout.HierarchicalLayout;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gui.Concept;
import gui.GraphHandler;
import gui.Main_old;
import gui.Stream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import javax.swing.JCheckBox;
import javax.swing.JToggleButton;
import org.graphstream.graph.Edge;
import org.graphstream.stream.file.FileSinkDGS;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceFactory;
import process.CognitiveLevel;

import process.QuestionHandler;

/**
 *
 * @author Niraj Palecha <nirajftw@gmail.com>
 */
public class ViewController implements Initializable {

    public Stage viewStage;
    public SingleGraph graph;
    private QuestionHandler qh;
    private final HierarchicalLayout layout = new HierarchicalLayout();
    private final FileSinkDGS out = new FileSinkDGS();
    public Viewer viewer;
    public View view;
    private boolean relations_visibility = true;
    private final HashMap<String, Integer> stats;
    private int loPartial = -1, qPartial = -1;
    public JFrame sideFrame;
    public JLabel total, LO, LOQ, nLOQ, blank;
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int screenHeight, screenWidth;
    private static final String[] numNames = {"", "one", "two", "three", "four", "five", "six"};

    public ViewController() {
        this.stats = new HashMap<>();
    }

    public void setStage(Stage stage) {
        viewStage = stage;
    }

    public void setQuestionHandler(QuestionHandler qh) {
        this.qh = qh;
    }

    public void setPartialState(int loPartial, int qPartial) {
        if (loPartial != -1) {
            this.loPartial = loPartial + 1;
        }
        this.qPartial = qPartial + 1;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {

            screenWidth = (int) screenSize.getWidth();
            screenHeight = (int) screenSize.getHeight();
            System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
            GraphHandler graphHandler = new GraphHandler(Main_old.owl);
            graph = new SingleGraph("Graph");
            graph = graphHandler.genGraph(graph);
            viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
            ProxyPipe pipe = viewer.newThreadProxyOnGraphicGraph();
            pipe.addAttributeSink(graph);
            view = viewer.addDefaultView(false);
            viewer.enableAutoLayout(layout);
            viewer.enableXYZfeedback(true);
            File stylesheet = new File("src/gui/test.css");
            String cssPath = stylesheet.getAbsolutePath();
            graph.addAttribute("ui.quality");
            graph.addAttribute("ui.antialias");
            graph.addAttribute("ui.stylesheet", "url(" + cssPath + ")");
            JFrame graphFrame = new JFrame();
            graphFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
            graphFrame.add((Component) view);
            graphFrame.setLocation(200, 0);
            graphFrame.setSize(screenWidth - 200, screenHeight - 40);
            graphFrame.setTitle("Instrument Annotated Ontology");
            graphFrame.setVisible(true);
            loadSideFrame();
            try {
                FileSource source = FileSourceFactory.sourceFor("graph.dgs");
                source.addSink(graph);
                source.begin("graph.dgs");
                while (source.nextEvents()) {
//                    Thread.sleep(50);
                }
                source.end();
            } catch (Exception e) {
                e.printStackTrace();
            }

            setData();
            mapCogLevel();
//      while (true) {
//        Thread.sleep(1000);
//        pipe.pump();
//      }
        } catch (OWLOntologyCreationException | OWLOntologyStorageException ex) {
            Logger.getLogger(ViewController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void setData() {
        if (qh == null) {
            try {
                qh = new QuestionHandler(Main_old.los, Main_old.q, Main_old.owl);
            } catch (OWLOntologyCreationException | IOException | OWLOntologyStorageException ex) {
                Logger.getLogger(ViewController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (loPartial == -1) {
            loPartial = qh.los.size();

        }
        if (qPartial == -1) {
            qPartial = qh.questions.size();
        }
        System.out.println("Number of LOs to process: " + loPartial);
        System.out.println("Number of Questions to process: " + qPartial);

        stats.put("total", 0);
        stats.put("LO", 0);
        stats.put("nLO", 0);
        stats.put("Q", 0);
        stats.put("nQ", 0);
        stats.put("LOQ", 0);
        stats.put("nLOQ", 0);
        for (int i = 0; i < loPartial; i++) {
            Set<String> implicitConcepts = qh.los.get(i).getImplicitConcepts();
            int loCogLevel = qh.los.get(i).getLevel();
            if (qh.los.get(i).getLevelName().equals(CognitiveLevel.noMatchString)) {
                //if no match do nothing
                loCogLevel = 0;
            } else {
                for (String concept : implicitConcepts) {
                    Node node = graph.getNode(concept);
                    node.setAttribute("LO Coverage", true);
                    if (node.hasAttribute("LO CogLevel")) {
                        int nodeCogLevel = node.getAttribute("LO CogLevel");
                        if (nodeCogLevel < loCogLevel) {
                            node.setAttribute("LO CogLevel", loCogLevel);
                        }
                    } else {
                        node.setAttribute("LO CogLevel", loCogLevel);
                    }
                }
            }
        }
        for (int i = 0; i < qPartial; i++) {
            Set<String> implicitConcepts = qh.questions.get(i).getImplicitConcepts();
            int qCogLevel = qh.questions.get(i).getLevel();
            if (qh.questions.get(i).getLevelName().equals(CognitiveLevel.noMatchString)) {
                //if no match do nothing
                qCogLevel = 0;
            } else {
                for (String concept : implicitConcepts) {
                    Node node = graph.getNode(concept);
                    node.setAttribute("Question asked", true);
                    if (node.hasAttribute("Q CogLevel")) {
                        int nodeCogLevel = node.getAttribute("Q CogLevel");
                        if (nodeCogLevel < qCogLevel) {
                            node.setAttribute("Q CogLevel", qCogLevel);
                        }
                    } else {
                        node.setAttribute("Q CogLevel", qCogLevel);
                    }
                }
            }
        }
        for (Node concept : graph.getNodeSet()) {
            stats.put("total", stats.get("total") + 1);

            if (concept.hasAttribute("LO Coverage")) {
                stats.put("LO", stats.get("LO") + 1);
            } else {
                stats.put("nLO", stats.get("nLO") + 1);
            }

            if (concept.hasAttribute("Question asked")) {
                stats.put("Q", stats.get("Q") + 1);
            } else {
                stats.put("nQ", stats.get("nQ") + 1);
            }

            if (concept.hasAttribute("LO Coverage") && concept.hasAttribute("Question asked")) {
                concept.setAttribute("ui.class", "LOQ");
                stats.put("LOQ", stats.get("LOQ") + 1);
            } else if (concept.hasAttribute("LO Coverage")) {
                concept.setAttribute("ui.class", "LO");
            } else if (concept.hasAttribute("Question asked")) {
                concept.setAttribute("ui.class", "nLOQ");
                stats.put("nLOQ", stats.get("nLOQ") + 1);
            }

        }
        total.setText("<html>Concepts in Syllabus: " + stats.get("total") + "</html>");
        LO.setText("<html>Concepts with LOs: " + stats.get("LO") + "</html>");
        LOQ.setText("<html>Concepts with LO & Q: " + stats.get("LOQ") + "</html>");
        nLOQ.setText("<html>Concepts with Questions,<br> but no LO: " + stats.get("nLOQ") + "</html>");
        System.out.println(stats);
    }

    public void mapCogLevel() {
        for (Node node : graph.getNodeSet()) {
            String cssClass = "";
            if (node.hasAttribute("LO CogLevel")) {
                cssClass += numNames[node.getAttribute("LO CogLevel")];
            } else {
                cssClass += "black";
            }
            if (node.hasAttribute("Q CogLevel")) {
                cssClass += numNames[node.getAttribute("Q CogLevel")];
            } else {
                cssClass += "black";
            }
            node.setAttribute("ui.class", cssClass);
        }
    }

    public void loadSideFrame() {
        sideFrame = new JFrame();
        sideFrame.setLocation(0, 0);
        sideFrame.setResizable(false);
        sideFrame.setSize(200, screenHeight - 40);
        sideFrame.setTitle("Options");
        //JButton computeBtn = new JButton("Compute");
        //JButton writeBtn = new JButton("Begin Write");
        //JButton relationToggle = new JButton("Hide Relations");
        JCheckBox relationToggle = new JCheckBox("View Relations");
        //JButton cogLevel = new JButton("Map CogLevel");
        JButton chart = new JButton("Chart");

        // Load Legend image
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("img/Legend-alt.png"));
            Image dimg = img.getScaledInstance(150, 200, Image.SCALE_SMOOTH);
            JLabel legend = new JLabel(new ImageIcon(dimg));
            legend.setBounds(20, 30, 150, 200);
            sideFrame.add(legend);
        } catch (IOException ex) {
            System.err.println("Legend image file not set properly");
        }

        //computeBtn.setBounds(30, 500, 120, 30);
        //cogLevel.setBounds(30, 540, 120, 30);
        //writeBtn.setBounds(30, 580, 120, 30);
        //relationToggle.setBounds(30, 620, 120, 30);
        //chart.setBounds(30, 660, 120, 30);
        relationToggle.setBounds(30, 540, 120, 30);
        chart.setBounds(30, 620, 120, 30);

        //sideFrame.add(computeBtn);
        //sideFrame.add(writeBtn);
        sideFrame.add(relationToggle);
        //sideFrame.add(cogLevel);
        sideFrame.add(chart);

        total = new JLabel();
        LO = new JLabel();
        LOQ = new JLabel();
        nLOQ = new JLabel();
        blank = new JLabel();
        total.setBounds(10, 250, 140, 50);
        LO.setBounds(10, 300, 140, 50);
        LOQ.setBounds(10, 350, 140, 50);
        nLOQ.setBounds(10, 400, 140, 50);
        sideFrame.add(total);
        sideFrame.add(LO);
        sideFrame.add(LOQ);
        sideFrame.add(nLOQ);
        sideFrame.add(blank);
        sideFrame.setVisible(true);
//        computeBtn.addActionListener((ActionEvent e) -> {
//            setData();
//        });

//        cogLevel.addActionListener((ActionEvent e) -> {
//            mapCogLevel();
//        });
        relationToggle.setSelected(relations_visibility);
        relationToggle.addActionListener((ActionEvent e) -> {
            if (relations_visibility) {
                graph.addAttribute("ui.stylesheet", "edge.relation{visibility-mode:hidden;}");
                relations_visibility = false;
                //relationToggle.setText("View Relations");
            } else {
                graph.addAttribute("ui.stylesheet", "edge.relation{visibility-mode:normal;}");
                relations_visibility = true;
                //relationToggle.setText("Hide Relations");
            }
        });

        chart.addActionListener((ActionEvent e) -> {
            flushCogLevel();
            final String html = "webview/index.html";
            final java.net.URI uri = java.nio.file.Paths.get(html).toAbsolutePath().toUri();
            try {
                Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start chrome " + uri.toString()});
            } catch (IOException ex) {
                Logger.getLogger(ViewController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        // Uncomment to make the "Begin Write" button work
//    writeBtn.addActionListener((ActionEvent e) -> {
//      try {
//        out.writeAll(graph, "graph.dgs");
//      } catch (IOException ex) {
//        Logger.getLogger(ViewController.class.getName()).log(Level.SEVERE, null, ex);
//      }
//    });
    }

    public void flushCogLevel() {
        List<Stream> streams = new ArrayList<>();
        Stream lo = new Stream("LO Cognitive Level");
        List<Concept> loConcepts = new ArrayList<>();
        int counter = 1;
        for (Node node : graph.getNodeSet()) {
            if (!node.getAttribute("ui.label").equals("Domain")) {
                Concept concept = new Concept(node.getAttribute("ui.label"));
                concept.y = node.hasAttribute("LO CogLevel") ? node.getAttribute("LO CogLevel") : 0;
                concept.x = counter;
                counter++;
                loConcepts.add(concept);
            }
        }
        lo.values = loConcepts;
        streams.add(lo);
        Stream q = new Stream("Q Cognitive Level");
        List<Concept> qConcepts = new ArrayList<>();
        counter = 1;
        for (Node node : graph.getNodeSet()) {
            if (!node.getAttribute("ui.label").equals("Domain")) {
                Concept concept = new Concept(node.getAttribute("ui.label"));
                concept.y = node.hasAttribute("Q CogLevel") ? node.getAttribute("Q CogLevel") : 0;
                concept.x = counter;
                counter++;
                qConcepts.add(concept);
            }
        }
        q.values = qConcepts;
        streams.add(q);

        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();
        File jsonData = new File("webview/concepts.json");
        try {
            jsonData.createNewFile();
            try (FileOutputStream fOut = new FileOutputStream(jsonData)) {
                OutputStreamWriter outWriter = new OutputStreamWriter(fOut);
                outWriter.append("var data = " + gson.toJson(streams));
                outWriter.close();
            }
        } catch (Exception e) {
            System.out.println(e);
        }

    }
}
