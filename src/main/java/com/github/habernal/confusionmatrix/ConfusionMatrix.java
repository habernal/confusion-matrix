/*
 * Copyright 2013-2015 Ivan Habernal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.habernal.confusionmatrix;

import java.util.*;

/**
 * Implementation of confusion matrix for evaluating learning algorithms; computes macro F-measure,
 * accuracy, confidence intervals, Cohen's Kappa
 *
 * @author Ivan Habernal
 */
public class ConfusionMatrix
{

    protected int total = 0;

    protected int correct = 0;

    protected Map<String, Map<String, Integer>> map;

    protected int numberOfDecimalPlaces = 3;

    protected TreeSet<String> allGoldLabels = new TreeSet<>();

    protected TreeSet<String> allPredictedLabels = new TreeSet<>();

    protected List<String> labelSeries = new ArrayList<>();

    private Locale locale = Locale.ENGLISH;

    public ConfusionMatrix()
    {
        this.map = new TreeMap<>();

    }

    public void setNumberOfDecimalPlaces(int numberOfDecimalPlaces)
            throws IllegalArgumentException
    {
        if (numberOfDecimalPlaces < 1 || numberOfDecimalPlaces > 100) {
            throw new IllegalArgumentException("Argument must be in rage 1-100");
        }

        this.numberOfDecimalPlaces = numberOfDecimalPlaces;
    }

    public void setLocale(Locale locale)
    {
        this.locale = locale;
    }

    private Locale getLocale() {
        return locale;
    }

    private String getFormat()
    {
        return "%." + numberOfDecimalPlaces + "f";
    }

    public void increaseValue(String goldValue, String observedValue)
    {
        increaseValue(goldValue, observedValue, 1);
    }

    /**
     * Returns the series of actual labels
     *
     * @return list of labels
     */
    public List<String> getLabelSeries()
    {
        return labelSeries;
    }

    /**
     * Increases value of goldValue x observedValue n times
     *
     * @param goldValue     exp
     * @param observedValue ac
     * @param times         n-times
     */
    public void increaseValue(String goldValue, String observedValue, int times)
    {
        allGoldLabels.add(goldValue);
        allPredictedLabels.add(observedValue);

        for (int i = 0; i < times; i++) {
            labelSeries.add(observedValue);
        }

        if (!map.containsKey(goldValue)) {
            map.put(goldValue, new TreeMap<String, Integer>());
        }

        if (!map.get(goldValue).containsKey(observedValue)) {
            map.get(goldValue).put(observedValue, 0);
        }

        int currentValue = this.map.get(goldValue).get(observedValue);
        this.map.get(goldValue).put(observedValue, currentValue + times);

        total += times;

        if (goldValue.equals(observedValue)) {
            correct += times;
        }
    }

    public double getAccuracy()
    {
        return ((double) correct / (double) total);
    }

    public int getTotalSum()
    {
        return total;
    }

    public int getRowSum(String label)
    {
        int result = 0;

        for (Integer i : map.get(label).values()) {
            result += i;
        }

        return result;
    }

    public int getColSum(String label)
    {
        int result = 0;

        for (Map<String, Integer> row : this.map.values()) {
            if (row.containsKey(label)) {
                result += row.get(label);
            }
        }

        return result;
    }

    public Map<String, Double> getPrecisionForLabels()
    {
        Map<String, Double> precisions = new LinkedHashMap<>();
        for (String label : allGoldLabels) {
            double precision = getPrecisionForLabel(label);

            precisions.put(label, precision);
        }
        return precisions;
    }

    public double getPrecisionForLabel(String label)
    {
        double precision = 0;
        int tp = 0;
        int fpAndTp = 0;

        if (map.containsKey(label) && map.get(label).containsKey(label)) {
            tp = this.map.get(label).get(label);
            fpAndTp = getColSum(label);
        }

        if (fpAndTp > 0) {
            precision = (double) tp / (double) (fpAndTp);
        }

        return precision;
    }

    /**
     * Micro-averaged F-measure gives equal weight to each document and is therefore
     * considered as an average over all the document/category pairs. It tends to be
     * dominated by the classifierâ€™s performance on common categories. (It's actually the accuracy).
     * <p/>
     * (from Ozgur et al., 2005. Text Categorization with Class-Based and Corpus-Based Keyword Selection.)
     *
     * @return double
     */
    public double getMicroFMeasure()
    {
        int allTruePositives = 0;
        int allTruePositivesAndFalsePositives = 0;
        int allTruePositivesAndFalseNegatives = 0;

        for (String label : map.keySet()) {
            if (map.containsKey(label) && map.get(label).containsKey(label)) {
                allTruePositives += this.map.get(label).get(label);
            }
            allTruePositivesAndFalsePositives += getColSum(label);
            allTruePositivesAndFalseNegatives += getRowSum(label);
        }

        double precision = (double) allTruePositives / (double) allTruePositivesAndFalsePositives;
        double recall = (double) allTruePositives / (double) allTruePositivesAndFalseNegatives;

        return (2.0 * precision * recall) / (precision + recall);
    }

    /**
     * Macro-averaged F-measure gives equal
     * weight to each category, regardless of its frequency. It is influenced more by the
     * classifier performance on rare categories.
     *
     * @return double
     */
    public double getMacroFMeasure()
    {
        Map<String, Double> fMeasureForLabels = getFMeasureForLabels();

        double totalFMeasure = 0;

        for (Double d : fMeasureForLabels.values()) {
            totalFMeasure += d;
        }

        return totalFMeasure / fMeasureForLabels.size();
    }

    /**
     * Macro-averaged F-measure with beta parameter.
     *
     * @param beta beta parameter
     * @return double
     */
    public double getMacroFMeasure(double beta)
    {
        Map<String, Double> fMeasureForLabels = getFMeasureForLabels(beta);

        double totalFMeasure = 0;

        for (Double d : fMeasureForLabels.values()) {
            totalFMeasure += d;
        }

        return totalFMeasure / fMeasureForLabels.size();
    }

    /**
     * Returns F-measure for categories; see See http://en.wikipedia.org/wiki/F1_score
     *
     * @return double
     */
    public Map<String, Double> getFMeasureForLabels()
    {
        Map<String, Double> fMeasure = new LinkedHashMap<>();

        Map<String, Double> precisionForLabels = getPrecisionForLabels();
        Map<String, Double> recallForLabels = getRecallForLabels();

        for (String label : allGoldLabels) {
            double p = precisionForLabels.get(label);
            double r = recallForLabels.get(label);

            double fm = 0;

            if ((p + r) > 0) {
                fm = (2 * p * r) / (p + r);
            }

            fMeasure.put(label, fm);
        }

        return fMeasure;
    }

    /**
     * See http://en.wikipedia.org/wiki/F1_score
     *
     * @param beta beta paremeter; higher than 1 prefers recall, lower than 1 prefers precision
     * @return double
     */
    public Map<String, Double> getFMeasureForLabels(double beta)
    {
        Map<String, Double> fMeasure = new LinkedHashMap<>();

        Map<String, Double> precisionForLabels = getPrecisionForLabels();
        Map<String, Double> recallForLabels = getRecallForLabels();

        for (String label : allGoldLabels) {
            double p = precisionForLabels.get(label);
            double r = recallForLabels.get(label);

            double fm = 0;

            if ((p + r) > 0) {
                fm = (1.0 + (beta * beta)) * ((p * r) / ((beta * beta * p) + r));
            }

            fMeasure.put(label, fm);
        }

        return fMeasure;
    }

    /**
     * Return recall for labels
     *
     * @return double
     */
    public Map<String, Double> getRecallForLabels()
    {
        Map<String, Double> recalls = new LinkedHashMap<>();
        for (String label : allGoldLabels) {
            double recall = getRecallForLabel(label);

            recalls.put(label, recall);
        }
        return recalls;
    }

    /**
     * Return recall for single label
     *
     * @param label label
     * @return double
     */
    public double getRecallForLabel(String label)
    {
        int fnAndTp = 0;
        double recall = 0;
        int tp = 0;

        if (map.containsKey(label) && map.get(label).containsKey(label)) {
            tp = this.map.get(label).get(label);
            fnAndTp = getRowSum(label);
        }

        if (fnAndTp > 0) {
            recall = (double) tp / (double) (fnAndTp);
        }

        return recall;
    }

    /**
     * Returns the half of the confidence interval on accuracy on alpha = 95
     *
     * @return conf. int
     */
    public double getConfidence95Accuracy()
    {
        return 1.96 * Math.sqrt(getAccuracy() * (1.0 - getAccuracy()) / total);
    }

    /**
     * Returns the half of the confidence interval on accuracy on alpha = 90
     *
     * @return conf. int
     */
    public double getConfidence90Accuracy()
    {
        return 1.645 * Math.sqrt(getAccuracy() * (1.0 - getAccuracy()) / total);
    }

    public double getConfidence90AccuracyLow()
    {
        return getAccuracy() - getConfidence90Accuracy();
    }

    public double getConfidence90AccuracyHigh()
    {
        return getAccuracy() + getConfidence90Accuracy();
    }

    /**
     * Returns the lower bound of the accuracy with alpha = 95
     *
     * @return accuracy minus half of the confidence interval
     */
    public double getConfidence95AccuracyLow()
    {
        return getAccuracy() - getConfidence95Accuracy();
    }

    /**
     * Returns the upper bound of the accuracy with alpha = 95
     *
     * @return accuracy plus half of the confidence interval
     */
    public double getConfidence95AccuracyHigh()
    {
        return getAccuracy() + getConfidence95Accuracy();
    }

    /**
     * Returns the half of confidence interval on alpha = 95 (see
     * http://alias-i.com/lingpipe/docs/api/com/aliasi/classify/ConfusionMatrix.html#confidence95%28%29)
     *
     * @return conf
     */
    public double getConfidence95MacroFM()
    {
        return 1.96 * Math.sqrt(getMacroFMeasure() * (1.0 - getMacroFMeasure()) / total);
    }

    public double getConfidence90MacroFM()
    {
        return 1.66 * Math.sqrt(getMacroFMeasure() * (1.0 - getMacroFMeasure()) / total);
    }

    /**
     * Returns the lower bound of the macro F-measure with alpha = 95
     *
     * @return macro F-measure minus half of the confidence interval
     */
    public double getConfidence95MacroFMLow()
    {
        return getMacroFMeasure() - getConfidence95MacroFM();
    }

    /**
     * Returns the upper bound of the macro F-measure with alpha = 95
     *
     * @return macro F-measure plus half of the confidence interval
     */
    public double getConfidence95MacroFMHigh()
    {
        return getMacroFMeasure() + getConfidence95MacroFM();
    }

    /**
     * Computes Cohen's Kappa
     *
     * @return double
     */
    public double getCohensKappa()
    {
        // compute p (which is actually accuracy)
        double p = getAccuracy();

        //		System.out.println(p);

        // compute pe
        double pe = 0;
        for (String label : this.allGoldLabels) {
            double row = getRowSum(label);
            double col = getColSum(label);

            //			System.out.println("Label " + label + ", sumCol: " + col + ", sumRow: " + row);

            pe += (row * col) / getTotalSum();
        }

        pe = pe / getTotalSum();

        return (p - pe) / (1 - pe);
    }

    private List<List<String>> prepareToString()
    {
        // adding zeros
        for (String row : allGoldLabels) {
            if (!map.containsKey(row)) {
                map.put(row, new TreeMap<String, Integer>());
            }

            for (String col : allPredictedLabels) {
                if (!map.get(row).containsKey(col)) {
                    map.get(row).put(col, 0);
                }
            }
        }

        List<List<String>> result = new ArrayList<>();

        List<String> allPredictedLabelsSorted = new ArrayList<>();
        TreeSet<String> extraPredictedLabels = new TreeSet<>(allPredictedLabels);
        extraPredictedLabels.removeAll(allGoldLabels);

        allPredictedLabelsSorted.addAll(allGoldLabels);
        allPredictedLabelsSorted.addAll(extraPredictedLabels);

        // header
        List<String> header = new ArrayList<>();
        header.add("↓gold\\pred→");
        header.addAll(allPredictedLabelsSorted);
        result.add(header);

        for (String rowLabel : allGoldLabels) {
            List<String> row = new ArrayList<>();
            row.add(rowLabel);

            for (String predictedLabel : allPredictedLabelsSorted) {
                int value = 0;

                if (this.map.containsKey(rowLabel) && this.map.get(rowLabel)
                        .containsKey(predictedLabel)) {
                    value = this.map.get(rowLabel).get(predictedLabel);
                }
                row.add(Integer.toString(value));
            }

            result.add(row);
        }

        return result;
    }

    protected String tableToString(List<List<String>> table)
    {
        // finding the maximum entry length
        int maxEntryLength = Integer.MIN_VALUE;
        for (List<String> row : table) {
            for (String value : row) {
                if (value.length() > maxEntryLength) {
                    maxEntryLength = value.length();
                }
            }
        }

        String f = "%" + (maxEntryLength + 1) + "s";

        StringBuilder sb = new StringBuilder();

        for (List<String> row : table) {
            for (String value : row) {
                sb.append(String.format(f, value));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    @Override
    public String toString()
    {
        List<List<String>> table = prepareToString();

        return tableToString(table);
    }

    /**
     * Prints in LaTeX format
     *
     * @return string
     */
    public String toStringLatex()
    {
        List<List<String>> table = prepareToString();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < table.size(); i++) {
            List<String> row = table.get(i);

            for (int j = 0; j < row.size(); j++) {
                String value = row.get(j);

                if ((i == 0 || j == 0) && !value.isEmpty()) {
                    sb.append("\\textbf{").append(value).append("} ");
                }
                else {
                    sb.append(value);
                    sb.append(" ");
                }

                if (j < row.size() - 1) {
                    sb.append("& ");
                }
            }

            sb.append("\\\\\n");
        }

        return sb.toString();
    }

    public String printNiceResults()
    {
        return "Macro F-measure: " + String.format(Locale.ENGLISH, getFormat(), getMacroFMeasure())
                + ", (CI at .95: " + String
                .format(Locale.ENGLISH, getFormat(), getConfidence95MacroFM())
                + "), micro F-measure (acc): " + String
                .format(Locale.ENGLISH, getFormat(), getMicroFMeasure());
    }

    public String printLabelPrecRecFm()
    {
        Map<String, Double> precisionForLabels = getPrecisionForLabels();
        Map<String, Double> recallForLabels = getRecallForLabels();
        Map<String, Double> fMForLabels = getFMeasureForLabels();

        StringBuilder sb = new StringBuilder("P/R/Fm: ");

        for (Map.Entry<String, Double> entry : precisionForLabels.entrySet()) {
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(String.format(Locale.ENGLISH, getFormat(), entry.getValue()));
            sb.append("/");
            sb.append(String.format(Locale.ENGLISH, getFormat(),
                    recallForLabels.get(entry.getKey())));
            sb.append("/");
            sb.append(String.format(Locale.ENGLISH, getFormat(), fMForLabels.get(entry.getKey())));
            sb.append(" ");
        }

        return sb.toString();
    }

    public double getAvgPrecision()
    {
        double res = 0;
        Collection<Double> values = getPrecisionForLabels().values();
        for (double d : values) {
            res += d;
        }

        return res / (double) values.size();
    }

    public double getAvgRecall()
    {
        double res = 0;
        Collection<Double> values = getRecallForLabels().values();
        for (double d : values) {
            res += d;
        }

        return res / (double) values.size();
    }

    /**
     * Sums up all matrices into a new one
     *
     * @param matrices confusion matrices
     * @return confusion matrix
     */
    public static ConfusionMatrix createCumulativeMatrix(ConfusionMatrix... matrices)
    {
        ConfusionMatrix result = new ConfusionMatrix();

        for (ConfusionMatrix matrix : matrices) {
            for (Map.Entry<String, Map<String, Integer>> gold : matrix.map.entrySet()) {
                for (Map.Entry<String, Integer> actual : gold.getValue().entrySet()) {
                    result.increaseValue(gold.getKey(), actual.getKey(), actual.getValue());
                }
            }
        }

        return result;
    }

    /**
     * Returns C + C^T - InC (this + transposed this - unit matrix * this), see
     * <p/>
     * See
     * Silvie Cinkova, Martin Holub, and Vincent Kriz. 2012. Managing uncertainty in semantic
     * tagging. In Proceedings of the 13th Conference of the European Chapter of the Association
     * for Computational Linguistics, EACL '12, pages 840-850, Stroudsburg, PA, USA.
     * Association for Computational Linguistics.
     *
     * @return new instance
     */
    public ConfusionMatrix getSymmetricConfusionMatrix()
    {
        return createCumulativeMatrix(this, getTransposedMatrix(), getNegativeUnitMatrix());
    }

    /**
     * Returns transposed confusion matrix (gold and predicted are switched)
     *
     * @return new instance
     */
    public ConfusionMatrix getTransposedMatrix()
    {
        ConfusionMatrix result = new ConfusionMatrix();

        for (Map.Entry<String, Map<String, Integer>> gold : this.map.entrySet()) {
            for (Map.Entry<String, Integer> predicted : gold.getValue().entrySet()) {
                int value = predicted.getValue();

                // add reverted values
                result.increaseValue(predicted.getKey(), gold.getKey(), value);
            }
        }

        return result;
    }

    /**
     * Returns unit matrix (identity matrix) with all diagonal values negative and non-diagonal zeros
     * <pre>
     * (- 1) * In
     * </pre>
     *
     * @return negative unit matrix
     */
    protected ConfusionMatrix getNegativeUnitMatrix()
    {
        ConfusionMatrix result = new ConfusionMatrix();

        for (Map.Entry<String, Map<String, Integer>> gold : this.map.entrySet()) {
            for (Map.Entry<String, Integer> predicted : gold.getValue().entrySet()) {
                int value = predicted.getValue();

                // negative value on diagonal
                if (gold.getKey().equals(predicted.getKey())) {
                    result.increaseValue(gold.getKey(), predicted.getKey(), -value);
                }
                else {
                    // zeros elsewhere
                    result.increaseValue(gold.getKey(), predicted.getKey(), 0);
                }
            }
        }

        return result;
    }

    /**
     * Confusion matrix printed to text by toString can be parsed back
     *
     * @param text input text
     * @return confusion matrix
     * @throws IllegalArgumentException if input is malformed
     */
    public static ConfusionMatrix parseFromText(String text)
            throws IllegalArgumentException
    {
        try {

            String[] lines = text.split("\n");
            String[] l = lines[0].split("\\s+");

            List<String> labels = new ArrayList<>();
            for (String aL : l) {
                if (!aL.isEmpty()) {
                    labels.add(aL);
                }
            }

            ConfusionMatrix result = new ConfusionMatrix();

            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];

                String[] split = line.split("\\s+");

                List<String> row = new ArrayList<>();
                for (String aSplit : split) {
                    if (!aSplit.isEmpty()) {
                        row.add(aSplit);
                    }
                }

                String predictedLabel = row.get(0);

                for (int r = 1; r < row.size(); r++) {
                    String s = row.get(r);
                    Integer val = Integer.valueOf(s);

                    String acutalLabel = labels.get(r - 1);

                    result.increaseValue(predictedLabel, acutalLabel, val);
                }
            }

            return result;
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Wrong input format", e);
        }
    }

    /**
     * Returns a distribution of classes in gold data and predicted data (absolute
     * and relative)
     *
     * @return string
     */
    public String printClassDistributionGold()
    {
        StringBuilder sb = new StringBuilder("Gold data distribution\t\t");
        sb.append("Predicted data distribution\n");
        for (String goldLabel : this.allGoldLabels) {
            int rowSum = getRowSum(goldLabel);
            int colSum = getColSum(goldLabel);

            sb.append(String.format(Locale.ENGLISH, "%s\t%d\t%.1f", goldLabel, rowSum,
                    (double) rowSum / (double) getTotalSum() * 100.0));
            sb.append("%\t");
            sb.append(String.format(Locale.ENGLISH, "%d\t%.1f", colSum,
                    (double) colSum / (double) getTotalSum() * 100.0));
            sb.append("%\n");
        }
        sb.append(String.format(Locale.ENGLISH, "Sum\t%d%n", getTotalSum()));
        return sb.toString().trim();
    }

    /**
     * Makes probabilistic confusion matrix, where the entries are real numbers sum up to 1 for
     * each gold row
     *
     * @return table
     */
    protected List<List<String>> prepareToStringProbabilistic()
    {
        // adding zeros
        for (String row : allGoldLabels) {
            if (!map.containsKey(row)) {
                map.put(row, new TreeMap<String, Integer>());
            }

            for (String col : allPredictedLabels) {
                if (!map.get(row).containsKey(col)) {
                    map.get(row).put(col, 0);
                }
            }
        }

        List<List<String>> result = new ArrayList<>();

        List<String> allPredictedLabelsSorted = new ArrayList<>();
        TreeSet<String> extraPredictedLabels = new TreeSet<>(allPredictedLabels);
        extraPredictedLabels.removeAll(allGoldLabels);

        allPredictedLabelsSorted.addAll(allGoldLabels);
        allPredictedLabelsSorted.addAll(extraPredictedLabels);

        // header
        List<String> header = new ArrayList<>();
        header.add("↓gold\\pred→");
        header.addAll(allPredictedLabelsSorted);
        result.add(header);

        for (String rowLabel : allGoldLabels) {
            List<String> row = new ArrayList<>();
            row.add(rowLabel);

            double rowSum = getRowSum(rowLabel);

            for (String predictedLabel : allPredictedLabelsSorted) {
                double value = 0;

                if (this.map.containsKey(rowLabel) && this.map.get(rowLabel)
                        .containsKey(predictedLabel)) {
                    value = this.map.get(rowLabel).get(predictedLabel) / rowSum;
                }
                row.add(String.format(locale, getFormat(), value));
            }

            result.add(row);
        }

        return result;
    }

    /**
     * Returns probabilistic confusion matrix (as table String)
     *
     * @return string
     */
    public String toStringProbabilistic()
    {
        List<List<String>> table = prepareToStringProbabilistic();

        return tableToString(table);
    }

}