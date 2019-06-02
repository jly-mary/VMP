package models;

import java.util.*;

import utils.MathUtils;
 
public class AR {
	
	double[] stdoriginalData={};
	int p;
	
	/**
	 * AR模型
	 * @param stdoriginalData
	 * @param p //p为MA模型阶数
	 */
	public AR(double [] stdoriginalData,int p)
	{
		this.stdoriginalData=new double[stdoriginalData.length];
		System.arraycopy(stdoriginalData, 0, this.stdoriginalData, 0, stdoriginalData.length);
		this.p=p;
	}
	
	/**
	 * @returnAR模型参数
	 */
	public Vector<double[]> ARmodel()
	{
		Vector<double[]> v = new Vector<double[]>();
		v.add(MathUtils.parCorrCompute(stdoriginalData, p, 0));
		return v;
	}
	
}