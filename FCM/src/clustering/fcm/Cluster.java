package clustering.fcm;

import java.util.ArrayList;

import clustering.fcm.DataPoint;

public class Cluster
{
	public DataPoint centroid;
	public ArrayList<DataPoint> memberDataPoints;
	
	Cluster(DataPoint centroid)
	{
		/* this.centroid = centroid;
		 *  This statement would create a pointer to the input datapoint and will not create a personal copy of the datapoint for this cluster centroid
		 */
		this.centroid = new DataPoint();
		for(int i=0;i<centroid.point.size();i++)
		{
			this.centroid.point.add(centroid.point.get(i));
		}
		
		memberDataPoints = new ArrayList<DataPoint>();
		
	}
	
	
}
