/*
	FileName: PlanarityTesting.java
	Authors:
		Nimesh Desai and Miguel Trujillo
 */

import java.io.*;
import java.util.*;
import java.lang.*;

public class PlanarityTesting {
	private TreeMap<Integer, TreeSet<Integer>> graph;
	private TreeMap<Integer, LinkedHashSet<Integer>> cycle;
	private TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> pieces;
	private int edgecount;
	private ArrayList edges = new ArrayList(0);
	private int vertexcount = 0;
	private String fileName;
	
	//Bipartite globals
	int numNodes;
	int numEdges;
	String filename;
	String vertexList;
	ArrayList<String> edgesList = new ArrayList<String>(0);
    ArrayList<String> edgesCheckList = new ArrayList<String>(0);

	public PlanarityTesting(TreeMap<Integer, TreeSet<Integer>> graph,
			TreeMap<Integer, LinkedHashSet<Integer>> cycle,
			TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> pieces,
			int edgecount, ArrayList edges, int vertexcount, String filename, String vertexList) {
		this.graph = graph;
		this.cycle = cycle;
		this.pieces = pieces;
		this.edgecount = edgecount;
		this.edges = edges;
		this.vertexcount = vertexcount;
		this.fileName = filename;
		this.vertexList = vertexList;
	}

	private enum Cnode {
		WHITE, RED, GRAY, GREEN, BLACK, NULL
	}

	// This is where the real algorithm begins
	public boolean PlanarityTest(TreeMap<Integer, TreeSet<Integer>> P,
			TreeMap<Integer, LinkedHashSet<Integer>> C,
			TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> pedazos)
			throws IOException
	{
		Vector colour = new Vector();
		Vector parent = new Vector();

		for (int j = 0; j <= P.size(); j++)
		{
			if (C.containsKey(j))
				colour.insertElementAt(Cnode.RED, j); // each will have its own
			// unique color coder.
			else
				colour.insertElementAt(Cnode.GRAY, j);
			parent.insertElementAt(-1, j);
		}

		// Step 1 find the pieces for this graph and cycle
		// System.out.print("pieces finding\n");
		// find the pieces
		TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> pieces = findPieces(
				P, C, colour, parent);
		
		
		//System.out.print("\nOriginal Graph is " + P + "\n");
		
		TreeMap<Integer, LinkedHashSet<Integer>> Cprime = new TreeMap<Integer, LinkedHashSet<Integer>>();
		// keep original graph intact too
		TreeMap<Integer, TreeSet<Integer>> Pprime = new TreeMap<Integer, TreeSet<Integer>>();

		LinkedHashSet alist = null;
		LinkedHashSet<Integer> list2 = null;
		Iterator r = null;
		int number = 0;

		//as deep a copy as it can be hoped
		for (int i = 0; i <= P.size(); i++) 
		{
			alist = C.get(i);
			if (alist != null)
			{
				list2 = new LinkedHashSet<Integer>(); 
				r = alist.iterator();
				while (r.hasNext())
				{
					number = (Integer)r.next();
					list2.add(number);
				}
				Cprime.put(i, list2);

			}
		}
		TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> pedazoes = new TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>>();
		TreeMap<Integer, TreeSet<Integer>> p = null;
		TreeSet<Integer> plist = null;
		TreeSet<Integer> pflist2 = null;
		TreeMap<Integer, TreeSet<Integer>> p2 = null;

		//copy the pieces map. Yes this method is probably  not the most efficient but it works.
		//Had a big problem with aliasing in regards to sets. This method makes sure that no aliasing occurs
		for (int i = 0; i <= P.size(); i++)
		{
			p = pieces.get(i);
			
			if (p != null)
			{
				p2 = new TreeMap<Integer, TreeSet<Integer>>();
				for (int j = 1; j <= P.size(); j++)
				{
					plist = p.get(j);
					if (plist != null)
					{
						pflist2 = new TreeSet<Integer>();
					r = plist.iterator();
					while (r.hasNext())
					{
						number = (Integer)r.next();
						pflist2.add(number);
					}
					
					p2.put(j, pflist2);
					}
				}
				pedazoes.put(i, p2);

			}
		}

		//Copy the graph
		TreeSet<Integer> palist = null;
		TreeSet<Integer> plist2 = null;
		for (int i = 0; i <= P.size(); i++)
		{
			palist = P.get(i);
			if (palist != null)
			{
				plist2 = new TreeSet<Integer>();
				r = palist.iterator();
				while (r.hasNext())
				{
					number = (Integer)r.next();
					plist2.add(number);
				}
				Pprime.put(i, plist2);

			}
		}
		
		// Step 2 the fun part
		for (int i = 0; i < pieces.size(); i++)
		{
			TreeMap<Integer, TreeSet<Integer>> piece = pieces.get(i);
			Set set = piece.keySet();
			int attachpoint = 0;
			int key = 0;
			Iterator it = set.iterator();
			while (it.hasNext())
			{
				key = (Integer)it.next();
				if (colour.get(key) == Cnode.RED)
					attachpoint++; // get the number of attach points
			}

			if (attachpoint > 2) // If  there  are only two attachment points then
			// the piece must be a path - no need to go
			// on
			{
				
				Pprime = modifyGraph(Pprime, Cprime, piece, colour, parent);
				
				Cprime = newCycle(Pprime, Cprime, pedazoes, colour, parent);
				
				if (!PlanarityTest(Pprime, Cprime, pedazoes))
					return false;
			}
		}

		// Step 3 - compute interlacement graph here
		//System.out.print("Original Cycle is " + C + "\n");
		//System.out.print("pieces are " + pieces + "\n");
		int graphsize = P.size() + 1;
		//interlacement occurs here
		InterlacementGraph interlace = new InterlacementGraph(pieces, Cprime,graphsize);
		String filename1 = interlace.findInterlacement();
		int thisEdges = interlace.getNumEdges();
		int thisNodes = interlace.getNumNodes();
		String thisVertexList = interlace.getVertexList();

		// Step 4 Test for bi-partiteness here

		//boolean is_isNot = isBipartite(fileName, edgecount, edges, vertexcount, vertexList);
		boolean is_isNot = isBipartite(filename1, thisEdges, edges, thisNodes, thisVertexList);		

		return (is_isNot);
	}

	public boolean start() throws IOException {
		// Begin the planarity test
		return PlanarityTest(graph, cycle, pieces);
	}

	private TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> findPieces(
			TreeMap<Integer, TreeSet<Integer>> P,
			TreeMap<Integer, LinkedHashSet<Integer>> C, Vector color,
			Vector parent) {
		Vector parentt = new Vector();
		for (int i = 0; i <= graph.size(); i++)
			parentt.add(-1);
		for (int i = 0; i < color.size(); i++) {
			if (color.get(i) != Cnode.RED)
				color.set(i, Cnode.GRAY);
		}
		int piececount = 0;

		int v = (Integer) cycle.firstKey(); // starting vertex
		// System.out.print("inside the findPieces the graph is " + graph +
		// "\n");

		return (DFSattach(v, P, C, color, parentt, piececount));
	}

	// private Vector parentt = new Vector();
	private TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> DFSattach(
			int v, TreeMap<Integer, TreeSet<Integer>> graph,
			TreeMap<Integer, LinkedHashSet<Integer>> cycle, Vector color,
			Vector parentt, int piececount) {
		LinkedHashSet calist = null; // get the edges of the cycle
		TreeSet vertexList = null;

		TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> pieces = new TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>>();
		int vertex = v;
		int vertex1 = 0;

		do {
			calist = cycle.get(vertex); // get a vertex
			vertexList = graph.get(vertex);
			Iterator ct = calist.iterator();
			Iterator it = vertexList.iterator();
			while (it.hasNext()) {
				vertex1 = (Integer) it.next();
				if (color.get(vertex1) != Cnode.RED
						&& color.get(vertex1) != Cnode.GREEN) {
					parentt.set(vertex1, vertex);

					TreeMap<Integer, TreeSet<Integer>> piece = new TreeMap<Integer, TreeSet<Integer>>();

					DFSpiece(vertex1, piece, graph, cycle, color, parentt);

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

		return pieces;
	}

	//DFS piece gets the pieces for the graph
	private void DFSpiece(int v, TreeMap<Integer, TreeSet<Integer>> piece,
			TreeMap<Integer, TreeSet<Integer>> graph,
			TreeMap<Integer, LinkedHashSet<Integer>> cycle, Vector color,
			Vector parentt) {
		int vertex = 0;

		// get the edges of the cycle
		TreeSet valist = null;

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
					DFSpiece(vertex, piece, graph, cycle, color, parentt);
				}
			}
			piece.put(v, piecealist);
		}
	}

	//here we modify the graph so it only contains the piece and the cycle
	private TreeMap<Integer, TreeSet<Integer>> modifyGraph(
			TreeMap<Integer, TreeSet<Integer>> graph,
			TreeMap<Integer, LinkedHashSet<Integer>> cycle,
			TreeMap<Integer, TreeSet<Integer>> piece, Vector color,
			Vector parent) {
		
		Set vertexkeys = piece.keySet();
		Set graphkeys = graph.keySet();
		Set cyclekeys = cycle.keySet();
		
		int key = 0;
		Integer gkeys[] = new Integer[graphkeys.size()];
		gkeys = (Integer[]) graphkeys.toArray(gkeys);
		for (int i = 0; i < gkeys.length; i++) {
			key = gkeys[i];

			if (!vertexkeys.contains(key) && !cyclekeys.contains(key)) {
				
				graph.remove(key); // remove any vertices in the graph that does
									// not appear in the piece or cycle
				color.set(key, Cnode.NULL);
				parent.set(key, -1);
			} else {
				TreeSet<Integer> neualist = new TreeSet<Integer>();
				TreeSet<Integer> plist = piece.get(key);
				LinkedHashSet<Integer> clist = cycle.get(key);
				if (plist != null)
					neualist.addAll(plist);
				if (clist != null)
					neualist.addAll(clist);
				
				graph.put(key, neualist);
			}
		}
		return graph;
	}
	
	private boolean isBipartite(String filename, int numEdges, ArrayList originalEdges, int numNodes, String vertexList) throws IOException
	{		
		//Bipartite private variables
	    String nodeSeq = new String();
	    boolean [] isBipartite = {true, true};
	    int _NOT = 0;
	    int _VERTICES1 = 1;
	    int _VERTICES2 = 2;
	    
		if( numNodes == 0 )
			return false;
		else
		{			
			String currentEdge = new String();			
			int vertex1 = 0;
		    int vertex2 = 0;		
	
	        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(filename)));	
            String tempStr = new String();	            
			StringTokenizer tokenizer;
			tokenizer = new StringTokenizer(vertexList, " ");
			
			int[] nodes = new int[numNodes];
			int[] edges = new int[2];
			
	 		for( int i = 0; i < numNodes; ++i )
	 		{
				 if( tokenizer.hasMoreTokens() )
		 			nodes[i] = Integer.parseInt(tokenizer.nextToken());
			}
				
            while ( !( tempStr.equals( " " ) ) )
            {
            	int[] pos = new int[numNodes];		
            	sortOutEdges(bufferedReader, numEdges);		              
            	tempStr = bufferedReader.readLine();		
            	for( int pass = 0; pass < 2; ++pass )
            	{
            		int length;
            		for( int i = 0; i < numNodes; ++i )
						pos[i] = _NOT;

            		for( int j = 0; j < numEdges; ++j )
            		{
            			if(pass == 0)
                    	 		currentEdge = edgesList.get(j).toString();
            			if(pass == 1)
                    	 		currentEdge = edgesCheckList.get(j).toString();
            			
                		StringTokenizer tk = new StringTokenizer(currentEdge, " ");                		
                		
						for( int i = 0; tk.hasMoreTokens(); ++i )
						{
							edges[i] = Integer.parseInt( tk.nextToken() );
						}
			 	 		vertex1 = getPosition(edges[0], nodes);					 	 		
			 	 		vertex2 = getPosition(edges[1], nodes);
	                    if(pos[vertex1] == _NOT && pos[vertex2] == _NOT)
	                    {
	                    	pos[vertex1] = _VERTICES1;
	                        pos[vertex2] = _VERTICES2;
	                    }		                      
		                  else
	                    	  if(pos[vertex1] == _NOT && pos[vertex2] == _VERTICES1)
	                              pos[vertex1] = _VERTICES2;
	                    	  else
	                    		  if(pos[vertex1] == _NOT && pos[vertex2] == _VERTICES2)
	                    			  pos[vertex1] = _VERTICES1;
	                    		  else
	                    			  if(pos[vertex1] == _VERTICES1 && pos[vertex2] == _NOT)
	                    				  pos[vertex2] = _VERTICES2;
	                    			  else
	    		                    	  if( pos[vertex1] == _VERTICES2 && pos[vertex2] == _NOT )
	    		                              pos[vertex2] = _VERTICES1;
	    		                    	  else
	    			                    	  if(pos[vertex1] == pos[vertex2])
	    			                    	  {
	    			                              isBipartite[pass] = false;
	    			                              //System.out.print("\nIs the grapth BiPartite: " + isBipartite[pass] + "\n");
	    			                              break;
	    			                    	  }	               
	    			                    	  else
	    			                    	  {
	    			                    		  //else nothing
	    			                    	  }
            		}//end of second for                     
	                 if(isBipartite[pass])
	                	 break;

	             }//end of first for
	             
	             if( !( isBipartite[0] ) || !( isBipartite[1] ) )
	             {
					 	//System.out.print( "\nNot Bipartite" );
					 	return false;
	             }
				 else
				 {
					//System.out.print( "\nBipartite " );					
					return true;
				 }	             
           }//end of while
            
        // this will be never reached, but added to complete the method
        return false;
	}
	}

	private boolean isSeparating
	(
			TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> pieces)
		{
			return (pieces.size() > 1);
		}

	private TreeMap<Integer, LinkedHashSet<Integer>> newCycle(
			TreeMap<Integer, TreeSet<Integer>> graph,
			TreeMap<Integer, LinkedHashSet<Integer>> cycle,
			TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> pieces,
			Vector color, Vector parent) {
		
		Set cyclekeys = cycle.keySet();
		
		Iterator ft = cyclekeys.iterator();
		TreeSet clist = null;
		LinkedHashSet attachpoints = new LinkedHashSet();
		while (ft.hasNext()) {
			int ckey = (Integer) ft.next();
			clist = graph.get(ckey);
			
			if (clist.size() > 2)
				attachpoints.add(ckey); // to get list of attachpoints to be
										// used

		}

		int attachnum = 0;
		Iterator at = attachpoints.iterator(); // get the attachpoints
		attachnum = (Integer) at.next();
		TreeSet usedattachp = new TreeSet();
		usedattachp.add(attachnum); // add attachnum to usedattachp set

		int downcount = -1;
		pieces.clear();
		cycle = modifyCycle(attachnum, graph, cycle, pieces, color, parent);
		
		pieces = findPieces(graph, cycle, color, parent); // find the pieces
		
		//this downcount stuff probably not needed but just in case it fails to find a cycle from one attachment point...
		while (!isSeparating(pieces) && downcount != 0) {
			

			if (downcount != -1)
				downcount--;

			pieces.clear();
			cycle = modifyCycle(attachnum, graph, cycle, pieces, color, parent);
			pieces = findPieces(graph, cycle, color, parent); // find the pieces

			
			cyclekeys = cycle.keySet();
			ft = cyclekeys.iterator();
			clist = null;
			attachpoints.clear(); // clear set of attachment points for new
									// cycle

			while (ft.hasNext()) {
				int ckey = (Integer) ft.next();
				clist = graph.get(ckey);
				if (clist.size() > 2)
					attachpoints.add(ckey); // to get list of attachpoints to be
											// used
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
		return cycle;
	}
	
	int getPosition(int node, int[] nodes)
    {
		int position = 0;
	    String temp;
	    for( int i = 0; i < nodes.length; i++ )
	    {
			if( node == nodes[i] )
			{
				position = i;
			}				
	   }
	   return position;
  	 }

	private TreeMap<Integer, LinkedHashSet<Integer>> modifyCycle(int attachnum,
			TreeMap<Integer, TreeSet<Integer>> graph,
			TreeMap<Integer, LinkedHashSet<Integer>> cycle,
			TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> pieces,
			Vector color, Vector parent) {
		for (int i = 0; i < color.size(); i++) {
			if (color.get(i) != Cnode.RED)
				color.set(i, Cnode.GRAY);
		}

		LinkedHashSet calist = null; // get the edges of the cycle
		TreeSet avlist = null;
		
		avlist = graph.get(attachnum); // get graph adjacency list
		int vertex = attachnum;
		
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
				DFSedge(vertex1, vertex, graph, cycle, pieces, color, parent,
						lastpoint);
			}
		}
		return cycle;
	}
	
	 void sortOutEdges(BufferedReader bufferedReader, int numNodes) throws IOException
     {
        String tempStr;
      	String pair;	      
     	edgesList.ensureCapacity(2 * numNodes);	     
        for( int i = 0; i < numNodes; ++i )
           {
               tempStr = bufferedReader.readLine();
               StringTokenizer tokenizer = new StringTokenizer(tempStr);	              
               while(tokenizer.hasMoreTokens())
               {
                   pair = tokenizer.nextToken();
                   pair = pair + " " + tokenizer.nextToken();
                   edgesList.add(i, pair);
               }
           }
			  
           for (int j = 0; j < numNodes; ++j )
           {
               String temp = (String) edgesList.get(j);
               //System.out.print("\nThe temp value for the edgesCheckList is: " + temp + "\n");
               edgesCheckList.ensureCapacity(j + 1);
               edgesCheckList.add(temp);
           }
           //System.out.print("\nThe edgesCheckList ArrayList is: " + edgesCheckList + "\n");	      
     } // end sortOutEdges

	private boolean aFounder = false;

	private void DFSedge(int v, int sattachv,
			TreeMap<Integer, TreeSet<Integer>> graph,
			TreeMap<Integer, LinkedHashSet<Integer>> cycle,
			TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> pieces,
			Vector color, Vector parent, int endpoint) {
		int vertex = 0;

		TreeSet valist = null; // get the edges of the cycle

		valist = graph.get(v);
		Iterator it = valist.iterator();
		if (v == endpoint) {
			// edge removed
			removeEdge(v, sattachv, graph, cycle, color, parent);

			// new cycle created
			createCycle(v, sattachv, graph, cycle, color, parent);

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

			pieces = findPieces(graph, cycle, color, parent); // find the pieces
			// System.out.print("The pieces are ~# " + pieces + " \n");

			if (isSeparating(pieces))
				aFounder = true; // separating cycle found, no need to go on
			else
				pieces.clear();
		} else {
			while (it.hasNext() && aFounder == false) {
				vertex = (Integer) it.next();

				if (vertex != sattachv && (Integer) vertex != parent.get(v)) // edge
																				// isn't
																				// the
																				// starting
																				// edge
				{
					if (color.get(vertex) != Cnode.RED) //no, we don't want to include a current attachment point in the new cycle edge
					{
						
						parent.set(vertex, v);
						DFSedge(vertex, sattachv, graph, cycle, pieces, color,
								parent, endpoint);
					}
					else if (vertex == endpoint) //...unless it is the endpoint we're trying to get to
					{
						
						parent.set(vertex, v);
						DFSedge(vertex, sattachv, graph, cycle, pieces, color,
								parent, endpoint);
					}
				}
			}
		}
		// return cycle;
	}

	private void removeEdge(int v1, int v2,
			TreeMap<Integer, TreeSet<Integer>> graph,
			TreeMap<Integer, LinkedHashSet<Integer>> cycle, Vector color,
			Vector parent) {
		
		LinkedHashSet calist = cycle.get(v1); // get the cycle adjacency list of
												// this point
		int count1 = 0;
		int count2 = 0;
		Iterator it = calist.iterator();
		int vertex = 0;
		int end = 0;
		int start = 0;

		calist = cycle.get(v2);
		end = v1;
		start = v2;

		
		Iterator ft = calist.iterator();
		vertex = (Integer) ft.next();
		if (vertex != end) {
			
			calist.remove(vertex);// break the connection
			int first = vertex;
			while (vertex != end) {
				color.set(vertex, Cnode.BLACK); // any intermediate vertices
												// removed
				calist = cycle.get(vertex);
				
				cycle.remove(vertex);
				ft = calist.iterator();
				first = vertex;
				vertex = (Integer) ft.next();
			}
			calist = cycle.get(vertex);
			calist.remove(first); // break the connection
		} else {
			calist.remove(end);
			calist = cycle.get(end);
			calist.remove(start);
		}
	}

	private void createCycle(int vertex, int v,
			TreeMap<Integer, TreeSet<Integer>> graph,
			TreeMap<Integer, LinkedHashSet<Integer>> cycle, Vector color,
			Vector parent) {
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
			createCycle(pnode, v, graph, cycle, color, parent);
		}
	}
}