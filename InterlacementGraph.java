/*
	FileName: InterlacementGraph.java
	Authors:
		Nimesh Desai and Miguel Trujillo
 */

import java.io.*;
import java.util.*;
import java.lang.Integer;

public class InterlacementGraph
{
	static TreeMap<Integer, TreeSet<Integer>> interlacedGraph = new TreeMap<Integer, TreeSet<Integer>>();
	static TreeMap<Integer, LinkedHashSet<Integer>> cycle = new TreeMap<Integer, LinkedHashSet<Integer>>();
	static TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> pieces = new TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>>();
	String filename;
	int numNodes = 0;
	int numEdges = 0;
	String vertexList;
	int graphsize;
	int counterForInterlacement = 0;
	
	//constructor
	public InterlacementGraph(TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> pieces, TreeMap<Integer, LinkedHashSet<Integer>> cycle, int graphsize )
	{
		this.cycle = cycle;
		this.pieces = pieces;
		this.filename = "interlace" + ".txt";
		this.graphsize = graphsize;
	}

	private enum Cnode
	{
		WHITE, RED, GRAY, GREEN, BLACK, NULL
	}
	
	
	public String findInterlacement() throws IOException
	{
		//create the output file
		BufferedWriter output = new BufferedWriter( new FileWriter( new File( filename ) ) );
		
		
		LinkedHashSet vertex = new LinkedHashSet();
		Iterator itVertex = vertex.iterator();
		
		String cycleVertices = cycle.keySet().toString();		

		int[] nodes = new int[pieces.size()];
		HashSet<Integer> attachp1 = null;
		HashSet<Integer> attachp2 = null;
		int setvertex = 0;
		int v1 = 0;
		boolean shared = false;
		Vector color = new Vector();
		LinkedHashSet clist = null;
		int end = 0;
		Iterator it = null;
		Iterator at = null;
		int count = 0; //count of attachment vertices.
		boolean interlacementfound = false;
		int start = 0;
		Set<Integer> cycleKeys = cycle.keySet();
		for(int x = 0; x < graphsize; x++) //get colors for vertices
		{
			if (cycleKeys.contains(x))
				color.insertElementAt(Cnode.BLACK, x);
			else
				color.insertElementAt(Cnode.NULL, x);

		}
		
		for(int i = 0; i < pieces.size()-1; i++)
		{
			 attachp1 = findAttach(pieces.get(i));
			
			for (int x = 0; x < color.size(); x++)
			{
				if (attachp1.contains(x))
					color.set(x, Cnode.GREEN); //set all attachment points to GREEN for piece 1
			}
				at = attachp1.iterator();
				v1 = (Integer)at.next();
				
				setvertex = v1;
				start = setvertex;
				
				while (at.hasNext())
				{
					v1 = (Integer)at.next();
				}
				end = v1; //and last attachment point
				

			for (int j = i + 1; j < pieces.size(); j++)
			{
				attachp2 = findAttach(pieces.get(j));
				for (int x = 0; x < color.size(); x++)
				{
					if (attachp2.contains(x) && attachp1.contains(x)) 
					color.set(x, Cnode.WHITE); //set all attachment points to RED for piece 2 shared attachment point
				 else if (attachp2.contains(x))
					color.set(x, Cnode.RED); //set all attachment points to RED for piece 2
				}
				if (color.get(v1) == Cnode.WHITE) //found an attachment point at start of scan
					shared = true;

				
				while (setvertex != end && interlacementfound == false) //scan until we reach the last vertex
				{
					
					clist = cycle.get(setvertex); //get the starting point in cycle
					it = clist.iterator();
					setvertex = (Integer)it.next();
					if (color.get(setvertex) == Cnode.RED)
					{
						count++;
						
					}
					if (color.get(setvertex) == Cnode.WHITE)
					{
						
						if (count != attachp2.size() - 1)
							interlacementfound = true;
						count++; //??
					}
					if (color.get(setvertex) == Cnode.GREEN) //come across an attachmentpoint in first piece
					{
						if(shared != true && count != attachp2.size() && count != 0)
							interlacementfound = true;
						if (shared == true && count != attachp2.size()-1 && count != 0)
							interlacementfound = true;
						
					}
					

				}
				int l = i + 1;
				int m = j + 1;
				if ( interlacementfound )
				{
					if( counterForInterlacement == 0 )
					{
						counterForInterlacement++;
						//System.out.print("Interlacement between Piece " + l + " and: Piece " + m+ "\n");
						output.write(Integer.toString(l));
						output.write(" ");
						output.write(Integer.toString(m));					
						nodes[i]++;
						nodes[j]++;
						numEdges = numEdges + 1;
						//System.out.print("\nThe total number of edges are: " + numEdges + "\n");
					}
					else
					{
						//System.out.print("Interlacement between Piece " + l + " and: Piece " + m+ "\n");
						output.newLine();
						output.write(Integer.toString(l));
						output.write(" ");
						output.write(Integer.toString(m));					
						nodes[i]++;
						nodes[j]++;
						numEdges = numEdges + 1;
						//System.out.print("\nThe total number of edges are: " + numEdges + "\n");
					}					
				}
				else
				{
					//System.out.print("No Interlacement between: " + l + " and: " + m + "\n");
				}

				for(int x = 0; x < color.size(); x++) //recolor 
				{
					if(attachp2.contains(x) && attachp1.contains(x))
						color.set(x, Cnode.GREEN);
					else if(attachp2.contains(x))
						color.set(x, Cnode.BLACK);
				}

				setvertex = start;
				interlacementfound = false;
				count = 0;
				shared = false;
				}

				
			for(int x = 0; x < color.size(); x++)
				{
					if(attachp1.contains(x))  //recolor
						color.set(x, Cnode.BLACK);
				}


				
			}

			

		
		
		
			vertexList = " ";
	for( int i = 0; i < pieces.size() ; ++i )
		{
			int j = i+1;
			if( nodes[i] > 0 )
			{
				numNodes++;
				if( vertexList.equals(" "))
					vertexList = vertexList + j;
				else
					vertexList = vertexList + " " + j;
			}
		}		
		output.close();
		//return the inter-lacement graph
		
		return filename;
	}

private HashSet<Integer> findAttach(TreeMap<Integer, TreeSet<Integer>> piece)
{
	HashSet<Integer> attachpoints = new HashSet<Integer>();
	TreeSet<Integer> vlist = null;
	int vertex = 0;
	Set<Integer> vertexList = piece.keySet();
	Iterator it = vertexList.iterator();
	while (it.hasNext())
	{
		vertex = (Integer)it.next();
		vlist = piece.get(vertex);
		if (vlist.size() < 2)
			attachpoints.add(vertex);
		
	}

	return attachpoints;
	
}
	public int getNumNodes()
	{
		return numNodes;
	}
	
	public int getNumEdges()
	{
		return numEdges;
	}
	
	public String getVertexList()
	{
		return vertexList;
	}
}