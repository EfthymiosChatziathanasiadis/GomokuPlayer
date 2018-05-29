// ECS629/759 Assignment 2 - ID3 Skeleton Code
// Author: Simon Dixon

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;

/*
* AUTHOR: EFTHYMIOS CHATZIATHANASIADIS
* STUDENT ID: 150359131
* ECS629 ARTIFICIAL INTELLIGENCE
* Assignment 2 - ID3
*/

class ID3 {

	/** Each node of the tree contains either the attribute number (for non-leaf
	 *  nodes) or class number (for leaf nodes) in <b>value</b>, and an array of
	 *  tree nodes in <b>children</b> containing each of the children of the
	 *  node (for non-leaf nodes).
	 *  The attribute number corresponds to the column number in the training
	 *  and test files. The children are ordered in the same order as the
	 *  Strings in strings[][]. E.g., if value == 3, then the array of
	 *  children correspond to the branches for attribute 3 (named data[0][3]):
	 *      children[0] is the branch for attribute 3 == strings[3][0]
	 *      children[1] is the branch for attribute 3 == strings[3][1]
	 *      children[2] is the branch for attribute 3 == strings[3][2]
	 *      etc.
	 *  The class number (leaf nodes) also corresponds to the order of classes
	 *  in strings[][]. For example, a leaf with value == 3 corresponds
	 *  to the class label strings[attributes-1][3].
	 **/
	class TreeNode {

		TreeNode[] children;
		int value;

		public TreeNode(TreeNode[] ch, int val) {
			value = val;
			children = ch;
		} // constructor

		public String toString() {
			return toString("");
		} // toString()

		String toString(String indent) {
			if (children != null) {
				String s = "";
				for (int i = 0; i < children.length; i++)
					s += indent + data[0][value] + "=" +
							strings[value][i] + "\n" +
							children[i].toString(indent + '\t');
				return s;
			} else
				return indent + "Class: " + strings[attributes-1][value] + "\n";
		} // toString(String)

	} // inner class TreeNode

	private int attributes; 	// Number of attributes (including the class)
	private int examples;		// Number of training examples
	private TreeNode decisionTree;	// Tree learnt in training, used for classifying
	private String[][] data;	// Training data indexed by example, attribute
	private String[][] strings; // Unique strings for each attribute
	private int[] stringCount;  // Number of unique strings for each attribute

	public ID3() {
		attributes = 0;
		examples = 0;
		decisionTree = null;
		data = null;
		strings = null;
		stringCount = null;
	} // constructor

	public void printTree() {
		if (decisionTree == null)
			error("Attempted to print null Tree");
		else
			System.out.println(decisionTree);
	} // printTree()

	/** Print error message and exit. **/
	static void error(String msg) {
		System.err.println("Error: " + msg);
		System.exit(1);
	} // error()

	static final double LOG2 = Math.log(2.0);

	static double xlogx(double x) {
		return x == 0? 0: x * Math.log(x) / LOG2;
	} // xlogx()

	/** Execute the decision tree on the given examples in testData, and print
	 *  the resulting class names, one to a line, for each example in testData.
	 **/
	public void classify(String[][] testData) {
		if (decisionTree == null)
			error("Please run training phase before classification");
		for(int example=1; example<testData.length; example++){
			int cl = classify(testData[example], decisionTree);
			System.out.println(strings[attributes-1][cl]);
		}
	} // classify()

	/*
  *GOAL: classify the input example
  *Input: example, decidion tree
  *Output: class
	* BASE CASE: leaf node reached
  */
	private int classify(String[] example, TreeNode tree){
		int question = tree.value;
		if(tree.children == null)return question;
		String exampleAnswer = example[question];
		for(int answer=0; answer<stringCount[question]; answer++){
			if(exampleAnswer.equals(strings[question][answer]))
					return classify(example, tree.children[answer]);
		}
		//answer to a question that was not seen in training phase
		return -1;
	}

	public void train(String[][] trainingData) {
		indexStrings(trainingData);
		ArrayList<String[]> examples = copy(trainingData);
		decisionTree = decisionTreeLearning(examples, new ArrayList<Integer>(), examples);
	} // train()

	/*
  *GOAL: construct a decision tree based on the input example set
  *Input: example list, questions asked already, parent example list
  *Output: Decision tree
	*BASE CASE:
	*	A.	Example subset is empty
	*	B.	Examples in subset have same class
	*	C.	No attributes/questions left
  */
	private TreeNode decisionTreeLearning(ArrayList<String[]> examples,
		ArrayList<Integer> questionsAsked, ArrayList<String[]> parentExamples){

		if(examples.isEmpty())
			return new TreeNode(null, plurality(parentExamples));
		else if(examplesHaveSameClass(examples))
			return new TreeNode(null, getClass(examples));
		else if(questionsAsked.size() >= attributes-1)
			return new TreeNode(null, plurality(examples));
		else{
			int bestQuestion = findBestQuestion(questionsAsked, examples);
			//add question to askedQuestions ArrayList
			ArrayList<Integer> questionsAskedCopy = new ArrayList<>(questionsAsked);
			questionsAskedCopy.add(bestQuestion);
			TreeNode [] children = new TreeNode[stringCount[bestQuestion]];
			for(int i = 0; i < children.length; i++){
				String splitValue = strings[bestQuestion][i];
				ArrayList<String[]> childExamples = split(examples, bestQuestion, splitValue);
				children[i] = decisionTreeLearning(childExamples, questionsAsked, examples);
			}
			return new TreeNode(children,bestQuestion);
		}

	}
	/*
  *GOAL: For a subset of examples:
	*      i) calculate information gain for each question
	*      ii) return question with the highest information gain
  *Input: questions already asked, subset of examples
  *Output: index of question with the highest info gain
  */
	private int findBestQuestion(ArrayList<Integer> questionsAsked,
															ArrayList<String[]> examples){
			double maxInfoGain = Double.MIN_VALUE;
			int maxGainIndex = 0;
			for(int i = 0; i < attributes-1; i++){
					if(questionsAsked.contains(i))continue;
					int question = i;
					//questionEntropy stores H(answer) for each answer of question i
					Double [] questionEntropy = new Double[stringCount[question]];
					//questionFrequencies stores size of each subset
					int [] questionFrequencies = new int[stringCount[question]];
					for(int j = 0; j < stringCount[question]; j++){
							String splitValue = strings[question][j];
							ArrayList<String[]> subset = split(examples, question, splitValue);
							//get class frequencies for the subset
							int [] classFrequencies = getClassFrequencies(subset);
							int total = subset.size();
							//calculate H(answer) for answer j to question i
							questionEntropy[j] = calculateH(classFrequencies, total);
							questionFrequencies[j] = total;
					}
					//calculate G(S,Q) for question i
					double totalEntropyAfter = 0.0;
					for(int q = 0; q < questionEntropy.length; q++){
							double H = questionEntropy[q];
							double weight = ((double)questionFrequencies[q]/(double)examples.size());
							totalEntropyAfter = totalEntropyAfter + ( weight * H);
					}
					int [] classFreq = getClassFrequencies(examples);
					double totalEntropyBefore = calculateH(classFreq, examples.size());
					//calculate information gain for question i
					double infoGain = totalEntropyBefore - totalEntropyAfter;
					if(infoGain > maxInfoGain){
						maxInfoGain = infoGain;
						maxGainIndex = i;
					}
			}
			return maxGainIndex;
	}
	/*
  *GOAL: From an example set, get the examples that have a
				 particular answer to a question
  *Input: examples list, Q: question index, A: answer value
  *Output: list of examples that have answer A to question Q.
  */
	private ArrayList<String[]> split(ArrayList<String[]> examples,
																		int question, String splitValue){
		ArrayList<String[]> subset = new ArrayList<String[]>();
		for(int i = 0; i < examples.size(); i++){
			String [] example = examples.get(i);
			if(example[question].equals(splitValue))
					subset.add(example);
		}
		return subset;
	}
	/*
  *GOAL: Calculate entropy for a given subset
  *Input: class frequencies of the subset, size of the subset
  *Output: entropy
  */
	private double calculateH(int [] classFrequencies, int total){
		double H = 0.0;
		for(int c = 0; c < classFrequencies.length; c++){
			double probability = (double)classFrequencies[c]/(double)total;
			H = H - xlogx(probability);
		}
		return H;
	}
	/*
  *GOAL: From a subset of examples get the frequency of each class
  *Input: list of examples
  *Output: array such that:
					i) Each index corresponds to a class
					ii) Each index stores the frequency of the indexed class
					 		in the input example subset
  */
  private int [] getClassFrequencies(ArrayList<String[]> examples){
		//stringCount[attributes-1] --> number of classes in dataset
		int [] classFrequencies = new int[stringCount[attributes-1]];
		for(int i = 0; i < examples.size(); i++){
			String [] example = examples.get(i);
			for(int j = 0; j < classFrequencies.length; j++){
				if(example[attributes-1].equals(strings[attributes-1][j]))
					//example class is j
					//increment frequency of class j
					classFrequencies[j]++;
			}
		}
		return classFrequencies;
	}
	/*
  *GOAL: Get the majority class of an example subset
  *Input: list of examples
  *Output: majority class index
  */
	private int plurality(ArrayList<String[]> examples){
		int [] classFrequencies = getClassFrequencies(examples);
		//find majority class
		int maxFreqindex = 0;
		for(int k = 1; k < classFrequencies.length; k++){
			if(classFrequencies[k] > classFrequencies[maxFreqindex])
				maxFreqindex = k;
		}
		return maxFreqindex;

	}
	/*
	*GOAL: Check whether a subset of examples have all the
				 same class.
	*Input: list of examples
	*Output: TRUE: All examples have the same class
					 FALSE: Examples do not have tha same class
	*/
	private boolean examplesHaveSameClass(ArrayList<String[]> examples){
		String [] firstExample= examples.get(0);
		String cl = firstExample[attributes-1];
		boolean sameClass = true;
		for(int i = 1; i < examples.size(); i++){
			String [] example = examples.get(i);
			if(!example[attributes-1].equals(cl)){
				sameClass = false;
				break;
			}
		}
		return sameClass;
	}
	/*
	*GOAL: Get the class of a subset with same classes
	*Input: examples list
	*Output: index of class
	*/
	private int getClass(ArrayList<String[]> examples){
		String [] example = examples.get(0);
		String cl = example[attributes-1];
		int classIndex = -1;
		for(int i = 0; i < stringCount[attributes-1]; i++){
			if(cl.equals(strings[attributes-1][i])){
				classIndex = i;
				break;
			}
		}
		return classIndex;
	}
	/*
  *GOAL: Change underlying data structure storing the dataset
				 from 2D array to dynamic ArrayList of String arrays.
  *Input: 2D dataset
  *Output: list of examples
  */
	static ArrayList<String[]> copy(String [][] data){
		ArrayList<String[]> copy = new ArrayList<String[]>();
		for(int i = 1; i < data.length; i++){
			copy.add(data[i].clone());
		}
		return copy;
	}


	/** Given a 2-dimensional array containing the training data, numbers each
	 *  unique value that each attribute has, and stores these Strings in
	 *  instance variables; for example, for attribute 2, its first value
	 *  would be stored in strings[2][0], its second value in strings[2][1],
	 *  and so on; and the number of different values in stringCount[2].
	 **/
	void indexStrings(String[][] inputData) {
		data = inputData;
		examples = data.length;
		attributes = data[0].length;
		stringCount = new int[attributes];
		strings = new String[attributes][examples];// might not need all columns
		int index = 0;
		for (int attr = 0; attr < attributes; attr++) {
			stringCount[attr] = 0;
			for (int ex = 1; ex < examples; ex++) {
				for (index = 0; index < stringCount[attr]; index++)
					if (data[ex][attr].equals(strings[attr][index]))
						break;	// we've seen this String before
				if (index == stringCount[attr])		// if new String found
					strings[attr][stringCount[attr]++] = data[ex][attr];
			} // for each example
		} // for each attribute
	} // indexStrings()

	/** For debugging: prints the list of attribute values for each attribute
	 *  and their index values.
	 **/
	void printStrings() {
		for (int attr = 0; attr < attributes; attr++)
			for (int index = 0; index < stringCount[attr]; index++)
				System.out.println(data[0][attr] + " value " + index +
									" = " + strings[attr][index]);
	} // printStrings()

	/** Reads a text file containing a fixed number of comma-separated values
	 *  on each line, and returns a two dimensional array of these values,
	 *  indexed by line number and position in line.
	 **/
	static String[][] parseCSV(String fileName)
								throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String s = br.readLine();
		int fields = 1;
		int index = 0;
		while ((index = s.indexOf(',', index) + 1) > 0)
			fields++;
		int lines = 1;
		while (br.readLine() != null)
			lines++;
		br.close();
		String[][] data = new String[lines][fields];
		Scanner sc = new Scanner(new File(fileName));
		sc.useDelimiter("[,\n]");
		for (int l = 0; l < lines; l++)
			for (int f = 0; f < fields; f++)
				if (sc.hasNext())
					data[l][f] = sc.next();
				else
					error("Scan error in " + fileName + " at " + l + ":" + f);
		sc.close();
		return data;
	} // parseCSV()

	public static void main(String[] args) throws FileNotFoundException,
												  IOException {
		if (args.length != 2)
			error("Expected 2 arguments: file names of training and test data");
		String[][] trainingData = parseCSV(args[0]);
		String[][] testData = parseCSV(args[1]);
		ID3 classifier = new ID3();
		classifier.train(trainingData);
		classifier.printTree();
		classifier.classify(testData);
	} // main()

} // class ID3
