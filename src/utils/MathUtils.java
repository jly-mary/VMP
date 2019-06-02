package utils;

import java.util.ArrayList;
import java.util.Collections;

import Jama.Matrix;
 
public  class MathUtils{
	/**
	 * 计算平均值
	 * @param dataArray
	 * @return
	 */
	public static double avgData(double[] dataArray)
	{
		return sumData(dataArray)/dataArray.length;
	}
	/**
	 * 求和
	 * @param dataArray
	 * @return
	 */
	public static double sumData(double[] dataArray)
	{
		double sumData=0;
		for(int i=0;i<dataArray.length;i++)
		{
			sumData+=dataArray[i];
		}
		return sumData;
	}
	/**
	 * 求标准差
	 * @param dataArray
	 * @return
	 */
	public static double stderrData(double[] dataArray)
	{
		return Math.sqrt(varerrData(dataArray));
	}
	/**
	 * 求方差
	 * @param dataArray
	 * @return
	 */
	public static double varerrData(double[] dataArray)
	{
		double variance=0;
		double avgsumData=avgData(dataArray);
		
		for(int i=0;i<dataArray.length;i++)
		{
			dataArray[i]-=avgsumData;
			variance+=dataArray[i]*dataArray[i];
		}
		return variance/dataArray.length;//variance error;
	}
	
	/**
	 * 计算自相关函数   Tho(k)=Grma(k)/Grma(0)
	 * @param dataArray 数列
	 * @param order 阶数
	 * @return
	 */
	public static double[] autoCorData(double[] dataArray,int order)
	{
		double[] autoCor = new double[order+1];
		double varData = varerrData(dataArray);//标准化过后的方差
		
		for(int i=0;i<=order;i++)
		{
			autoCor[i]=0;
			for(int j=0;j<dataArray.length-i;j++)
			{
				autoCor[i]+=dataArray[j+i]*dataArray[j];
			}
			autoCor[i] /= (dataArray.length-i);
			autoCor[i] /= varData;
		}
		return autoCor;
	}
	
	/**
	 * 自相关系数 
	 * @param dataArray
	 * @param order
	 * @return
	 */
	public static double[] autoCorGrma(double[] dataArray,int order)
	{
		double[] autoCor=new double[order+1];
		for(int i=0;i<=order;i++)
		{
			autoCor[i]=0;
			for(int j=0;j<dataArray.length-i;j++)
			{
				autoCor[i] += dataArray[j+i]*dataArray[j];
			}
			autoCor[i]/=(dataArray.length-i);
			
		}
		return autoCor;
	}
	
	/**
	 * 偏自相关系数
	 * @param dataArray
	 * @param order
	 * @return
	 */
	public static double[] parAutoCorData(double[] dataArray,int order)
	{
		double parautocor[]=new double[order];
		
		for(int i=1;i<=order;i++)
	    {
			parautocor[i-1] = parCorrCompute(dataArray, i, 0)[i-1];
	    }
		return parautocor;
	}
	
	/**
	 * 产生Toplize矩阵
	 * @param dataArray
	 * @param order
	 * @return
	 */
	public static double[][] toplize(double[] dataArray,int order)
	{//返回toplize二维数组
		double[][] toplizeMatrix=new double[order][order];
		double[] atuocorr=autoCorData(dataArray,order);
 
		for(int i=1;i<=order;i++)
		{
			int k=1;
			for(int j=i-1;j>0;j--)
			{
				toplizeMatrix[i-1][j-1]=atuocorr[k++];
			}
			toplizeMatrix[i-1][i-1]=1;
			int kk=1;
			for(int j=i;j<order;j++)
			{
				toplizeMatrix[i-1][j]=atuocorr[kk++];
			}
		}
		return toplizeMatrix;
	}
 
	/**
	 * ACF-MA 模型的参数 
	 * @param autocorData
	 * @param q
	 * @return
	 */
	public static double[] getMApara(double[] autocorData,int q)
	{
		double[] maPara=new double[q+1];//第一个存放噪声参数，后面q个存放ma参数sigma2,ma1,ma2...
		double[] tempmaPara=new double[q+1];
		
		double temp=0;
		boolean iterationFlag=true;
		//解方程组
		//迭代法解方程组
		maPara[0]=1;//初始化
		int count=10000;
		while(iterationFlag&&count-->0)
		{
			temp=0;
			for(int i=1;i<maPara.length;i++)
			{
				temp+=maPara[i]*maPara[i];
			}
			tempmaPara[0] = autocorData[0]/(1 + temp);
			
			for(int i=1;i<maPara.length;i++)
			{
				temp=0;
				for(int j=1;j<maPara.length-i;j++)
				{
					temp+=maPara[j]*maPara[j+i];
				}
				tempmaPara[i]=-(autocorData[i]/tempmaPara[0]-temp);
			}
			iterationFlag=false;
			for(int i=0;i<maPara.length;i++)
			{
				if(Math.abs(maPara[i]-tempmaPara[i])<0.00001)
				{
					iterationFlag=true;
					break;
				}
			}
			System.arraycopy(tempmaPara, 0, maPara, 0, tempmaPara.length);
		}
	
		return maPara;
	}
	
	/**
	 *  PACF-AR 计算AR偏自相关系数
	 * @param dataArray
	 * @param p
	 * @param q
	 * @return
	 */
	public static double[] parCorrCompute(double[] dataArray,int p, int q)
	{
		double[][] toplizeArray=new double[p][p]; //p阶toplize矩阵；
		
		double[] atuocorr = autoCorData(dataArray,p+q);  //返回p+q阶的自相关函数
		double[] autocorrF = autoCorGrma(dataArray, p+q); //返回p+q阶的自相关系数
		for(int i=1;i<=p;i++)
		{
			int k=1;
			for(int j=i-1;j>0;j--)
			{
				toplizeArray[i-1][j-1]=atuocorr[q + k++];
			}
			toplizeArray[i-1][i-1]=atuocorr[q];
			int kk=1;
			for(int j=i;j<p;j++)
			{
				toplizeArray[i-1][j]=atuocorr[q + kk++];
			}
		}
		
	    Matrix toplizeMatrix = new Matrix(toplizeArray);//由二位数组转换成二维矩阵
	    Matrix toplizeMatrixinverse = null;
	    toplizeMatrixinverse=toplizeMatrix.inverse();//矩阵求逆运算
		
	    double[] temp=new double[p];
	    for(int i=1;i<=p;i++)
	    {
	    	temp[i-1]=atuocorr[q+i];
	    }
	    
		Matrix autocorrMatrix=new Matrix(temp, p);
		Matrix parautocorDataMatrix= toplizeMatrixinverse.times(autocorrMatrix); //  [Fi]=[toplize]x[autocorr]';
		//矩阵计算结果应该是按照[a b c]'  列向量存储的
		//System.out.println("row="+parautocorDataMatrix.getRowDimension()+"  Col="+parautocorDataMatrix.getColumnDimension());
		//parautocorDataMatrix.print(p, 2);//(输出几行,小数点后保留位数)
		//System.out.println(parautocorDataMatrix.get(p-1,0));
		
		double[] result=new double[parautocorDataMatrix.getRowDimension()+1];
		for(int i=0;i<parautocorDataMatrix.getRowDimension();i++)
		{
			result[i]=parautocorDataMatrix.get(i,0);
		}
		
		//估算sigmat2
		double sum2=0;
		for(int i=0;i<p;i++)
			for(int j=0;j<p;j++)
			{
				sum2+=result[i]*result[j]*autocorrF[Math.abs(i-j)];
			}
		result[result.length-1]=autocorrF[0]-sum2; //result数组最后一个存储干扰估计值
		
		
		return result;   //返回0列的最后一个就是k阶的偏自相关系数 pcorr[k]=返回值
	}
	
	/**
	 * 计算平均绝对百分误差
	 * @param predResults
	 * @param originData
	 * @return
	 */
	public static double mape(double[] predResults, double[] originData){
		double mape = 0.0;
		int N = predResults.length;
		for(int i=0; i<N; i++){
			double origin = originData[i];
			if(origin==0.0){
				origin = 0.001;
			}
			mape += Math.abs((predResults[i]-origin)/origin);
		}
		return mape*100/N;
	}
	
	/**
	 * 计算平均绝对误差
	 * @param predResults
	 * @param originData
	 * @return
	 */
	public static double mae(double[] predResults, double[] originData){
		double mae = 0.0;
		int N = predResults.length;
		for(int i=0; i<N; i++){
			mae += Math.abs(predResults[i]-originData[i]);
		}
		return mae/N;
	}
	/**
	 * 一阶差分
	 * @param originalData
	 * @return
	 */
	public static double[] diff1(double[] originalData) 
	{
		double [] diffData=new double[originalData.length-1];
		for(int i=0;i<originalData.length-1;i++)
		{
			diffData[i]=originalData[i+1]-originalData[i];
		}
 
		return diffData;
	}
	
	public  static  double getMax(double[] arr){
		ArrayList<Double> arrList = new ArrayList<>();
		for(double num : arr){
			arrList.add(num);
		}
		return Collections.max(arrList);
	}
	
	public  static  double getMin(double[] arr){
		ArrayList<Double> arrList = new ArrayList<>();
		for(double num : arr){
			arrList.add(num);
		}
		return Collections.min(arrList);
	}
}
