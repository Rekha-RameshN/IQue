package process;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import train.FileNames;

/**
 * @author Akshen Kadakia <akshenk8@gmail.com>
 */
public class TesterSheetGenerator {

    public static final String sheetName1 = "Cognitive";
    public static final String sheetName2 = "Concepts";
    public static final String sheetName3 = "Imp Nodes";
    public static final String sheetName4 = "Formula";

    String[][] impNodes = {
        {"ADT"},
        {"Representation"},
        {"Application"},
        {"Traversal_Operation"},
        {},
        {},
        {"Hash_Table", "Collision_Avoidance"},
        {},
        {},
        {}
    };

    /**
     * pre-requistes for calculations
     *
     * @param ALPHA
     * @param BETA
     * @param q
     * @param los
     * @param owl
     * @param file_name
     * @throws OWLOntologyCreationException
     * @throws IOException
     * @throws OWLOntologyStorageException
     */
    public void generateSheet(float ALPHA, float BETA, File q, File los, File owl, String file_name)
            throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {

        QuestionHandler qh = new QuestionHandler(los, q, owl);
        ArrayList<Question> qs = qh.questions, ls = qh.los;
        generateSheet(ALPHA, BETA, qs, ls, qh, file_name);
    }

    /**
     * pre-requistes for calculations
     *
     * @param q
     * @param los
     * @param owl
     * @param file_name
     * @throws OWLOntologyCreationException
     * @throws IOException
     * @throws OWLOntologyStorageException
     */
    public void generateSheet(File q, File los, File owl, String file_name)
            throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {

        QuestionHandler qh = new QuestionHandler(los, q, owl);
        ArrayList<Question> qs = qh.questions, ls = qh.los;
        generateSheet(qs, ls, qh, file_name);
    }

    /**
     * pre-requistes for calculations writes workbook to file_name
     *
     * @param ALPHA
     * @param BETA
     * @param q
     * @param lo
     * @param qh
     * @param owl
     * @param file_name
     * @throws OWLOntologyCreationException
     * @throws IOException
     * @throws OWLOntologyStorageException
     */
    public void generateSheet(float ALPHA, float BETA, ArrayList<Question> q, ArrayList<Question> lo,
            QuestionHandler qh, String file_name)
            throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
        QuestionCompare qc = new QuestionCompare(ALPHA, BETA, qh);

        FileOutputStream fileOut = new FileOutputStream(file_name);
        //write this workbook to an Outputstream.
        calculationsSheet(q, lo, qc).write(fileOut);
        fileOut.flush();
        fileOut.close();
    }

    /**
     * pre-requistes for calculations writes workbook to file_name
     *
     * @param q
     * @param lo
     * @param qh
     * @param owl
     * @param file_name
     * @throws OWLOntologyCreationException
     * @throws IOException
     * @throws OWLOntologyStorageException
     */
    public void generateSheet(ArrayList<Question> q, ArrayList<Question> lo,
            QuestionHandler qh, String file_name)
            throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
        QuestionCompare qc = new QuestionCompare(qh);

        FileOutputStream fileOut = new FileOutputStream(file_name);
        //write this workbook to an Outputstream.
        calculationsSheet(q, lo, qc).write(fileOut);
        fileOut.flush();
        fileOut.close();
    }

    /**
     * Generates the average cognitive and content level alignment
     *
     * @param Q
     * @param L
     * @param qh
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public ArrayList generateAlignmentValues(ArrayList<Question> Q, ArrayList<Question> L, QuestionHandler qh) throws FileNotFoundException, IOException {
        QuestionCompare qc = new QuestionCompare(qh);
        XSSFWorkbook wb = calculationsSheet(Q, L, qc);

        XSSFSheet formulaSheet = wb.getSheet(sheetName4),
                cogLevelSheet = wb.getSheet(sheetName1);

        //wb.removeSheetAt(wb.getSheetIndex(sheetName2));
        wb.removeSheetAt(wb.getSheetIndex(sheetName3));

        int lastCol = formulaSheet.getRow(0).getLastCellNum();
        int lastRow = formulaSheet.getLastRowNum() + 1;

        LinkedHashMap<Integer, ArrayList> questionValMap = new LinkedHashMap<>();
        ArrayList<Integer> nonUtilityQuestion = new ArrayList<>();
        double maxSum = 0.0, maxCount = 0;
        double cogLevelAlignment, contentAlignment;

        for (int row = 1; row < lastRow; row++) {
            double tmpMaxRow = 0;
            //for avoiding 2nd loop
            LinkedHashMap<Integer, Double> valMap = new LinkedHashMap<>();

            XSSFRow currRow = formulaSheet.getRow(row);
            //find max of this row
            for (int col = 1; col < lastCol; col++) {
                XSSFCell cell = currRow.getCell(col);
                double val = cell.getNumericCellValue();
                if (val > tmpMaxRow) {
                    tmpMaxRow = val;
                }
                if (val > 0) {
                    valMap.put(col, val);
                }
            }
            //if 1 set rest to 0
            if (tmpMaxRow == 1.0) {
                Set<Integer> s = new LinkedHashSet();
                s.addAll(valMap.keySet());
                for (int c : s) {
                    if (valMap.get(c) != tmpMaxRow) {
                        XSSFCell cell = currRow.getCell(c);
                        cell.setCellValue("0");
                        valMap.remove(c);
                    }
                }
            } //test if additional information 
            else {
                if (valMap.size() > 0) {
                    valMap = (LinkedHashMap) sortByValue(valMap);
                    //System.out.println(""+valMap.keySet());
                    Set<Integer> locSet = valMap.keySet();
                    Iterator<Integer> locIt = locSet.iterator();
                    Question result = Q.get(locIt.next() - 1);
                    while (locIt.hasNext()) {
                        Question q1 = Q.get(locIt.next() - 1);
                        result = combine(result, q1);
                    }
                    qh.calculateClassification(result);
                    //System.out.println(""+result);
                    //System.out.println(""+qc.compare(L.get(row-1), result));
                    double tmpVal = (double) qc.compare(L.get(row - 1), result).get(QuestionCompare.VAL);
                    if (tmpVal > tmpMaxRow) {
                        ArrayList<Integer> keys = new ArrayList<>();
                        keys.addAll(valMap.keySet());

                        ArrayList<Integer> toRemove = new ArrayList<>();
                        for (int k1 = 0; k1 < keys.size() - 1; k1++) {
                            Question A = Q.get(keys.get(k1) - 1);
                            for (int k2 = k1 + 1; k2 < keys.size(); k2++) {
                                Question B = Q.get(keys.get(k2) - 1);
                                Question tmp = combine(A, B);
                                qh.calculateClassification(tmp);
                                double chkVal = (double) qc.compare(L.get(row - 1), tmp).get(QuestionCompare.VAL);
                                double k1_val = valMap.get(k1),
                                        k2_val = valMap.get(k2);
                                if (chkVal > Math.max(k1_val, k2_val)
                                        || k1_val == k2_val) {
                                    //keep both
                                } else {
                                    //remove lower always k1 as sorted
                                    double min = Math.min(k1_val, k2_val);
                                    if (k1_val == min) {
                                        toRemove.add(k1);
                                    } else if (k2_val == min) {
                                        toRemove.add(k2);
                                    }
                                }
                            }
                        }
                        for (int loc : toRemove) {
                            valMap.remove(loc);
                            XSSFCell cell = currRow.getCell(loc);
                            cell.setCellValue("0");
                        }
                    } else {
                        Set<Integer> s = new LinkedHashSet();
                        s.addAll(valMap.keySet());
                        for (int loc : s) {
                            double val = valMap.get(loc);
                            if (val != tmpMaxRow) {
                                valMap.remove(loc);
                                XSSFCell cell = currRow.getCell(loc);
                                cell.setCellValue("0");
                            }
                        }
                    }
                }
            }
            double avg = 0.0;
            for (int loc : valMap.keySet()) {
                avg += valMap.get(loc);
                if (questionValMap.containsKey(loc)) {
                    ArrayList toAdd = questionValMap.get(loc);
                    toAdd.set(0, (int) toAdd.get(0) + 1);
                    toAdd.set(1, (double) toAdd.get(1) + valMap.get(loc));
                    questionValMap.put(loc, toAdd);
                } else {
                    ArrayList toAdd = new ArrayList();
                    toAdd.add(1);
                    toAdd.add(valMap.get(loc));
                    questionValMap.put(loc, toAdd);
                }
            }
            avg /= valMap.size();
            if(Double.isNaN(avg)){
                avg=0.0;
            }
            maxSum += avg;
            maxCount++;
            XSSFCell cell = currRow.createCell(lastCol);
            cell.setCellValue("" + avg);
            //System.out.println("" + valMap);
        }
        //System.out.println(""+questionValMap);
        XSSFRow utilityRow = formulaSheet.createRow(lastRow);
        for (int loc = 1; loc < lastCol; loc++) {
            XSSFCell cell = utilityRow.createCell(loc);
            if (questionValMap.containsKey(loc)) {
                ArrayList list = questionValMap.get(loc);
                double tmp=(double) list.get(1) / (int) list.get(0);
                if(Double.isNaN(tmp)){
                    tmp=0.0;
                }
                cell.setCellValue("" + tmp);
            } else {
                cell.setCellValue("0");
                nonUtilityQuestion.add(loc);
            }
        }
        {
            //MAX average
            XSSFCell cell = utilityRow.createCell(lastCol);
            contentAlignment = (maxSum / maxCount) * 100.0;
            cell.setCellValue("" + contentAlignment);
        }

        //using nonUtilityQuestion calculate cognitive level alignment
        int cogMaxSum = 0, cogMaxCount = 0;
        for (int row = 1; row < lastRow; row++) {
            XSSFRow currRow = cogLevelSheet.getRow(row);
            int tmpMAX = 0;
            for (int col = 1; col < lastCol; col++) {
                if (!nonUtilityQuestion.contains(col)) {
                    XSSFCell cell = currRow.getCell(col);
                    if (cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
                        int val = (int) cell.getNumericCellValue();
                        if (val > tmpMAX) {
                            tmpMAX = val;
                        }
                    }
                }
            }
            XSSFRow formulaCurrRow = formulaSheet.getRow(row);
            XSSFCell cell = formulaCurrRow.createCell(lastCol + 1);
            cell.setCellValue("" + tmpMAX);
            cogMaxSum += tmpMAX;
            cogMaxCount++;
        }
        {
            //avg cognitive level            
            XSSFCell cell = utilityRow.createCell(lastCol + 1);
            cogLevelAlignment = (cogMaxSum * 1.0) / cogMaxCount;
            cell.setCellValue("" + cogLevelAlignment);
        }

        //System.out.println(""+(cogLevelAlignment+contentAlignment)/2);
        //for testing 
        String file_name = FileNames.currentDir() + FileNames.SEPARATOR + "test1.xlsx";
         FileOutputStream fileOut = new FileOutputStream(file_name);
         //write this workbook to an Outputstream.
         wb.write(fileOut);
         fileOut.flush();
         fileOut.close();
         Desktop.getDesktop().open(new File(file_name));
        ArrayList retResult = new ArrayList();
        retResult.add(Math.round(contentAlignment * 100.0) / 100.0);
        retResult.add(Math.round(cogLevelAlignment * 100.0) / 100.0);
        retResult.add(nonUtilityQuestion);
        return retResult;
    }

    /**
     * combines concepts of two of more questions
     *
     * @param q
     * @return
     */
    private Question combine(Question... q) {
        if (q.length == 0) {
            return null;
        }
        if (q.length == 1) {
            return q[0];
        }
        Question result = q[q.length - 1];
        LinkedHashSet<String> concepts = new LinkedHashSet<>();
        for (Question s : q) {
            concepts.addAll(s.getImplicitConcepts());
        }
        result.setImplicitConcepts(concepts);
        return result;
    }

    /**
     * sorts the map for ascending order
     *
     * @param <K>
     * @param <V>
     * @param map
     * @return
     */
    public <K, V extends Comparable<? super V>> Map<K, V>
            sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list
                = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /*public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException, IOException {
     TesterSheetGenerator tsg = new TesterSheetGenerator();
     File lo = new File(FileNames.currentDir() + FileNames.SEPARATOR + "lnew.txt");
     File q = new File(FileNames.currentDir() + FileNames.SEPARATOR + "qnew.txt");
     File owl = new File(FileNames.currentDir() + FileNames.SEPARATOR + "final.owl");

     QuestionHandler qh = new QuestionHandler(lo, q, owl);

     tsg.generateAlignmentValues(qh.questions, qh.los, qh);
     }*/
    /**
     * creates 4 sheets and returns workbook
     *
     * @param qs
     * @param ls
     * @param qc
     * @return
     */
    XSSFWorkbook calculationsSheet(ArrayList<Question> qs, ArrayList<Question> ls, QuestionCompare qc) {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet1 = wb.createSheet(sheetName1);
        XSSFSheet sheet2 = wb.createSheet(sheetName2);
        XSSFSheet sheet4 = wb.createSheet(sheetName4);

        //only 10 los important nodes
        /*for (int i = 0; i < 10; i++) {
         Question g = ls.get(i);
         HashMap<String, Double> imp = new HashMap<>();
         for (int j = 0; j < impNodes[i].length; j++) {
         imp.put(impNodes[i][j], g.defaultImpNodeValue);
         }
         g.setImportantNodes(imp);
         ls.set(i, g);
         }*/
        //re-use fonts
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        style.setFont(font);
        style.setWrapText(true);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

        CellStyle style2 = wb.createCellStyle();
        style2.setWrapText(true);
        style2.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

        for (int i = 0; i < ls.size() + 1; i++) {
            XSSFRow row1 = sheet1.createRow(i);
            XSSFRow row2 = sheet2.createRow(i);
            XSSFRow row4 = sheet4.createRow(i);
            //height in twips
            row1.setHeight((short) 1200);
            row2.setHeight((short) 1200);
            row4.setHeight((short) 1200);
            for (int j = 0; j < qs.size() + 1; j++) {
                XSSFCell cell1 = row1.createCell(j);
                XSSFCell cell2 = row2.createCell(j);
                XSSFCell cell4 = row4.createCell(j);
                if (i == 0 && j == 0) {
                    //width in excel differs                            
                    sheet1.setColumnWidth(j, 6600);
                    sheet2.setColumnWidth(j, 6600);
                    sheet4.setColumnWidth(j, 6600);
                    cell1.setCellValue("LO\\Question");
                    cell1.setCellStyle(style);
                    cell2.setCellValue("LO\\Question");
                    cell2.setCellStyle(style);
                    cell4.setCellValue("LO\\Question");
                    cell4.setCellStyle(style);
                } else if (i == 0 && j != 0) {
                    sheet1.setColumnWidth(j, 6600);
                    sheet2.setColumnWidth(j, 6600);
                    sheet4.setColumnWidth(j, 6600);
                    cell1.setCellValue(j + "." + qs.get(j - 1).getStatement());
                    cell1.setCellStyle(style);
                    cell2.setCellValue(j + "." + qs.get(j - 1).getStatement());
                    cell2.setCellStyle(style);
                    cell4.setCellValue(j + "." + qs.get(j - 1).getStatement());
                    cell4.setCellStyle(style);
                } else if (i != 0 && j == 0) {
                    cell1.setCellValue(i + "." + ls.get(i - 1).getStatement());
                    cell1.setCellStyle(style);
                    cell2.setCellValue(i + "." + ls.get(i - 1).getStatement());
                    cell2.setCellStyle(style);
                    cell4.setCellValue(i + "." + ls.get(i - 1).getStatement());
                    cell4.setCellStyle(style);
                } else {
                    //cog
                    if (ls.get(i - 1).getLevel() == CognitiveLevel.noMatchInt
                            || qs.get(j - 1).getLevel() == CognitiveLevel.noMatchInt) {
                        cell1.setCellValue(CognitiveLevel.noMatchString);
                    } else {
                        int diff = Math.abs(ls.get(i - 1).getLevel() - qs.get(j - 1).getLevel());
                        cell1.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
                        //cell1.setCellValue(100-(diff*20));
                        if (diff == 0) {
                            cell1.setCellValue(100);
                        } else if (diff == 1) {
                            cell1.setCellValue(50);
                        } else {
                            cell1.setCellValue(0);
                        }
                    }
                    //concepts
                    Question LO = ls.get(i - 1);
                    Question Q = qs.get(j - 1);

                    HashMap<String, Object> res = qc.compare(LO, Q);

                    /*cell2.setCellValue("L:" + (LinkedHashSet<String>) res.get(QuestionCompare.L)
                     + "\nQ:" + (LinkedHashSet<String>) res.get(QuestionCompare.QU)
                     + "\nLNQ:" + (HashMap<String, LinkedHashSet<String>>) res.get(QuestionCompare.LNQ)
                     + "\nL|Q:" + (LinkedHashSet<String>) res.get(QuestionCompare.L_Q)
                     + "\nQ|L:" + (LinkedHashSet<String>) res.get(QuestionCompare.Q_L)
                     + "\n" + (Double) res.get(QuestionCompare.VAL));*/
                    cell2.setCellValue("LNQ:" + ((HashMap<String, LinkedHashSet<String>>) res.get(QuestionCompare.LNQ)).keySet().size()
                            + "\nL|Q:" + ((LinkedHashSet<String>) res.get(QuestionCompare.L_Q)).size()
                            + "\nQ|L:" + ((LinkedHashSet<String>) res.get(QuestionCompare.Q_L)).size()
                            + "\n" + ((double) res.get(QuestionCompare.VAL)));
                    cell2.setCellStyle(style2);

                    cell4.setCellValue((double) res.get(QuestionCompare.VAL));
                    cell4.setCellStyle(style2);
                }
            }
        }
        sheet1.createFreezePane(1, 1);
        sheet2.createFreezePane(1, 1);
        sheet4.createFreezePane(1, 1);

        XSSFSheet sheet3 = wb.createSheet(sheetName3);
        for (int i = 0; i < ls.size() + 1; i++) {
            XSSFRow row1 = sheet3.createRow(i);
            //height in twips
            row1.setHeight((short) 1200);
            for (int j = 0; j < 2; j++) {
                XSSFCell cell1 = row1.createCell(j);
                if (i == 0) {
                    sheet3.setColumnWidth(j, 6600);
                    if (j == 0) {
                        cell1.setCellValue("LO");
                    } else {
                        cell1.setCellValue("Important nodes");
                    }
                    cell1.setCellStyle(style);
                } else {
                    if (j == 0) {
                        cell1.setCellValue(ls.get(i - 1).getStatement());
                        cell1.setCellStyle(style);
                    } else {
                        cell1.setCellValue(ls.get(i - 1).getImportantNodes().keySet().toString());
                    }
                }
            }
        }
        /*try {
            FileOutputStream fos = new FileOutputStream(
                    FileNames.currentDir() + FileNames.SEPARATOR + FileNames.TEACHER_FOLDER_NAME
                    + FileNames.SEPARATOR + "demo.xlsx");
            wb.write(fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {

        }*/
        return wb;
    }
}
