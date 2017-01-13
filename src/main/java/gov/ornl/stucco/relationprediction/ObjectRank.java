package gov.ornl.stucco.relationprediction;
//This program is just my solution to the problem of how to sort objects that are associated with some value, where 
//the value is not stored in some variable of it.  So I just put the object in the obj variable, the value in the 
//value variable, and put all of the ObjectRank objects in a sortable collection, then do Collections.sort on it.
//So it is generally useful for lots of things.


public class ObjectRank implements Comparable<ObjectRank>
{
	public Object obj;
	public Double value;

	
	ObjectRank(Object obj, double value)
	{
		this.obj = obj;
		this.value = value;
	}
	
	
	public int compareTo(ObjectRank o) 
	{
		double order = value - o.value;
		
		if(order > 0)
			return 1;
		else if(order < 0)
			return -1;
		else
			return 0;
	}
	
	public String toString()
	{
		return obj + "\t" + value;
	}
	
}
