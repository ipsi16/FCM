/*
 * 1. Read i/p from  file 
 * 2. Assign cluster centroids
 * 3. Calculate distance --> membership
 * 4. Calculate new centroid
 * 5. Check if u(n)-u(n-1)<del
 * 	a. if yes then print results and quit
 *  b. else repeat step 3 to 4
 *  
 */


package clustering.fcm;

import indexes.DBIndex;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;

public class Fcm 
{
	public static final int n = 500;
	public static final int noOfClusters = 3;
	public static final float epsolon = 0.01f;
	public static final int m = 2;	
	public static ArrayList<Cluster> clusters = new ArrayList<Cluster>();
	public static ArrayList<DataPoint> orgDatapoints = new ArrayList<DataPoint>();
	public static ArrayList<DataPoint> datapoints = new ArrayList<DataPoint>();
	public static float[][] membership = new float[noOfClusters][n];
	public static float[][] oldMembership = new float[noOfClusters][n];
	
	public static void main(String[] args)
	{
		
		
		//read i/p from file
		try
		{
			fetchData();
			
		} catch (NumberFormatException e) {
			System.err.println("Invalid number entries in the file\nFile should only contan comma seperated numbers");
			return;
		}
		if(datapoints.size()<noOfClusters)
		{
			System.out.println("Insufficient points");
			return;
		}
		
		//normalize datapoints
		normalise();
		
			
		//allocate cluster centroids			
		for(int i=0;i < noOfClusters; i++)
		{
			Cluster c = new Cluster(datapoints.get(i));
			clusters.add(c);
		}
		
		//calculate membership value of each point for each clusters
		calculateMembership();
				
		//recalculate centroid
		while(!stopSignal())
		{
			determineNewCentroid();
			calculateMembership();
		}
        
		//Final Output
        for(int i=0;i<noOfClusters;i++)
        {
        	System.out.print("Cluster "+(i+1)+" : ");
            for(int j=0;j<orgDatapoints.size();j++)
            {
                if(membership[i][j]!=0)
                {
                	System.out.print(membership[i][j]*100+"% of "+orgDatapoints.get(j).point +"\t");
                }
            }
            System.out.println();         
		}
        
        allotPointsToClusters();
        
        /*int i =0;
        for(Cluster c : clusters)
        {
        	System.out.println("\nCluster "+(i+1));
        	for(DataPoint dp : c.memberDataPoints )
        	{
        		System.out.println(dp.point);
        	}
        	i++;
        }*/
        
        //DB Index
        DBIndex dbindex = new DBIndex(clusters);
        System.out.println(dbindex.returnIndex());
}

	private static void fetchData() throws NumberFormatException
	{		
		try
		{
			FileReader freader = new FileReader("Data.txt");
			BufferedReader breader = new BufferedReader(freader);
			String dataLine = breader.readLine();
			while(dataLine!=null)
			{
				ArrayList<Float> dim = new ArrayList<Float>();				
				String[] dimString = dataLine.split(",");				
				for (String string : dimString)
				{
					string = string.trim();
					dim.add(Float.parseFloat(string));					
				}
				DataPoint datapoint = new DataPoint(dim);
				datapoints.add(datapoint);	
				orgDatapoints.add(DataPoint.copyDataPoint(datapoint));
				dataLine = breader.readLine();
			}
			breader.close();
		}
		catch(NumberFormatException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	private static void calculateMembership()
	{
		for(int i = 0; i < noOfClusters; i++)
		{
			for(int j=0; j<datapoints.size();j++)
			{
				oldMembership[i][j]= membership[i][j];
			}
		}
	
		
		for(int i = 0; i < noOfClusters; i++)
		{
			for(int j=0; j<datapoints.size();j++)
			{
				membership[i][j]=-1;
			}
		}
				
		for(int i = 0; i < noOfClusters; i++)
		{
			for(int j=0; j<datapoints.size();j++)
			{
				float dij = DataPoint.distanceBetween(clusters.get(i).centroid,datapoints.get(j) );
				
				if(dij==0.0f)
				{
					membership[i][j]= 1;
					for(int h=0;h<noOfClusters;h++)
					{
						if(h!=i)
						{	
							membership[h][j]=0; 
						}
					}
				}
				else if(membership[i][j]==-1) 
				{
					membership[i][j]=0;
					for(int k = 0; k < noOfClusters; k++)
					{
						membership[i][j]+=Math.pow((dij/DataPoint.distanceBetween(datapoints.get(j),clusters.get(k).centroid)), 2.0/(m-1));
						
					}
					membership[i][j] = 1/membership[i][j];
				}
				
			}
		}		
	}
	
	private static void determineNewCentroid()
	{
		//System.out.println("Cluster Centroids: \n");
		for(int i=0;i<noOfClusters;i++)
		{
			ArrayList<Float> dp = new ArrayList<Float>();
			for(int k=0;k<datapoints.get(0).point.size();k++)
			{
				dp.add(0.0f);
			}
			
			float denominator = 0;
			for(int j=0;j<datapoints.size();j++)
			{
				denominator +=Math.pow(membership[i][j], m) ;
				for(int k=0;k<datapoints.get(j).point.size();k++)
				{
					dp.set(k, dp.get(k)+(float)Math.pow(membership[i][j], m)*datapoints.get(j).point.get(k));
				}
			}
			for(int k = 0;k<dp.size();k++)
			{
				dp.set(k, dp.get(k)/denominator);
			}
			clusters.get(i).centroid = new DataPoint(dp);
			//System.out.println(clusters.get(i).centroid.point);
			
		}
	}
	
	private static boolean stopSignal()
	{
		for(int i=0;i<noOfClusters;i++)
		{
			for(int j=0;j<datapoints.size();j++)
			{
				if(Math.abs(oldMembership[i][j]-membership[i][j])>epsolon)
					return false;
			}
		}
		return true;
	}

	public static void normalise()				//standard normalization between range 0 - 1
	{
		//arrays that store max and min value for every ith dimension
		float[] max,min;
		max = new float[datapoints.get(0).point.size()];
		min = new float[datapoints.get(0).point.size()];
		
		//initialising max and min array to dimensions of first datapoint
		for(int i = 0; i<datapoints.get(0).point.size();i++)
		{
			max[i] = datapoints.get(0).point.get(i).floatValue(); 
			min[i] = datapoints.get(0).point.get(i).floatValue(); 
		}
			
		//finding max and min values for each dimension 
		for(DataPoint dp : datapoints)
		{
			ArrayList<Float> currPoint = dp.point;
			for(int i =0 ; i<currPoint.size();i++)
			{
				if(currPoint.get(i)>max[i])
				{
					max[i]=currPoint.get(i);
				}
				else if(currPoint.get(i)<min[i])
				{
					min[i]=currPoint.get(i);
				}
			}			
		}
		
		//applying normalization formula new value = (oldValue - oldMinVal)/(oldMaxVal - oldMinVal)
		for(DataPoint dp : datapoints)
		{
			ArrayList<Float> currPoint = dp.point;
			for(int i =0 ; i<currPoint.size();i++)
			{
				currPoint.set(i, (currPoint.get(i)-min[i])/(max[i]-min[i]));
			}
		}
	}
	
	public static void allotPointsToClusters()
	{
		for(int j=0;j<datapoints.size();j++)		//For each datapoint
		{
			int maxPos=0;							//Finding cluster to which the jth cluster has maximum membership
			for(int i=0; i<noOfClusters;i++)
			{
				if(membership[i][j]>membership[maxPos][j])
					maxPos = i;
			}
			
			clusters.get(maxPos).memberDataPoints.add(datapoints.get(j));	//adding datapoint to cluster with max membership
		}
	}
}
