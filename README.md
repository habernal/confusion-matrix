# confusion-matrix

A minimalistic Java implementation of confusion matrix for evaluating learning algorithms, including accuracy, macro F-measure, Cohen's Kappa, and probabilistic confusion matrix.

(c) Ivan Habernal

Licenced under ASL 2.0

## Installation

Add this Maven dependency

```
<dependency>
  <groupId>com.github.habernal</groupId>
  <artifactId>confusion-matrix</artifactId>
  <version>1.0</version>
</dependency>
```

## Usage

An example from http://www.compumine.se/web/public/newsletter/20071/precision-recall

```
     A  B  C
A  	25 	5 	2
B  	3 	32 	4
C  	1 	0 	15
```

```java
ConfusionMatrix cm = new ConfusionMatrix();

cm.increaseValue("neg", "neg", 25);
cm.increaseValue("neg", "neu", 5);
cm.increaseValue("neg", "pos", 2);
cm.increaseValue("neu", "neg", 3);
cm.increaseValue("neu", "neu", 32);
cm.increaseValue("neu", "pos", 4);
cm.increaseValue("pos", "neg", 1);
cm.increaseValue("pos", "pos", 15);

System.out.println(cm);
System.out.println(cm.printLabelPrecRecFm());
System.out.println(cm.getPrecisionForLabels());
System.out.println(cm.getRecallForLabels());
System.out.println(cm.printNiceResults());
```

For other examples, see the JUnit Test in `com.github.habernal.confusionmatrix.ConfusionMatrixTest`