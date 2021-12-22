package com.github.habernal.confusionmatrix;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Ivan Habernal
 */
public class ConfusionMatrixTest {

	static ConfusionMatrix confusionMatrix;

	/**
	 * <pre>
	 *      A  B  C
	 *  A  	25 	5 	2
	 * B  	3 	32 	4
	 * C  	1 	0 	15
	 * </pre>
	 * <p/>
	 * <p/>
	 * example from http://www.compumine.se/web/public/newsletter/20071/precision-recall
	 */
	@BeforeClass
	public static void setUpBeforeClass() {
		confusionMatrix = new ConfusionMatrix();

		confusionMatrix.increaseValue("neg", "neg", 25);
		confusionMatrix.increaseValue("neg", "neu", 5);
		confusionMatrix.increaseValue("neg", "pos", 2);
		confusionMatrix.increaseValue("neu", "neg", 3);
		confusionMatrix.increaseValue("neu", "neu", 32);
		confusionMatrix.increaseValue("neu", "pos", 4);
		confusionMatrix.increaseValue("pos", "neg", 1);
		confusionMatrix.increaseValue("pos", "pos", 15);

		//		System.out.println(confusionMatrix);
	}

	@Deprecated
	public static ConfusionMatrix getMatrixForWeightedKappa() {
		ConfusionMatrix cm = new ConfusionMatrix();

		cm.increaseValue("poor", "poor", 0);
		cm.increaseValue("poor", "fair", 1);
		cm.increaseValue("poor", "good", 2);
		cm.increaseValue("poor", "excellent", 3);
		cm.increaseValue("fair", "poor", 1);
		cm.increaseValue("fair", "fair", 0);
		cm.increaseValue("fair", "good", 1);
		cm.increaseValue("fair", "excellent", 2);
		cm.increaseValue("good", "poor", 2);
		cm.increaseValue("good", "fair", 1);
		cm.increaseValue("good", "good", 0);
		cm.increaseValue("good", "excellent", 1);
		cm.increaseValue("excellent", "poor", 3);
		cm.increaseValue("excellent", "fair", 2);
		cm.increaseValue("excellent", "good", 2);
		cm.increaseValue("excellent", "excellent", 0);

		return cm;
	}

	@Test
	public void testGetRowSum()
			throws Exception {
		assertEquals(32, confusionMatrix.getRowSum("neg"));
	}

	@Test
	public void testGetColSum()
			throws Exception {
		assertEquals(29, confusionMatrix.getColSum("neg"));
	}

	@Test
	public void testPrecision()
			throws Exception {
		assertEquals(0.86, confusionMatrix.getPrecisionForLabels().get("neg"), 0.01);
	}

	@Test
	public void testRecall()
			throws Exception {
		assertEquals(0.78, confusionMatrix.getRecallForLabels().get("neg"), 0.01);
	}

	@Test
	public void testMicroFMeasure()
			throws Exception {
		System.out.println(confusionMatrix.getMicroFMeasure());

		assertEquals(confusionMatrix.getAccuracy(), confusionMatrix.getMicroFMeasure(), 0.01);
	}

	@Test
	public void testMacroFMeasure()
			throws Exception {
		System.out.println(confusionMatrix.getMacroFMeasure());
	}

	@Test
	public void testConfidence()
			throws Exception {
		System.out.println(confusionMatrix.getConfidence95Accuracy());
	}

	/*
	 * Example from http://www-users.york.ac.uk/~mb55/msc/clinimet/week4/kappash2.pdf Table 6, pg. 4
	 */
	@Test
	public void testCohensKappa()
			throws Exception {
		ConfusionMatrix cm = new ConfusionMatrix();

		cm.increaseValue("poor", "poor", 2);
		cm.increaseValue("poor", "fair", 12);
		cm.increaseValue("poor", "good", 8);
		cm.increaseValue("fair", "poor", 9);
		cm.increaseValue("fair", "fair", 35);
		cm.increaseValue("fair", "good", 43);
		cm.increaseValue("fair", "excellent", 7);
		cm.increaseValue("good", "poor", 4);
		cm.increaseValue("good", "fair", 36);
		cm.increaseValue("good", "good", 103);
		cm.increaseValue("good", "excellent", 40);
		cm.increaseValue("excellent", "poor", 1);
		cm.increaseValue("excellent", "fair", 8);
		cm.increaseValue("excellent", "good", 36);
		cm.increaseValue("excellent", "excellent", 22);

		System.out.println(cm);

		System.out.println(cm.getCohensKappa());
	}

	/*
	www.itc.nl/~rossiter/teach/R/R_ac.pdf page 11
	 */
	@Test
	public final void testConfidenceInterval() {
		ConfusionMatrix cm = new ConfusionMatrix();

		cm.increaseValue("A", "A", 35);
		cm.increaseValue("A", "B", 14);
		cm.increaseValue("A", "C", 11);
		cm.increaseValue("A", "D", 1);
		cm.increaseValue("B", "A", 4);
		cm.increaseValue("B", "B", 11);
		cm.increaseValue("B", "C", 3);
		//		cm.increaseValue("B", "D", 0);
		cm.increaseValue("C", "A", 12);
		cm.increaseValue("C", "B", 9);
		cm.increaseValue("C", "C", 38);
		cm.increaseValue("C", "D", 4);
		cm.increaseValue("D", "A", 2);
		cm.increaseValue("D", "B", 5);
		cm.increaseValue("D", "C", 12);
		cm.increaseValue("D", "D", 2);

		System.out.println(cm);

		assertEquals(cm.getAccuracy(), 0.5276, 0.0001);
		assertEquals(cm.getConfidence95AccuracyLow(), 0.4479, 0.005);
		assertEquals(cm.getConfidence95AccuracyHigh(), 0.6073, 0.005);

		System.out.println(cm.printClassDistributionGold());
	}

	@Test
	public void testMoreExpectedLabelsThanGold()
			throws Exception {
		ConfusionMatrix cm = new ConfusionMatrix();
		cm.increaseValue("1", "1");
		cm.increaseValue("1", "2");
		cm.increaseValue("2", "2");
		cm.increaseValue("2", "3");

		System.out.println(cm);
		System.out.println(cm.printLabelPrecRecFm());
		System.out.println(cm.getPrecisionForLabels());
		System.out.println(cm.getRecallForLabels());
		System.out.println(cm.printNiceResults());

	}

	@Test
	public void testParseFromText()
			throws Exception {
		String s = "                NEG       NEU       POS\n" +
				"       NEG        25         5         2\n" +
				"       NEU         3        32         4\n" +
				"       POS         1         0        15";

		ConfusionMatrix cm = ConfusionMatrix.parseFromText(s);
		System.out.println(cm);

		s = "NEG       NEU       POS\n" +
				"       NEG      6977      1850      1560\n" +
				"       NEU      1200     20083     10660\n" +
				"       POS       547      4960     97470\n";

		System.out.println(ConfusionMatrix.parseFromText(s).getConfidence95MacroFM());

		s = "NEG       NEU       POS\n" +
				"       NEG     23350      4790      1576\n" +
				"       NEU      5650     22573      2545\n" +
				"       POS      2220      2549     26128\n";

		ConfusionMatrix confusionMatrix1 = ConfusionMatrix.parseFromText(s);
		confusionMatrix1.setNumberOfDecimalPlaces(5);
		System.out.println(confusionMatrix1.printNiceResults());
		System.out.println(confusionMatrix1.getConfidence95MacroFM());

		s = " NEG       POS\n" +
				"       NEG     11157      1343\n" +
				"       POS      1292     11208\n";

		ConfusionMatrix c = ConfusionMatrix.parseFromText(s);
		c.setNumberOfDecimalPlaces(5);
		System.out.println(c);
		System.out.println(c.getConfidence90MacroFM());
		System.out.println(c.getConfidence95MacroFM());
	}

	@Test
	public void testOther()
			throws Exception {
		ConfusionMatrix cmB = new ConfusionMatrix();
		cmB.increaseValue("corr", "corr", 0);
		cmB.increaseValue("corr", "non-corr", 77 + 51);
		cmB.increaseValue("non-corr", "corr", 0);
		cmB.increaseValue("non-corr", "non-corr", 488 + 41);

		System.out.println(cmB);
		System.out.println(cmB.printLabelPrecRecFm());
		System.out.println(cmB.printNiceResults());

		System.out.println("Rand. forest");

		ConfusionMatrix cmRF = new ConfusionMatrix();
		cmRF.increaseValue("corr", "corr", 77);
		cmRF.increaseValue("corr", "non-corr", 51);
		cmRF.increaseValue("non-corr", "corr", 41);
		cmRF.increaseValue("non-corr", "non-corr", 488);

		System.out.println(cmRF);

		System.out.println(cmRF.printLabelPrecRecFm());
		System.out.println(cmRF.printNiceResults());
	}

	@Test
	@Ignore
	// not really a test
	public void testSomething() {
		{
			ConfusionMatrix cm = new ConfusionMatrix();
			cm.increaseValue("1", "1");
			cm.increaseValue("1", "1");
			cm.increaseValue("1", "1");
			cm.increaseValue("1", "1");
			cm.increaseValue("1", "1");
			cm.increaseValue("1", "1");
			cm.increaseValue("1", "2");
			cm.increaseValue("1", "1");
			cm.increaseValue("1", "1");
			cm.increaseValue("1", "3");
			cm.increaseValue("1", "2");
			cm.increaseValue("3", "3");
			cm.increaseValue("1", "3");
			cm.increaseValue("1", "3");
			cm.increaseValue("3", "3");
			cm.increaseValue("1", "1");
			cm.increaseValue("1", "1");
			cm.increaseValue("1", "1");
			cm.increaseValue("3", "3");
			cm.increaseValue("1", "1");
			cm.increaseValue("3", "3");
			cm.increaseValue("1", "1");
			cm.increaseValue("1", "3");
			cm.increaseValue("1", "1");
			cm.increaseValue("1", "1");
			cm.increaseValue("3", "3");
			cm.increaseValue("3", "3");
			cm.increaseValue("1", "3");
			cm.increaseValue("1", "1");
			cm.increaseValue("1", "1");
			cm.increaseValue("1", "1");
			cm.increaseValue("1", "3");
			cm.increaseValue("1", "1");
			cm.increaseValue("1", "1");
			cm.increaseValue("3", "3");
			cm.increaseValue("3", "3");
			cm.increaseValue("1", "3");
			cm.increaseValue("1", "1");
			cm.increaseValue("3", "3");
			cm.increaseValue("1", "1");
			cm.increaseValue("1", "1");
			cm.increaseValue("3", "3");
			cm.increaseValue("3", "3");
			cm.increaseValue("1", "1");
			cm.increaseValue("3", "3");
			cm.increaseValue("1", "1");
			cm.increaseValue("1", "1");
			cm.increaseValue("1", "3");
			cm.increaseValue("1", "2");
			cm.increaseValue("2", "3");

			System.out.println(cm);
			System.out.println(cm.getCohensKappa());
			System.out.println(cm.printLabelPrecRecFm());
			System.out.println(cm.printNiceResults());
		}

		{
			// after corrections
			ConfusionMatrix cm = new ConfusionMatrix();
			cm.increaseValue("1", "1", 30);
			cm.increaseValue("3", "3", 12);

			cm.increaseValue("1", "2", 5);
			//            cm.increaseValue("1", "1", 5);

			cm.increaseValue("1", "3", 3);
			cm.increaseValue("2", "3", 1);
			//            cm.increaseValue("3", "3", 1);

			System.out.println(cm);
			System.out.println(cm.getCohensKappa());
			System.out.println(cm.printLabelPrecRecFm());
			System.out.println(cm.printNiceResults());
		}

		{
			// new
			ConfusionMatrix cm = new ConfusionMatrix();
			cm.increaseValue("1", "1", 4);
			cm.increaseValue("3", "3", 4);
			cm.increaseValue("1", "3");
			cm.increaseValue("2", "3");

			System.out.println(cm);
			System.out.println(cm.getCohensKappa());
			System.out.println(cm.printLabelPrecRecFm());
			System.out.println(cm.printNiceResults());
		}

		{
			// new after correction
			ConfusionMatrix cm = new ConfusionMatrix();
			cm.increaseValue("1", "1", 4);
			cm.increaseValue("3", "3", 5);
			cm.increaseValue("1", "3");

		}

		{
			ConfusionMatrix cm = new ConfusionMatrix();
			cm.increaseValue("1", "1", 750);
			cm.increaseValue("1", "2", 250);
			cm.increaseValue("2", "1", 500);
			cm.increaseValue("2", "2", 500);

			System.out.println(cm);
			System.out.println(cm.printLabelPrecRecFm());
			System.out.println(cm.printNiceResults());

		}
		{
			ConfusionMatrix cm = new ConfusionMatrix();
			cm.increaseValue("T", "T", 67);
			cm.increaseValue("T", "H", 33);
			cm.increaseValue("H", "H", 0);

			System.out.println(cm);
			System.out.println(cm.printLabelPrecRecFm());
			System.out.println(cm.printNiceResults());
			System.out.println(cm.getAccuracy() + " " + cm.getConfidence95AccuracyLow() + " " + cm
					.getConfidence95AccuracyHigh());
			System.out.println(cm.getAccuracy() + " " + cm.getConfidence90AccuracyLow() + " " + cm
					.getConfidence90AccuracyHigh());
		}
		{
			ConfusionMatrix cm = new ConfusionMatrix();
			cm.increaseValue("T", "T", 55);
			cm.increaseValue("T", "H", 45);
			cm.increaseValue("H", "H", 0);

			System.out.println(cm);
			System.out.println(cm.printLabelPrecRecFm());
			System.out.println(cm.printNiceResults());
			System.out.println(cm.getAccuracy() + " " + cm.getConfidence95AccuracyLow() + " " + cm
					.getConfidence95AccuracyHigh());
			System.out.println(cm.getAccuracy() + " " + cm.getConfidence90AccuracyLow() + " " + cm
					.getConfidence90AccuracyHigh());

		}

	}

	@Test
	public void testAdd()
			throws Exception {
		ConfusionMatrix add = ConfusionMatrix.createCumulativeMatrix(confusionMatrix);

		System.out.println(confusionMatrix);
		System.out.println(add);

		ConfusionMatrix add2 = ConfusionMatrix
				.createCumulativeMatrix(confusionMatrix, confusionMatrix);

		System.out.println(confusionMatrix);
		System.out.println(add2);

	}

	@Test
	public void testMatrix()
			throws Exception {
		ConfusionMatrix cm = new ConfusionMatrix();
		cm.increaseValue("yes", "yes");
		cm.increaseValue("yes", "no");

		System.out.println(cm);
	}

	@Test
	public void testToStringProbabilistic()
			throws Exception {
		System.out.println(confusionMatrix.toStringProbabilistic());
	}

	@Test
	public void testProbabilisticThreeAnnotators() {
		// agreement between annotator1 and annotator2
		ConfusionMatrix a1a2 = new ConfusionMatrix();
		a1a2.increaseValue("after", "before");
		a1a2.increaseValue("after", "after", 2);
		a1a2.increaseValue("before", "thus");

		System.out.println(a1a2);

		// agreement between annotator1 and annotator3
		ConfusionMatrix a1a3 = new ConfusionMatrix();
		a1a3.increaseValue("after", "before", 2);
		a1a3.increaseValue("after", "after", 6);
		a1a3.increaseValue("thus", "after");
		a1a3.increaseValue("before", "thus", 2);

		System.out.println(a1a3);

		// agreement between annotator2 and annotator2
		ConfusionMatrix a2a3 = new ConfusionMatrix();
		a2a3.increaseValue("after", "after", 3);
		a2a3.increaseValue("thus", "before", 2);
		a2a3.increaseValue("before", "thus");

		System.out.println(a2a3);

		ConfusionMatrix cumulativeMatrix = ConfusionMatrix.createCumulativeMatrix(a1a2, a1a3, a2a3);

		System.out.println("Cumulative matrix");
		System.out.println(cumulativeMatrix);

		System.out.println("Probabilistic");
		System.out.println(cumulativeMatrix.toStringProbabilistic());
	}

	@Test
	public void testBug()
			throws Exception {
        /*
        ↓gold\pred→               ""        "bec_aft" "in other words"   "specifically"         "though"           "thus"         "before"
               ""                9                5                0                5                1                2                1
        "bec_aft"                1                7                0                0                0                1                1
 "in other words"                0                1                1                0                0                0                0
   "specifically"                0                0                0                1                0                0                0
         "though"                0                0                0                0                0                0                1
           "thus"                0                2                0                0                0                1                0

      ↓gold\pred→               ""        "bec_aft" "in other words"   "specifically"         "though"           "thus"         "before"
               ""               15                2                3                0                0                3                0
        "bec_aft"                2                5                0                0                0                2                1
 "in other words"                0                0                0                1                0                1                0
   "specifically"                0                0                0                1                0                0                0
         "though"                1                0                0                0                0                0                0
           "thus"                0                1                0                0                0                2                0

      ↓gold\pred→               ""        "bec_aft"         "before" "in other words"   "specifically"         "though"           "thus"
               ""                9                0                0                1                0                0                0
        "bec_aft"                4                7                0                0                1                0                3
         "before"                2                0                1                0                0                0                0
 "in other words"                0                0                0                0                0                0                1
   "specifically"                2                1                0                1                1                0                1
         "though"                0                0                0                1                0                0                0
           "thus"                1                0                0                0                0                0                3
         */

		ConfusionMatrix a1a2 = ConfusionMatrix.parseFromText(
				"              \"\"        \"bec_aft\" \"in_other_words\"   \"specifically\"         \"though\"           \"thus\"         \"before\"\n"
						+ "               \"\"                9                5                0                5                1                2                1\n"
						+ "        \"bec_aft\"                1                7                0                0                0                1                1\n"
						+ " \"in_other_words\"                0                1                1                0                0                0                0\n"
						+ "   \"specifically\"                0                0                0                1                0                0                0\n"
						+ "         \"though\"                0                0                0                0                0                0                1\n"
						+ "           \"thus\"                0                2                0                0                0                1                0");

		ConfusionMatrix a1a3 = ConfusionMatrix.parseFromText(
				"     \"\"        \"bec_aft\" \"in_other_words\"   \"specifically\"         \"though\"           \"thus\"         \"before\"\n"
						+ "               \"\"               15                2                3                0                0                3                0\n"
						+ "        \"bec_aft\"                2                5                0                0                0                2                1\n"
						+ " \"in_other_words\"                0                0                0                1                0                1                0\n"
						+ "   \"specifically\"                0                0                0                1                0                0                0\n"
						+ "         \"though\"                1                0                0                0                0                0                0\n"
						+ "           \"thus\"                0                1                0                0                0                2                0\n");

		ConfusionMatrix a2a3 = ConfusionMatrix.parseFromText(
				"               \"\"        \"bec_aft\"         \"before\" \"in_other_words\"   \"specifically\"         \"though\"           \"thus\"\n"
						+ "               \"\"                9                0                0                1                0                0                0\n"
						+ "        \"bec_aft\"                4                7                0                0                1                0                3\n"
						+ "         \"before\"                2                0                1                0                0                0                0\n"
						+ " \"in_other_words\"                0                0                0                0                0                0                1\n"
						+ "   \"specifically\"                2                1                0                1                1                0                1\n"
						+ "         \"though\"                0                0                0                1                0                0                0\n"
						+ "           \"thus\"                1                0                0                0                0                0                3");

		System.out.println(a1a2);
		System.out.println(a1a3);
		System.out.println(a2a3);

		ConfusionMatrix cm = ConfusionMatrix
				.createCumulativeMatrix(a1a2.getSymmetricConfusionMatrix(),
						a1a3.getSymmetricConfusionMatrix(), a2a3.getSymmetricConfusionMatrix());

		System.out.println(cm);

		System.out.println(cm.toStringProbabilistic());

	}

	/*
	 * Testing on tables from
	 * Cinkova, S., Holub, M., & Kriz, V. (2012). Managing Uncertainty in Semantic Tagging.
	 * In Proceedings of the 13th Conference of the European Chapter of the Association for
	 * Computational Linguistics (pp. 840–850). Avignon, France: Association for Computational
	 * Linguistics. Retrieved from http://www.aclweb.org/anthology/E12-1085
	 */
	@Test
	public void testProbabilityConfusionMatrixCinkovaEtAl2012()
			throws Exception {
		ConfusionMatrix a1vsa2 = ConfusionMatrix.parseFromText(
				"1 1.a 2 4 5\n1 29 1 1 0 0\n1.a 0 1 0 0 0\n2 0 1 11 0 0\n4 0 0 0 2 0\n5 0 0 0 3 1");

		ConfusionMatrix a1vsa3 = ConfusionMatrix.parseFromText(
				"1 1.a 2 4 5\n1 29 2 0 0 0\n1.a 1 0 0 0 0\n2 0 0 12 0 0\n4 0 0 0 1 1\n5 0 0 0 0 4");

		ConfusionMatrix a2vsa3 = ConfusionMatrix.parseFromText(
				"1 1.a 2 4 5\n1 27 2 0 0 0\n1.a 2 0 1 0 0\n2 1 0 11 0 0\n4 0 0 0 1 4\n5 0 0 0 0 1");

		ConfusionMatrix cm = ConfusionMatrix
				.createCumulativeMatrix(a1vsa2.getSymmetricConfusionMatrix(),
						a1vsa3.getSymmetricConfusionMatrix(), a2vsa3.getSymmetricConfusionMatrix());

		// Table 2a: Aggregated Confusion Matrix
		// from Cinkova et al., 2012
		ConfusionMatrix expected = ConfusionMatrix.parseFromText(
				"1 1.a 2 4 5\n 1 85 8 2 0 0\n 1.a 8 1 2 0 0\n 2 2 2 34 0 0\n"
						+ " 4 0 0 0 4 8\n 5 0 0 0 8 6");

		assertEquals(expected.toString(), cm.toString());

		// Table 4: Confusion Probability Matrix
		List<List<String>> prepareToStringProbabilistic = cm.prepareToStringProbabilistic();

		assertEquals(Arrays.asList("1", "0.895", "0.084", "0.021", "0.000", "0.000"),
				prepareToStringProbabilistic.get(1));
		assertEquals(Arrays.asList("1.a", "0.727", "0.091", "0.182", "0.000", "0.000"),
				prepareToStringProbabilistic.get(2));
		assertEquals(Arrays.asList("2", "0.053", "0.053", "0.895", "0.000", "0.000"),
				prepareToStringProbabilistic.get(3));
		assertEquals(Arrays.asList("4", "0.000", "0.000", "0.000", "0.333", "0.667"),
				prepareToStringProbabilistic.get(4));
		assertEquals(Arrays.asList("5", "0.000", "0.000", "0.000", "0.571", "0.429"),
				prepareToStringProbabilistic.get(5));
	}

	@Test
	public void testNegativeUnitMatrix()
			throws Exception {
		ConfusionMatrix cm1 = new ConfusionMatrix();
		cm1.increaseValue("1", "1", 1);
		cm1.increaseValue("1", "2", 2);
		cm1.increaseValue("2", "1", 3);
		cm1.increaseValue("2", "2", 4);

		ConfusionMatrix negativeUnitMatrix = cm1.getNegativeUnitMatrix();

		assertTrue(-1 == negativeUnitMatrix.map.get("1").get("1"));
		assertTrue(0 == negativeUnitMatrix.map.get("1").get("2"));
		assertTrue(0 == negativeUnitMatrix.map.get("2").get("1"));
		assertTrue(-4 == negativeUnitMatrix.map.get("2").get("2"));
	}

	/*
	 * Transposing confusion matrix from Table 3: A1 vs. A2 from
	 * Cinkova, S., Holub, M., & Kriz, V. (2012). Managing Uncertainty in Semantic Tagging.
	 * In Proceedings of the 13th Conference of the European Chapter of the Association for
	 * Computational Linguistics (pp. 840–850). Avignon, France: Association for Computational
	 * Linguistics. Retrieved from http://www.aclweb.org/anthology/E12-1085
	 */
	@Test
	public void testTransposedMatrix()
			throws Exception {
		ConfusionMatrix a1vsa2 = ConfusionMatrix.parseFromText(
				"1 1.a 2 4 5\n1 29 1 1 0 0\n1.a 0 1 0 0 0\n2 0 1 11 0 0\n4 0 0 0 2 0\n5 0 0 0 3 1");
		System.out.println(a1vsa2);

		ConfusionMatrix t = a1vsa2.getTransposedMatrix();

		assertEquals(29, (long) t.map.get("1").get("1"));
		assertEquals(0, (long) t.map.get("1").get("1.a"));
		assertEquals(0, (long) t.map.get("1").get("2"));
		assertEquals(0, (long) t.map.get("1").get("4"));
		assertEquals(0, (long) t.map.get("1").get("5"));

		assertEquals(1, (long) t.map.get("1.a").get("1"));
		assertEquals(1, (long) t.map.get("2").get("1"));
		assertEquals(0, (long) t.map.get("4").get("1"));
		assertEquals(0, (long) t.map.get("5").get("1"));

		assertEquals(0, (long) t.map.get("4").get("1"));
		assertEquals(0, (long) t.map.get("4").get("1.a"));
		assertEquals(0, (long) t.map.get("4").get("2"));
		assertEquals(2, (long) t.map.get("4").get("4"));
		assertEquals(3, (long) t.map.get("4").get("5"));
	}

	@Test
	public void testSymmetricConfusionMatrixA1A2()
			throws Exception {
		ConfusionMatrix a1vsa2 = ConfusionMatrix.parseFromText(
				"1 1.a 2 4 5\n1 29 1 1 0 0\n1.a 0 1 0 0 0\n2 0 1 11 0 0\n4 0 0 0 2 0\n5 0 0 0 3 1");
		System.out.println(a1vsa2);

		ConfusionMatrix s = a1vsa2.getSymmetricConfusionMatrix();

		assertEquals(1, (long) s.map.get("1.a").get("1"));
		assertEquals(1, (long) s.map.get("1").get("1.a"));
		assertEquals(29, (long) s.map.get("1").get("1"));
	}

	@Test
	public void testSymmetricConfusionMatrixA1A3()
			throws Exception {
		ConfusionMatrix a1vsa3 = ConfusionMatrix.parseFromText(
				"1 1.a 2 4 5\n1 29 2 0 0 0\n1.a 1 0 0 0 0\n2 0 0 12 0 0\n4 0 0 0 1 1\n5 0 0 0 0 4");
		System.out.println(a1vsa3);
		ConfusionMatrix s = a1vsa3.getSymmetricConfusionMatrix();
		System.out.println(s);

		assertEquals(3, (long) s.map.get("1.a").get("1"));
		assertEquals(3, (long) s.map.get("1").get("1.a"));
		assertEquals(29, (long) s.map.get("1").get("1"));
	}

	/**
	 * Table 1 from Forman, G., & Scholz, M. (2010). Apples-to-Apples in Cross-Validation Studies:
	 * Pitfalls in Classifier Performance Measurement. ACM SIGKDD Explorations Newsletter, 12(1),
	 * 49–57.
	 *
	 * @throws Exception
	 */
	@Test
	public void testApplesToApples()
			throws Exception {
		ConfusionMatrix f1 = new ConfusionMatrix();
		f1.increaseValue("pos", "pos", 3);
		f1.increaseValue("neg", "neg", 373);
		double pf1 = f1.getPrecisionForLabel("pos");
		double rf1 = f1.getRecallForLabel("pos");
		double fmf1 = f1.getFMeasureForLabels().get("pos");

		System.out.println("------\nFold 1 confusion matrix");
		System.out.println(f1);
		System.out.println("Precision (pos): " + pf1);
		System.out.println("Recall (pos): " + rf1);
		System.out.println("F-measure (pos): " + fmf1);

		ConfusionMatrix f2 = new ConfusionMatrix();
		f2.increaseValue("pos", "pos", 4);
		f2.increaseValue("neg", "pos", 1);
		f2.increaseValue("neg", "neg", 372);
		double pf2 = f2.getPrecisionForLabel("pos");
		double rf2 = f2.getRecallForLabel("pos");
		double fmf2 = f2.getFMeasureForLabels().get("pos");

		System.out.println("------\nFold 2 confusion matrix");
		System.out.println(f2);
		System.out.println("Precision (pos): " + pf2);
		System.out.println("Recall (pos): " + rf2);
		System.out.println("F-measure (pos): " + fmf2);

		ConfusionMatrix f3 = new ConfusionMatrix();
		f3.increaseValue("pos", "pos", 4);
		f3.increaseValue("neg", "pos", 13);
		f3.increaseValue("neg", "neg", 372);
		double pf3 = f3.getPrecisionForLabel("pos");
		double rf3 = f3.getRecallForLabel("pos");
		double fmf3 = f3.getFMeasureForLabels().get("pos");

		System.out.println("------\nFold 3 confusion matrix");
		System.out.println(f3);
		System.out.println("Precision (pos): " + pf3);
		System.out.println("Recall (pos): " + rf3);
		System.out.println("F-measure (pos): " + fmf3);

		ConfusionMatrix f4 = new ConfusionMatrix();
		f4.increaseValue("pos", "pos", 3);
		f4.increaseValue("pos", "neg", 1);
		f4.increaseValue("neg", "pos", 5);
		f4.increaseValue("neg", "neg", 372);
		double pf4 = f4.getPrecisionForLabel("pos");
		double rf4 = f4.getRecallForLabel("pos");
		double fmf4 = f4.getFMeasureForLabels().get("pos");

		System.out.println("------\nFold 4 confusion matrix");
		System.out.println(f4);
		System.out.println("Precision (pos): " + pf4);
		System.out.println("Recall (pos): " + rf4);
		System.out.println("F-measure (pos): " + fmf4);

		// average f-measure
		double fAvg = (fmf1 + fmf2 + fmf3 + fmf4) / 4.0;
		System.out.println("F_avg: " + fAvg);

		// average precision and recall first
		double pAvg = (pf1 + pf2 + pf3 + pf4) / 4.0;
		double rAvg = (rf1 + rf2 + rf3 + rf4) / 4.0;
		System.out.println("Average precision: " + pAvg);
		System.out.println("Average recall: " + rAvg);
		// compute F-measure
		double fPrRe = (2 * pAvg * rAvg) / (pAvg + rAvg);
		System.out.println("F_pr,re: " + fPrRe);

		// and single confusion matrix only
		ConfusionMatrix cm = new ConfusionMatrix();
		cm.increaseValue("pos", "pos", 3);
		cm.increaseValue("neg", "neg", 373);
		cm.increaseValue("pos", "pos", 4);
		cm.increaseValue("neg", "pos", 1);
		cm.increaseValue("neg", "neg", 372);
		cm.increaseValue("pos", "pos", 4);
		cm.increaseValue("neg", "pos", 13);
		cm.increaseValue("neg", "neg", 372);
		cm.increaseValue("pos", "pos", 3);
		cm.increaseValue("pos", "neg", 1);
		cm.increaseValue("neg", "pos", 5);
		cm.increaseValue("neg", "neg", 372);
		double p = cm.getPrecisionForLabel("pos");
		double r = cm.getRecallForLabel("pos");
		double fm = cm.getFMeasureForLabels().get("pos");

		System.out.println("------\nCombined confusion matrix");
		System.out.println(cm);
		System.out.println("Precision (pos): " + p);
		System.out.println("Recall (pos): " + r);
		System.out.println("F-measure (pos) F_tp,fp: " + fm);

	}

	@Test
	public void testGithubIssue3() {
		ConfusionMatrix cf = new ConfusionMatrix();

		cf.increaseValue("100", "48");
		cf.increaseValue("100", "87");
		cf.increaseValue("104", "104", 15);
		cf.increaseValue("104", "44");
		cf.increaseValue("104", "44");
		cf.increaseValue("104", "48");
		cf.increaseValue("104", "49", 6);
		cf.increaseValue("104", "87");
		cf.increaseValue("106", "48");
		cf.increaseValue("106", "49");
		cf.increaseValue("106", "49");
		cf.increaseValue("11", "10");
		cf.increaseValue("11", "10");
		cf.increaseValue("11", "22");
		cf.increaseValue("11", "56");
		cf.increaseValue("11", "60");
		cf.increaseValue("11", "62");
		cf.increaseValue("111", "56");
		cf.increaseValue("115", "104");
		cf.increaseValue("115", "104");
		cf.increaseValue("116", "44");
		cf.increaseValue("117", "89");
		cf.increaseValue("12", "7");
		cf.increaseValue("12", "7");
		cf.increaseValue("13", "14");
		cf.increaseValue("13", "14");
		cf.increaseValue("13", "14");
		cf.increaseValue("13", "26");
		cf.increaseValue("13", "26");
		cf.increaseValue("13", "7");
		cf.increaseValue("17", "71");
		cf.increaseValue("17", "71");
		cf.increaseValue("18", "19");
		cf.increaseValue("18", "56");
		cf.increaseValue("2", "2");
		cf.increaseValue("2", "7");
		cf.increaseValue("20", "87");
		cf.increaseValue("22", "22");
		cf.increaseValue("25", "32");
		cf.increaseValue("29", "76");
		cf.increaseValue("30", "104");
		cf.increaseValue("30", "30");
		cf.increaseValue("30", "30");
		cf.increaseValue("30", "30");
		cf.increaseValue("30", "56");
		cf.increaseValue("30", "63");
		cf.increaseValue("30", "7");
		cf.increaseValue("30", "7");
		cf.increaseValue("31", "3");
		cf.increaseValue("31", "35");
		cf.increaseValue("31", "65");
		cf.increaseValue("31", "8");
		cf.increaseValue("33", "44");
		cf.increaseValue("33", "44");
		cf.increaseValue("33", "49");
		cf.increaseValue("33", "49");
		cf.increaseValue("34", "104");
		cf.increaseValue("34", "34", 5);
		cf.increaseValue("34", "48");
		cf.increaseValue("34", "76");
		cf.increaseValue("34", "76");
		cf.increaseValue("34", "76");
		cf.increaseValue("35", "34");
		cf.increaseValue("35", "34");
		cf.increaseValue("36", "37");
		cf.increaseValue("39", "40");
		cf.increaseValue("39", "40");
		cf.increaseValue("4", "19");
		cf.increaseValue("4", "19");
		cf.increaseValue("4", "19");
		cf.increaseValue("4", "30");
		cf.increaseValue("4", "4");
		cf.increaseValue("4", "4");
		cf.increaseValue("4", "60");
		cf.increaseValue("4", "60");
		cf.increaseValue("41", "14");
		cf.increaseValue("41", "19");
		cf.increaseValue("41", "19");
		cf.increaseValue("41", "44");
		cf.increaseValue("41", "56");
		cf.increaseValue("41", "56");
		cf.increaseValue("41", "7");
		cf.increaseValue("41", "76");
		cf.increaseValue("41", "80");
		cf.increaseValue("41", "87");
		cf.increaseValue("41", "89");
		cf.increaseValue("42", "42");
		cf.increaseValue("42", "42");
		cf.increaseValue("43", "10");
		cf.increaseValue("43", "87");
		cf.increaseValue("44", "34");
		cf.increaseValue("44", "44");
		cf.increaseValue("44", "44");
		cf.increaseValue("44", "48");
		cf.increaseValue("44", "49");
		cf.increaseValue("45", "48");
		cf.increaseValue("45", "48");
		cf.increaseValue("45", "51");
		cf.increaseValue("45", "87", 6);
		cf.increaseValue("46", "104");
		cf.increaseValue("47", "104");
		cf.increaseValue("47", "104");
		cf.increaseValue("47", "48");
		cf.increaseValue("47", "48");
		cf.increaseValue("47", "49", 9);
		cf.increaseValue("47", "87");
		cf.increaseValue("48", "34");
		cf.increaseValue("48", "34");
		cf.increaseValue("48", "48", 30);
		cf.increaseValue("48", "87", 5);
		cf.increaseValue("49", "104");
		cf.increaseValue("49", "44");
		cf.increaseValue("49", "48", 4);
		cf.increaseValue("49", "49", 10);
		cf.increaseValue("5", "10");
		cf.increaseValue("5", "10");
		cf.increaseValue("50", "50");
		cf.increaseValue("50", "52");
		cf.increaseValue("52", "50");
		cf.increaseValue("52", "50");
		cf.increaseValue("52", "71");
		cf.increaseValue("54", "25");
		cf.increaseValue("54", "48");
		cf.increaseValue("55", "19");
		cf.increaseValue("55", "7");
		cf.increaseValue("56", "30");
		cf.increaseValue("56", "30");
		cf.increaseValue("56", "49");
		cf.increaseValue("57", "57");
		cf.increaseValue("58", "59");
		cf.increaseValue("59", "66");
		cf.increaseValue("59", "66");
		cf.increaseValue("61", "61");
		cf.increaseValue("62", "62");
		cf.increaseValue("62", "62");
		cf.increaseValue("64", "14");
		cf.increaseValue("64", "62");
		cf.increaseValue("64", "63");
		cf.increaseValue("67", "68");
		cf.increaseValue("68", "42");
		cf.increaseValue("68", "68");
		cf.increaseValue("69", "69");
		cf.increaseValue("7", "7");
		cf.increaseValue("70", "72");
		cf.increaseValue("71", "112");
		cf.increaseValue("71", "15");
		cf.increaseValue("71", "7");
		cf.increaseValue("71", "71");
		cf.increaseValue("71", "72");
		cf.increaseValue("72", "19");
		cf.increaseValue("72", "51");
		cf.increaseValue("72", "62");
		cf.increaseValue("74", "87");
		cf.increaseValue("74", "87");
		cf.increaseValue("76", "7");
		cf.increaseValue("82", "56");
		cf.increaseValue("82", "76");
		cf.increaseValue("92", "42");
		cf.increaseValue("93", "44");
		cf.increaseValue("94", "43");
		cf.increaseValue("94", "44");
		cf.increaseValue("94", "44");
		cf.increaseValue("94", "44");
		cf.increaseValue("94", "76");
		cf.increaseValue("95", "104");
		cf.increaseValue("95", "87");

        System.out.println(cf);

        System.out.println("acc " + cf.getAccuracy());
        System.out.println("microF " + cf.getMicroFMeasure());
	}
}