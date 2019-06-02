package models;

import java.util.Vector;
import utils.MathUtils;;
public class MA {
 
	double[] stdoriginalData={};
	int q;
	
	/** MA模型
	 * @param stdoriginalData //预处理过后的数据
	 * @param q //q为MA模型阶数
	 */
	public MA(double [] stdoriginalData, int q)
	{
		this.stdoriginalData=new double[stdoriginalData.length];
		System.arraycopy(stdoriginalData, 0, this.stdoriginalData, 0, stdoriginalData.length);
		this.q=q;
		
	}
	
	/**
	 * @return MA模型参数
	 */
	public Vector<double[]> MAmodel()
	{
		Vector<double[]> v=new Vector<double[]>();
		v.add(MathUtils.getMApara(MathUtils.autoCorGrma(stdoriginalData, q), q));
		//第一个存放噪声参数，后面q个存放ma参数sigma2 , ma1,  ma2...
		return v;//拿到MA模型里面的参数值
	}
		
	
}