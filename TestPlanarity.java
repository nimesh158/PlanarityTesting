/*
	FileName: TestPlanarity.java
	Authors:
		Nimesh Desai and Miguel Trujillo
 */

import java.io.*;
import java.util.*;
import java.lang.Integer;

public class TestPlanarity {
	static TreeMap<Integer, TreeSet<Integer>> graph = new TreeMap<Integer, TreeSet<Integer>>();
	static TreeMap<Integer, LinkedHashSet<Integer>> cycle = new TreeMap<Integer, LinkedHashSet<Integer>>();
	static TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> pieces = new TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>>();
	static ArrayList edgesList = new ArrayList(0);
	static int vertexcount = 0;
	// static TreeMap<Integer, TreeSet<Integer>> piece = new TreeMap<Integer,
	// TreeSet<Integer>>();
	// static LinkedHashSet cycle = new LinkedHashSet();;
	static int edgecount = 0;
	static int cycleedgecount = 0;
	static int pieceedgecount = 0;
	static int piececount = 0;
	static String vertexList = new String();
	static BufferedReader stdin = new BufferedReader(new InputStreamReader(
			System.in));

	public static void main(String[] args) throws Exception {
		Vector input = new Vector();
		String edge = new String();
		//vertexList = new String();

		// ask the user to input the graph filename
		System.out.print("Enter The Filename To Read: ");
		String filename = stdin.readLine();

		BufferedReader reader = new BufferedReader(new FileReader(filename));
		int edgecount = 0;

		while (reader.ready()) {
			edge = reader.readLine();
			input.addElement(edge);
			edgesList.add(edge);
			edgecount++;
		}
		//System.out.print("\nThe total number of edges is: " + edgecount + "\n");
		//System.out.print("\nThe edges are: " + edgesList + "\n");
		reader.close();

		int i = 0;

		String[] numbers = new String[2];
		String edges = "";

		TreeSet<Integer> alist1 = null;
		TreeSet<Integer> alist2 = null;

		while (i < input.size()) {
			edges = (String) input.elementAt(i);
			numbers = edges.split(" ");

			Integer num1 = new Integer(numbers[0]);
			Integer num2 = new Integer(numbers[1]);

			alist1 = graph.get(num1);
			if (alist1 == null)
				alist1 = new TreeSet<Integer>();

			alist1.add(num2);
			graph.put(num1, alist1);

			alist2 = graph.get(num2);
			if (alist2 == null)
				alist2 = new TreeSet<Integer>();

			alist2.add(num1);
			graph.put(num2, alist2);

			i++;
		}

		if (!vertexEdgeCheck()) {
			System.out.print("Non-Planar");
			System.exit(0);
		}

		findCycle();

		PlanarityTesting test = new PlanarityTesting(graph, cycle, pieces,
				edgecount, edgesList, vertexcount, filename, vertexList);
		if (test.start())
			System.out.print("Planar");
		else
			System.out.print("Non-Planar");
	}

	// check to see if number of edges is at most 3n-6
	static boolean vertexEdgeCheck()
	{
		vertexcount = 1;
		TreeSet<Integer> vertex = null;
		
		for (int i = 0; i < graph.size(); i++)
		{
			vertex = graph.get(i);
			if (vertex != null)
			{
				//System.out.print("\n" + vertex + "\n");
				vertexcount++;
			}
		}
		//System.out.print("The total number of vertices are: " + vertexcount	+ "\n");
		
		Set<Integer> edgeslist = graph.keySet();
		Object[] a = new Integer[vertexcount];
		a = edgeslist.toArray();
		vertexList = " ";
		for (int i = 0; i < a.length; ++i)
			vertexList = vertexList + a[i] + " ";
		//System.out.print("\nVertex List is: " + vertexList + "\n");
		
		return (edgecount < (3 * vertexcount - 6));
	}

	static enum Cnode {
		WHITE, RED, GRAY, GREEN, BLACK, NULL
	}

	static Vector color = new Vector();
	static Vector parent = new Vector();

	static void findCycle() {
		TreeSet<Integer> vertex;
		int i = 0;
		for (i = 0; i <= graph.size(); i++) {
			vertex = graph.get(i);
			if (vertex != null) {
				// first we color the graphs
				color.insertElementAt(Cnode.WHITE, i);
				parent.insertElementAt(-1, i);
			} else {
				color.insertElementAt(Cnode.NULL, i);
				parent.insertElementAt(-2, i);
			}
		}

		i = 0;
		int v = 0;
		TreeSet vertexlist = graph.get(i);

		while (vertexlist == null) {
			i++;
			vertexlist = graph.get(i);
		}
		v = i;

		DFS(v);
		findPieces();
		//System.out.print("cy " + cycle + "\n");
		Set cyclekeys = cycle.keySet();
		Iterator ft = cyclekeys.iterator();
		TreeSet clist = null;
		LinkedHashSet attachpoints = new LinkedHashSet();
		while (ft.hasNext()) {
			int ckey = (Integer) ft.next();
			clist = graph.get(ckey);
			if (clist.size() > 2)
				// to get list of attach points to be used
				attachpoints.add(ckey);
		}

		int attachnum = 0;
		// get the attach points
		Iterator at = attachpoints.iterator();
		attachnum = (Integer) at.next();
		TreeSet usedattachp = new TreeSet();
		// add attachnum to usedattachp set
		usedattachp.add(attachnum);

		int downcount = -1;

		while (!isSeparating() && downcount != 0) {
			if (downcount != -1)
				downcount--;

			pieceedgecount = 0;
			cycleedgecount = 0;
			piececount = 0;

			modifyCycle(attachnum);

			// findPieces();
			cyclekeys = cycle.keySet();
			ft = cyclekeys.iterator();
			clist = null;
			// clear set of attachment points for new cycle
			attachpoints.clear();

			while (ft.hasNext()) {
				int ckey = (Integer) ft.next();
				clist = graph.get(ckey);
				if (clist.size() > 2)
					// to get list of attach points to be used
					attachpoints.add(ckey);
			}

			at = attachpoints.iterator();
			attachnum = (Integer) at.next();
			while (usedattachp.contains(attachnum) && at.hasNext()) {
				attachnum = (Integer) at.next(); // do this to ensure we don't
													// start search with an old
													// attachment point
			}

			if (!usedattachp.containsAll(attachpoints)) {
				downcount = 2;
			}
			usedattachp.add(attachnum); // now add attachment point to table

			aFounder = false;
		}

		//System.out.print("The cycle is " + cycle + "\n");
		//System.out.print("The pieces are " + pieces + "\n");
	}

	// here we search for a cycle
	static void DFS(int vertex) {
		// int u = 0;
		int v = -1;
		color.set(vertex, Cnode.GRAY);
		LinkedHashSet cyclealist = null;
		TreeSet vertexlist = graph.get(vertex); // get adjacency list for vertex

		Iterator i1 = vertexlist.iterator();
		v = (Integer) i1.next();

		while (v == (Integer) parent.get(vertex) && i1.hasNext()) {
			v = (Integer) i1.next();
		}

		if (color.get(v) == Cnode.GRAY) // detected a node that has been
										// traversed - cycle
		{
			cyclealist = cycle.get(vertex);
			if (cyclealist == null)
				cyclealist = new LinkedHashSet();

			cyclealist.add(v);
			cycle.put(vertex, cyclealist);
			color.set(vertex, Cnode.RED);

			createCycle(vertex, v);

			cyclealist = cycle.get(v);
			if (cyclealist == null)
				cyclealist = new LinkedHashSet();

			cyclealist.add(vertex);
			cycle.put(v, cyclealist);
			color.set(v, Cnode.RED);
			return;
		} else {
			parent.set(v, vertex);
			DFS(v);
		}
	}

	// create a cycle
	static void createCycle(int vertex, int v) {
		int pnode;
		LinkedHashSet cyclealist = null;

		pnode = (Integer) parent.get(vertex);
		if (pnode == v) {
			cyclealist = cycle.get(pnode);
			if (cyclealist == null)
				cyclealist = new LinkedHashSet();

			cyclealist.add(vertex);
			cycle.put(pnode, cyclealist);
			color.set(v, Cnode.RED);

			cyclealist = cycle.get(vertex);
			if (cyclealist == null)
				cyclealist = new LinkedHashSet();

			cyclealist.add(pnode);
			cycle.put(vertex, cyclealist);
			color.set(pnode, Cnode.RED);

		} else {
			cyclealist = cycle.get(pnode);
			if (cyclealist == null)
				cyclealist = new LinkedHashSet();

			cyclealist.add(vertex);
			cycle.put(pnode, cyclealist);
			color.set(v, Cnode.RED);

			cyclealist = cycle.get(vertex);
			if (cyclealist == null)
				cyclealist = new LinkedHashSet();

			cyclealist.add(pnode);
			cycle.put(vertex, cyclealist);
			color.set(pnode, Cnode.RED);
			createCycle(pnode, v);
		}
	}

	// find the pieces to the graph
	static Vector parentt = new Vector();

	static void findPieces() {
		LinkedHashSet calist = null; // get the edges of the cycle
		int i = 0;
		calist = cycle.get(i);
		int v = 0; // starting vertex
		int vertex;

		for (i = 0; i <= graph.size(); i++)
			parentt.add(-1);

		v = cycle.firstKey();

		pieceedgecount = 0;

		DFSattach(v);
	}

	static Vector attachv = new Vector();
	static int anum = 0;

	static void DFSattach(int v) {
		// get the edges of the cycle
		LinkedHashSet calist = null;
		TreeSet vertexList = null;

		int vertex = v;
		int vertex1 = 0;

		do {
			calist = cycle.get(vertex);
			vertexList = graph.get(vertex);
			Iterator ct = calist.iterator();
			Iterator it = vertexList.iterator();
			while (it.hasNext()) {
				vertex1 = (Integer) it.next();
				if (color.get(vertex1) != Cnode.RED
						&& color.get(vertex1) != Cnode.GREEN) {
					parentt.set(vertex1, vertex);

					TreeMap<Integer, TreeSet<Integer>> piece = new TreeMap<Integer, TreeSet<Integer>>();

					DFSpiece(vertex1, piece);

					pieces.put(piececount, piece); // add a piece to the piece
													// DS
					piececount++;

				}
				// check for chords
				else if (color.get(vertex1) == Cnode.RED
						&& !calist.contains(vertex1)) {
					TreeMap<Integer, TreeSet<Integer>> piece = new TreeMap<Integer, TreeSet<Integer>>();
					TreeSet piecelist = new TreeSet();
					TreeSet piecelist2 = new TreeSet();
					piecelist.add(vertex1);
					piece.put(vertex, piecelist);
					piecelist2.add(vertex);
					piece.put(vertex1, piecelist2);
					if (!pieces.containsValue(piece)) // chances are we'll run
														// across the chord
														// twice
					{
						pieces.put(piececount, piece);
						piececount++;
					}
				}
			}

			vertex = (Integer) ct.next();
		} while (vertex != v);
	}

	// add pieces to piece map
	static void DFSpiece(int v, TreeMap<Integer, TreeSet<Integer>> piece) {
		int vertex = 0;
		TreeSet valist = null; // get the edges of the cycle

		pieceedgecount++;

		TreeSet piecealist = null;
		piecealist = piece.get(v);
		if (piecealist == null)
			piecealist = new TreeSet();

		valist = graph.get(v);
		Iterator it = valist.iterator();
		if (color.get(v) != Cnode.RED) {
			color.set(v, Cnode.GREEN); // add vertex to the piece
			while (it.hasNext()) {
				vertex = (Integer) it.next();
				piecealist.add(vertex);
				TreeSet piecealist2 = piece.get(vertex);
				if (piecealist2 == null)
					piecealist2 = new TreeSet();

				piecealist2.add(v);
				piece.put(vertex, piecealist2);

				if ((color.get(vertex) != Cnode.GREEN)
						&& (Integer) vertex != parentt.get(v)) {
					parentt.set(vertex, v);
					DFSpiece(vertex, piece);
				}
			}
			piece.put(v, piecealist);
		}
	}

	// check to see if the cycle is indeed a separating cycle
	static boolean isSeparating() {
		return (pieces.size() > 1);
	}

	static int attachnum = 0;

	// Modify the cycle
	static void modifyCycle(int attachnum) {
		for (int i = 0; i < color.size(); i++) {
			if (color.get(i) != Cnode.RED)
				color.set(i, Cnode.GRAY);
		}

		LinkedHashSet calist = null; // get the edges of the cycle
		TreeSet avlist = null;
		int i = 0;

		// calist = cycle.get(attachnum); //get cycle adjacency list of
		// attachment vertex
		avlist = graph.get(attachnum); // get graph adjacency list
		int vertex = attachnum;
		// int vertexavlist = graph.get(attachnum); //get graph adjacency list
		int vertex2 = 0;
		Iterator ct = null;
		TreeSet vlist = null;

		do {
			calist = cycle.get(vertex); // get cycle adjacency list of
										// attachment vertex

			ct = calist.iterator();
			vertex2 = (Integer) ct.next(); // get next vertex in the cycle
			vlist = graph.get(vertex2); // get this vertex's graph adjacency
										// list
			vertex = vertex2;
		} while (vlist.size() < 3);

		int lastpoint = vertex;

		vertex = attachnum;

		Iterator it = avlist.iterator();
		int vertex1 = 0;
		while (it.hasNext()) {
			vertex1 = (Integer) it.next();
			if (color.get(vertex1) != Cnode.RED
					&& color.get(vertex1) != Cnode.BLACK) {
				parent.set(vertex1, vertex);
				DFSedge(vertex1, vertex, lastpoint);
			}
		}
	}

	static boolean aFounder = false;

	// DFS edge finds a new edge for the new cycle
	static void DFSedge(int v, int sattachv, int endpoint) {
		int vertex = 0;

		TreeSet valist = null; // get the edges of the cycle

		valist = graph.get(v);
		Iterator it = valist.iterator();
		if (v == endpoint) {
			// edge removed
			removeEdge(v, sattachv);
			// new cycle created

			createCycle(v, sattachv);
			// The following code makes travesing the cycle consistent

			Vector colorcycle = new Vector();
			for (int i = 0; i <= graph.size(); i++) {
				colorcycle.insertElementAt(Cnode.WHITE, i);
			}
			colorcycle.set(sattachv, Cnode.RED);
			LinkedHashSet alist = cycle.get(sattachv);
			Iterator ft = alist.iterator();
			int last = (Integer) ft.next();
			alist.remove(last); // this is for consistency in reading the cycle
			alist.add(last);
			ft = alist.iterator();
			vertex = (Integer) ft.next();
			parent.set(vertex, sattachv);

			while (vertex != sattachv) {
				colorcycle.set(vertex, Cnode.RED);
				alist = cycle.get(vertex);
				ft = alist.iterator();
				last = (Integer) ft.next();
				v = last;

				if (colorcycle.get(v) == Cnode.RED) {
					alist.remove(v); // this is for consistency in reading the
										// cycle
					alist.add(v);
					Iterator fft = alist.iterator();
					v = (Integer) fft.next();
				}
				vertex = v;
			}

			alist = cycle.get(sattachv);
			ft = alist.iterator();
			last = (Integer) ft.next();
			LinkedHashSet blist = cycle.get(last);
			Iterator gt = blist.iterator();
			int first = (Integer) gt.next();
			if (first == sattachv) {
				alist.remove(last); // this is for consistency in reading the
									// cycle
				alist.add(last);
			}
			piececount = 0;
			findPieces(); // find the pieces

			if (isSeparating())
				aFounder = true; // separating cycle found, no need to go on
			else
				pieces.clear();
		} else {
			while (it.hasNext() && aFounder == false) {
				vertex = (Integer) it.next();

				// edge isn't the starting edge
				if (vertex != sattachv && (Integer) vertex != parent.get(v)) {
					
					if (color.get(vertex) != Cnode.RED) //no, we don't want to include a current attachment point in the new cycle edge
					{

						parent.set(vertex, v);
						DFSedge(vertex, sattachv, endpoint);
					}
					else if (vertex == endpoint) //...unless it is the endpoint we're trying to get to
					{

						parent.set(vertex, v);
						DFSedge(vertex, sattachv, endpoint);
					}
				}
			}
		}
	}

	static void removeEdge(int v1, int v2) {
		LinkedHashSet calist = cycle.get(v1); // get the cycle adjacency list of
												// this point

		Iterator it = calist.iterator();
		int vertex = 0;
		int end = 0;
		int start = 0;

		calist = cycle.get(v2);
		end = v1;
		start = v2; // start the process

		it = calist.iterator();
		vertex = (Integer) it.next();
		if (vertex != end) {
			calist.remove(vertex);// break the connection
			int first = vertex;
			while (vertex != end) {
				color.set(vertex, Cnode.BLACK); // any intermediate vertices
												// removed
				calist = cycle.get(vertex);
				cycle.remove(vertex);
				it = calist.iterator();
				first = vertex;
				vertex = (Integer) it.next();
				// break the connection
				// remove vertex from the cycle
			}
			calist = cycle.get(vertex);
			calist.remove(first); // break the connection
		} else {
			calist.remove(end);
			calist = cycle.get(end);
			calist.remove(start);
		}

	}
}