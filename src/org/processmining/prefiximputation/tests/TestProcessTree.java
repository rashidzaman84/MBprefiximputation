package org.processmining.prefiximputation.tests;

import java.util.HashSet;

import org.processmining.processtree.Block;
import org.processmining.processtree.Edge;
import org.processmining.processtree.Event;
import org.processmining.processtree.Expression;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.Task;
import org.processmining.processtree.Variable;
import org.processmining.processtree.impl.AbstractBlock;
import org.processmining.processtree.impl.AbstractEvent;
import org.processmining.processtree.impl.AbstractOriginator.Group;
import org.processmining.processtree.impl.AbstractOriginator.Role;
import org.processmining.processtree.impl.AbstractTask;
import org.processmining.processtree.impl.ExpressionImpl;
import org.processmining.processtree.impl.ProcessTreeImpl;
import org.processmining.processtree.impl.VariableImpl;


public class TestProcessTree {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//ProcessTree tree = getTestVisTree();
		//ProcessTree tree2 = getSimpleTreeWithSkips();
		//ProcessTree tree =getSimpleTree();
		//ProcessTree tree = getExampleTree();
		
		/*System.out.println(tree);
		System.out.println("is Tree: " + tree.isTree());
		System.out.println("is DAG:  " + tree.isDag());
		System.out.println(tree.getEdges());
		System.out.println(tree.getElements());
		System.out.println(tree.getExpressions());
		System.out.println(tree.getNodes());
		System.out.println(tree.getVariables());
		System.out.println(tree.getRoot());
		System.out.println("------------------------------------------------------");
		System.out.println(tree2);
		System.out.println("is Tree: " + tree2.isTree());
		System.out.println("is DAG:  " + tree2.isDag());
		System.out.println(tree2.getEdges());
		System.out.println(tree2.getElements());
		System.out.println(tree2.getExpressions());
		System.out.println(tree2.getNodes());
		System.out.println(tree2.getVariables());
		System.out.println(tree2.getRoot());*/
		
		
		ProcessTree tree2 = RashidTree();
		System.out.println(tree2);
		//System.out.println("is Tree: " + tree2.isTree());
		//System.out.println("is DAG:  " + tree2.isDag());
		System.out.println("-----------------------------------------");
		/*for(Node n: tree2.getNodes()) {
			System.out.println("////////////////////////////////////");
			System.out.println("the node type is: "+ tree2.getType(n));
			System.out.println(n.getName() + " and is equal to Apple: " + n.getName().equals("Apple"));
			System.out.println(n.getIncomingEdges());
			System.out.println(n.getParents());
			//System.out.println(n.getProcessTree());
			System.out.println(n.isRoot());
			//System.out.println(n.getClass());
		}*/
		//////
		///////////TO FIND ND ACTIVITIES
		
		HashSet<Node> NDNodes = new HashSet<Node>();
		HashSet<Block> NDBlocks = new HashSet<Block>();
		
		for(Node n: tree2.getNodes()) {
			//System.out.println(n.getName());
			//System.out.println("Target found: " + tree2.getType(n).toString());
			if((tree2.getType(n).toString()== "Manual task" || tree2.getType(n).toString()== "Automatic task")) {
				for(Block m: n.getParents()) {
					if(tree2.getType(m).toString()=="And") {
						NDNodes.add(n);
						NDBlocks.add(m);
					}
				}
			}
		}
		System.out.println("The nodes in all ND regions are: " + NDNodes);
			
		
		//--------------------------------------------------------------------------------------
		//TO FIND CASE SPECIFIC ND ACTIVITIES
		String orpanEvent = "Banana";
		Node targetNode = null;
		HashSet<Node> caseSpecificNDNodes = new HashSet<Node>();
		HashSet<Block> relevantBlocks = new HashSet<Block>();
		for(Node n: tree2.getNodes()) {
			
			if((tree2.getType(n).toString()== "Manual task" || tree2.getType(n).toString()== "Automatic task") && n.getName().equals(orpanEvent)){
				System.out.println("The Orphan event found in the process tree: " + n);
				targetNode = n;
				//System.out.println(n.getParents());				
				break;
			}
		}
		//Boolean condition = true;
		//Boolean start=true;
		//Block target = null;
		/////////////////
		relevantBlocks.addAll(findRelevatBlocks(tree2, targetNode ,relevantBlocks));
		///////////////
		
		/*while(condition) {
			if(start) {
				for(Block b: targetNode.getParents()) {
					target=b;
					relevantBlocks.add(target);
					start=false;
			} continue;
			}
			for(Block b: target.getParents()) {
				target=b;
			}
			if(tree2.getType(target).toString()=="And") {
				relevantBlocks.add(target);
			}
			if(target.isRoot()) {
				condition=false;
			}
		}*/		
		System.out.println("Relevant Blocks are: " + relevantBlocks);
		
		for(Block b: relevantBlocks) {
			caseSpecificNDNodes.addAll(findRelevatActivities(tree2, b,caseSpecificNDNodes, orpanEvent));			
		}
		System.out.println("For Orphan event: " + orpanEvent + " ND activities are: " + caseSpecificNDNodes );
	}
	
	public static HashSet<Block> findRelevatBlocks(ProcessTree tree2, Node n, HashSet<Block> arrayList){
		
		for(Block b: n.getParents()) {
			if(b.isRoot()) {
				System.out.println("Root reached");
				continue;
			}
			if(tree2.getType(b).toString()=="And") {
				arrayList.add(b);
			}
			findRelevatBlocks(tree2, b, arrayList);
			
		}				
		return arrayList;		
	}
	
	public static HashSet<Node> findRelevatActivities(ProcessTree tree2,Block b, HashSet<Node> hashSet, String orpanEvent ){
		for(Node n: b.getChildren()) {
			if((tree2.getType(n).toString()== "Manual task" || tree2.getType(n).toString()== "Automatic task")) {
				if(n.getName()==orpanEvent) {   //also include its children if it has a sequential children
					;
				}else {
					hashSet.add(n);
				}
				
			}else {
				Block m = (Block) n;
				findRelevatActivities(tree2, m, hashSet, orpanEvent);								
			}
		}
		return hashSet;
	}
	
	
	
	
	
	
	
		/*for(Block block : NDBlocks) {
			for(Node k : block.getChildren()) {
				
				System.out.println("Block: " + block + " and child: " + k.getName());
				if(k.getName().equals(orpanEvent)) {
					caseSpecificNDNodes.addAll(block.getChildren());
				}
				
			}
		}
		System.out.println(caseSpecificNDNodes);*/
		
		
		
		
		/*for(Block m :targetNode.getParents() ) {
			//System.out.println("the no. of children are : " + m.numChildren() + " and children are: " + m.getChildren());
			if(tree2.getType(m).toString()=="And") {
				transitionsNDRegion.addAll(m.getChildren().toString());
			}
			System.out.println(m.getName());
			System.out.println(tree2.getType(m));
			System.out.println(m.getParents());
			for(Node k :m.getParents() ){
				System.out.println(k.getParents());
				for(Node l :k.getParents() ) {
					System.out.println(l.getParents());
					System.out.println(l);
					System.out.println(l.isRoot());
				}
			}
		}
		
		System.out.println(tree2.getRoot());
		*/
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
			
		/*System.out.println(tree);
		System.out.println("is Tree: " + tree.isTree());
		System.out.println("is DAG:  " + tree.isDag());
		System.out.println(tree.getEdges());
		System.out.println(tree.getElements());
		System.out.println(tree.getExpressions());
		System.out.println(tree.getNodes());
		System.out.println(tree.getVariables());
		System.out.println(tree.getRoot());
		
		for(Node n: tree.getNodes()) {
			System.out.println("For Node: " + n);
			for(Block p:n.getParents()) {
				System.out.println("For Block: " + n);
				System.out.println(p.isRoot());
				System.out.println(p.isLeaf());
			}
			System.out.println("For node: " + n + " the parent nodes are: " + n.getParents());
			System.out.println("And the incoming edges are: " + n.getIncomingEdges());
			System.out.println("And the Dependent Properties are: " + n.getDependentProperties());
			System.out.println("And the number of Parents are: " + n.numParents());
			System.out.println("And the Independent Properties are: " + n.getIndependentProperties());
			//System.out.println("And the incoming edges are: " + n.);
		}*/
		

	

	public static ProcessTree getTree() {
		//return getSimpleTreeWithSkips();
		return getTestVisTree();
	}
	
	public static ProcessTree RashidTree() {
		ProcessTree tree = new ProcessTreeImpl();
		Edge edge;
		
		//root
		Block root = new AbstractBlock.Seq("Seq");
		root.setProcessTree(tree);
		tree.addNode(root);
		tree.setRoot(root);
		
		Task apple = new AbstractTask.Automatic("Apple");
		apple.setProcessTree(tree);
		tree.addNode(apple);
		
		edge = root.addChild(apple);
		apple.addIncomingEdge(edge);
		tree.addEdge(edge);
		
		///////////////////
	
		Block and0 = new AbstractBlock.And("And0");
		and0.setProcessTree(tree);
		tree.addNode(and0);
		
		edge = root.addChild(and0);
		and0.addIncomingEdge(edge);
		tree.addEdge(edge);
		/////
		Task test1 = new AbstractTask.Manual("Test1");
		test1.setProcessTree(tree);
		tree.addNode(test1);
		
		edge = and0.addChild(test1);
		test1.addIncomingEdge(edge);
		tree.addEdge(edge);
		
		
		Task test2 = new AbstractTask.Manual("Test2");
		test2.setProcessTree(tree);
		tree.addNode(test2);
		
		edge = and0.addChild(test2);
		test2.addIncomingEdge(edge);
		tree.addEdge(edge);
		/////
		/////////////
		
		Block and = new AbstractBlock.And("And");
		and.setProcessTree(tree);
		tree.addNode(and);
		
		edge = root.addChild(and);
		and.addIncomingEdge(edge);
		tree.addEdge(edge);
		
		/*Block seqq = new AbstractBlock.Seq("Seqq");
		seqq.setProcessTree(tree);
		tree.addNode(seqq);
		
		edge = and.addChild(seqq);
		seqq.addIncomingEdge(edge);
		tree.addEdge(edge);
		
		Task dummy = new AbstractTask.Manual("Dummy");
		dummy.setProcessTree(tree);
		tree.addNode(dummy);
		
		edge = seqq.addChild(dummy);
		dummy.addIncomingEdge(edge);
		tree.addEdge(edge);*/
		
		Block and2 = new AbstractBlock.And("And2");
		and2.setProcessTree(tree);
		tree.addNode(and2);
		
		edge = and.addChild(and2);
		and2.addIncomingEdge(edge);
		tree.addEdge(edge);
		/////
		Task banana = new AbstractTask.Manual("Banana");
		banana.setProcessTree(tree);
		tree.addNode(banana);
		
		edge = and2.addChild(banana);
		banana.addIncomingEdge(edge);
		tree.addEdge(edge);
		
		
		Task chocolate = new AbstractTask.Manual("Chocolate");
		chocolate.setProcessTree(tree);
		tree.addNode(chocolate);
		
		edge = and2.addChild(chocolate);
		chocolate.addIncomingEdge(edge);
		tree.addEdge(edge);
		/////
		
		Task diaper = new AbstractTask.Manual("Diaper");
		diaper.setProcessTree(tree);
		tree.addNode(diaper);
		
		edge = and.addChild(diaper);
		diaper.addIncomingEdge(edge);
		tree.addEdge(edge);
		
		Task eat = new AbstractTask.Manual("Eat");
		eat.setProcessTree(tree);
		tree.addNode(eat);
		
		edge = root.addChild(eat);
		eat.addIncomingEdge(edge);
		tree.addEdge(edge);
		
		
		return tree;
	}

	public static ProcessTree getTestVisTree() {
		ProcessTree tree = new ProcessTreeImpl();
		Edge edge;

		// And( Xor( F+complete ,  ) , B+complete , A+complete )

		Block and = new AbstractBlock.And("And");
		and.setProcessTree(tree);
		tree.addNode(and);
		tree.setRoot(and);

		Block xor = new AbstractBlock.Xor("xor");
		xor.setProcessTree(tree);
		tree.addNode(xor);

		edge = and.addChild(xor);
		xor.addIncomingEdge(edge);
		tree.addEdge(edge);

		Task f = new AbstractTask.Manual("F");
		f.setProcessTree(tree);
		tree.addNode(f);

		edge = xor.addChild(f);
		f.addIncomingEdge(edge);
		tree.addEdge(edge);

		Task tau = new AbstractTask.Automatic("");
		tau.setProcessTree(tree);
		tree.addNode(tau);

		edge = xor.addChild(tau);
		tau.addIncomingEdge(edge);
		tree.addEdge(edge);

		Task a = new AbstractTask.Manual("B");
		a.setProcessTree(tree);
		tree.addNode(a);

		edge = and.addChild(a);
		a.addIncomingEdge(edge);
		tree.addEdge(edge);

		Task b = new AbstractTask.Manual("A");
		b.setProcessTree(tree);
		tree.addNode(b);

		edge = and.addChild(b);
		b.addIncomingEdge(edge);
		tree.addEdge(edge);

		return tree;
	}

	public static ProcessTree getSimpleTreeWithSkips() {
		ProcessTree tree = new ProcessTreeImpl();
		Edge edge;

		Block and = new AbstractBlock.And("N1");
		and.setProcessTree(tree);
		tree.addNode(and);
		tree.setRoot(and);

		for (int i = 0; i < 10; i++) {
			Task a;
			if (i % 3 == 0) {
				a = new AbstractTask.Automatic("task " + i);
			} else {
				a = new AbstractTask.Manual("task " + i);
			}
			a.setProcessTree(tree);
			tree.addNode(a);

			edge = and.addChild(a);
			a.addIncomingEdge(edge);
			tree.addEdge(edge);

		}
		return tree;

	}

	public static ProcessTree getSimpleTree() {
		ProcessTree tree = new ProcessTreeImpl();
		Edge edge;

		// xor loop
		Block loopXor = new AbstractBlock.XorLoop("N1");
		loopXor.setProcessTree(tree);
		tree.addNode(loopXor);
		tree.setRoot(loopXor);

		// Leaf A
		Task a = new AbstractTask.Manual("A");
		a.setProcessTree(tree);
		tree.addNode(a);

		edge = loopXor.addChild(a);
		a.addIncomingEdge(edge);
		tree.addEdge(edge);

		// Leaf B
		Task b = new AbstractTask.Manual("B");
		b.setProcessTree(tree);
		tree.addNode(b);

		edge = b.addParent(loopXor);
		loopXor.addOutgoingEdge(edge);
		tree.addEdge(edge);

		// Leaf A again
		edge = loopXor.addChild(a);
		a.addIncomingEdge(edge);
		tree.addEdge(edge);

		return tree;
	}

	public static ProcessTree getExampleTree() {
		ProcessTree tree = new ProcessTreeImpl();

		// Roles
		Role receptionist = new Role("receptionise");
		tree.addOriginator(receptionist);

		Role advisor = new Role("advisor");
		tree.addOriginator(advisor);

		Role clerc = new Role("clerc");
		tree.addOriginator(clerc);

		Role scientist = new Role("scientist");
		tree.addOriginator(scientist);

		// Groups
		Group frontOffice = new Group("front office");
		tree.addOriginator(frontOffice);

		Group backOffice = new Group("back office");
		tree.addOriginator(backOffice);

		Group finance = new Group("finance");
		tree.addOriginator(finance);

		Group rAndD = new Group("R&D");
		tree.addOriginator(rAndD);

		// variables
		Variable d1 = new VariableImpl("d1");
		tree.addVariable(d1);

		Variable d2 = new VariableImpl("d2");
		tree.addVariable(d2);

		Variable d3 = new VariableImpl("d3");
		tree.addVariable(d3);

		Variable d4 = new VariableImpl("d4");
		tree.addVariable(d4);

		//Expressions
		Expression d1GTd2 = new ExpressionImpl("d1 > d2", d1, d2);

		//Nodes

		//root
		Block root = new AbstractBlock.Seq("N0");
		root.getReadVariables().add(d1);
		root.getWrittenVariables().add(d4);
		root.setProcessTree(tree);
		tree.addNode(root);
		tree.setRoot(root);

		// xor loop
		Block loopXor = new AbstractBlock.XorLoop("N1");
		loopXor.setProcessTree(tree);
		loopXor.getReadVariables().add(d1);
		loopXor.getWrittenVariables().add(d1);
		loopXor.getWrittenVariables().add(d2);
		tree.addNode(loopXor);

		Edge edge = root.addChild(loopXor, d1GTd2);
		loopXor.addIncomingEdge(edge);
		tree.addEdge(edge);

		// Leaf A
		Task a = new AbstractTask.Manual("A", receptionist, frontOffice);
		a.setProcessTree(tree);
		a.getReadVariables().add(d1);
		a.getWrittenVariables().add(d1);
		a.getWrittenVariables().add(d2);
		tree.addNode(a);

		edge = loopXor.addChild(a);
		a.addIncomingEdge(edge);
		tree.addEdge(edge);

		// Leaf B
		Task b = new AbstractTask.Manual("B", receptionist, backOffice);
		b.setProcessTree(tree);
		b.getReadVariables().add(d2);
		b.getWrittenVariables().add(d2);
		tree.addNode(b);

		edge = b.addParent(loopXor);
		loopXor.addOutgoingEdge(edge);
		tree.addEdge(edge);

		// Leaf A again
		edge = loopXor.addChild(a);
		a.addIncomingEdge(edge);
		tree.addEdge(edge);

		// Deferred choice in sequence
		Block def = new AbstractBlock.Def("N2");
		def.setProcessTree(tree);
		def.getWrittenVariables().add(d3);
		def.getWrittenVariables().add(d4);
		tree.addNode(def);

		edge = root.addChild(def);
		edge.setBlockable(true);
		edge.setHideable(true);
		def.addIncomingEdge(edge);
		tree.addEdge(edge);

		// Event timeout
		Event timeout = new AbstractEvent.TimeOut("timeout", "Timeout occurred");
		timeout.setProcessTree(tree);
		timeout.getWrittenVariables().add(d3);
		timeout.getWrittenVariables().add(d4);
		tree.addNode(timeout);

		edge = def.addChild(timeout);
		timeout.addIncomingEdge(edge);
		tree.addEdge(edge);

		// Event message
		Event message = new AbstractEvent.Message("message", "Message received");
		message.setProcessTree(tree);
		message.getWrittenVariables().add(d3);
		message.getWrittenVariables().add(d4);
		tree.addNode(message);

		edge = def.addChild(message);
		message.addIncomingEdge(edge);
		tree.addEdge(edge);

		// Leaf skip
		Task skip = new AbstractTask.Automatic("skip");
		skip.setProcessTree(tree);
		//skip.getWrittenVariables().add(d3);
		//skip.getWrittenVariables().add(d4);
		tree.addNode(skip);

		edge = timeout.addChild(skip);
		skip.addIncomingEdge(edge);
		tree.addEdge(edge);

		// Leaf C
		Task c = new AbstractTask.Manual("C", advisor, finance);
		c.setProcessTree(tree);
		c.getReadVariables().add(d3);
		c.getWrittenVariables().add(d3);
		c.getWrittenVariables().add(d4);
		tree.addNode(c);

		edge = message.addChild(c);
		c.addIncomingEdge(edge);
		tree.addEdge(edge);

		// Leaf A again, but noe before the def
		edge = root.addChildAt(a, 1);
		a.addIncomingEdge(edge);
		a.addParent(root);
		tree.addEdge(edge);

		// Placeholder
		Block placeHolder = new AbstractBlock.PlaceHolder("placeholder");
		placeHolder.setProcessTree(tree);
		placeHolder.getReadVariables().add(d1);
		placeHolder.getReadVariables().add(d3);
		placeHolder.getWrittenVariables().add(d4);
		tree.addNode(placeHolder);

		edge = root.addChild(placeHolder);
		edge.setHideable(true);
		edge.setBlockable(true);
		placeHolder.addIncomingEdge(edge);
		tree.addEdge(edge);

		// First child of placeholder
		edge = placeHolder.addChild(def);
		def.addIncomingEdge(edge);
		tree.addEdge(edge);

		// Leaf D;
		Task d = new AbstractTask.Manual("D", scientist, rAndD);
		d.setProcessTree(tree);
		d.getReadVariables().add(d1);
		d.getWrittenVariables().add(d4);
		tree.addNode(d);

		edge = placeHolder.addChild(d);
		d.addIncomingEdge(edge);
		tree.addEdge(edge);

		// Leaf E;
		Task e = new AbstractTask.Manual("E", clerc, finance);
		e.setProcessTree(tree);
		e.getReadVariables().add(d3);
		e.getWrittenVariables().add(d4);
		tree.addNode(e);

		edge = placeHolder.addChild(e);
		e.addIncomingEdge(edge);
		tree.addEdge(edge);

		return tree;
	}
}