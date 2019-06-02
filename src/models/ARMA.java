package models;

import java.util.Vector;

import utils.MathUtils;

public class ARMA {
	
	double[] stdoriginalData={};
	int p;
	int q;
	
	/**
	 * ARMA模型
	 * @param stdoriginalData
	 * @param p,q //p,q为MA模型阶数
	 */
	public ARMA(double [] stdoriginalData,int p,int q)
	{
		this.stdoriginalData=new double[stdoriginalData.length];
		System.arraycopy(stdoriginalData, 0, this.stdoriginalData, 0, stdoriginalData.length);
		this.p=p;
		this.q=q;	
	}
	
	public Vector<double[]> ARMAmodel()
	{
		double[] arCoe = MathUtils.parCorrCompute(stdoriginalData, p, q);
		double[] autoCorData = getAutoCorofMA(p, q, stdoriginalData, arCoe);
		double[] maCoe = MathUtils.getMApara(autoCorData, q);//得到MA模型里面的参数值
		
		Vector<double[]> v=new Vector<double[]>();
		v.add(arCoe);
		v.add(maCoe);
		return v;
	}
	/**
	 * 得到MA的自相关系数
	 * @param p
	 * @param q
	 * @param stdoriginalData
	 * @param autoCordata
	 * @return
	 */
	public double[] getAutoCorofMA(int p, int q, double[] stdoriginalData, double[] autoRegress)
	{
		int temp=0;
		double[] errArray=new double[stdoriginalData.length-p];
		int count=0;
		for(int i=p;i<stdoriginalData.length;i++)
		{
			temp=0;
			for(int j=1;j<=p;j++)
				temp += stdoriginalData[i-j]*autoRegress[j-1];
			errArray[count++]=stdoriginalData[i]-temp;//保存估计残差序列
		}
		return MathUtils.autoCorGrma(errArray, q);
	}
}