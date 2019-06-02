package models;

import java.util.*;

import utils.MathUtils;

public class ARIMA {
 
	double[] originalData={};
	double[] originalDatafirDif={};
	double[] originalDatasecDif={};
	double[] originalDatathiDif={};
	double[] originalDataforDif={};
	double[] originalDatafriDif={};
	
	double stderrDara=0;
	double avgsumData=0;
	Vector<double[]> armaARMAcoe=new Vector<double[]>();
	Vector<double[]> bestarmaARMAcoe=new Vector<double[]>();
	int typeofPredeal=0; 
	
	/**
	 * 构造函数
	 * @param originalData 原始时间序列数据
	 */
	public ARIMA(double [] originalData,int typeofPredeal)
	{
		this.originalData=originalData;
		this.typeofPredeal=typeofPredeal;//数据预处理类型 1:一阶普通查分 7：季节性差分
	}
	
	/**
	 * 原始数据标准化处理：一阶季节性差分
	 * @return 差分过后的数据
	 */ 
	public double[] preDealDif(double[] originalData)
	{
		//seasonal Difference:Peroid=7
		double []tempData=new double[originalData.length-7];
		for(int i=0;i<originalData.length-7;i++)
		{
			tempData[i]=originalData[i+7]-originalData[i];
		}
		return tempData;
	}
		
	/**
	 * 得到一阶差分数据数据
	 * @param originalData
	 * @return 差分过后的数据
	 */
	public double[] preFirDif(double[] originalData) 
	{
		double []tempData=new double[originalData.length-1];
		for(int i=0;i<originalData.length-1;i++)
		{
			tempData[i]=originalData[i+1]-originalData[i];
		}
 
		return tempData;
	}
	
	/**
	 * 原始数据标准化处理：Z-Score归一化
	 * @param 待处理数据
	 * @return 归一化过后的数据
	 */
	public double[] preDealNor(double[] tempData)
	{
		//Z-Score
		avgsumData=MathUtils.avgData(tempData);
		stderrDara=MathUtils.stderrData(tempData);
		
		for(int i=0;i<tempData.length;i++)
		{
			tempData[i]=(tempData[i]-avgsumData)/stderrDara;
		}
		return tempData;
	}
	
	public ModelAndPara getARIMAmodel(int[] bestmodel)
	{
		double[] stdoriginalData=null;
		
		if(typeofPredeal==0)
			{
				stdoriginalData=new double[originalData.length];
				System.arraycopy(originalData, 0, stdoriginalData, 0,originalData.length);
			}
		else if(typeofPredeal==1)		//原始数据一阶普通差分处理
			{
				originalDatafirDif=new double[this.preFirDif(originalData).length];//原始数据一阶普通差分处理
				System.arraycopy(this.preFirDif(originalData), 0, originalDatafirDif, 0, originalDatafirDif.length);	
				//用于训练的数据变为差分后的数据
				stdoriginalData=new double[originalDatafirDif.length];
				System.arraycopy(originalDatafirDif, 0, stdoriginalData, 0, originalDatafirDif.length);	
			}
		else if (typeofPredeal==2)
			{
				originalDatafirDif=new double[this.preFirDif(originalData).length];//原始数据一阶普通差分处理
				System.arraycopy(this.preFirDif(originalData), 0, originalDatafirDif, 0,originalDatafirDif.length);	
 
				originalDatasecDif=new double[this.preFirDif(originalDatafirDif).length];
				System.arraycopy(this.preFirDif(originalDatafirDif), 0, originalDatasecDif, 0,originalDatasecDif.length);	
 
				stdoriginalData=new double[originalDatasecDif.length];
				System.arraycopy(originalDatasecDif, 0, stdoriginalData, 0, originalDatasecDif.length);	
			}
		else if(typeofPredeal==3)
			{
				originalDatafirDif=new double[this.preFirDif(originalData).length];//原始数据一阶普通差分处理
				System.arraycopy(this.preFirDif(originalData), 0, originalDatafirDif, 0,originalDatafirDif.length);	
	
				originalDatasecDif=new double[this.preFirDif(originalDatafirDif).length];
				System.arraycopy(this.preFirDif(originalDatafirDif), 0, originalDatasecDif, 0,originalDatasecDif.length);	
 
				originalDatathiDif=new double[this.preFirDif(originalDatasecDif).length];
				System.arraycopy(this.preFirDif(originalDatasecDif), 0, originalDatathiDif, 0,originalDatathiDif.length);	
	
				stdoriginalData=new double[originalDatathiDif.length];
				System.arraycopy(originalDatathiDif, 0, stdoriginalData, 0,originalDatathiDif.length);	
 
			}
		else if(typeofPredeal==4)
			{
			
				originalDatafirDif=new double[this.preFirDif(originalData).length];//原始数据一阶普通差分处理
				System.arraycopy(this.preFirDif(originalData), 0, originalDatafirDif, 0,originalDatafirDif.length);	
	
				originalDatasecDif=new double[this.preFirDif(originalDatafirDif).length];
				System.arraycopy(this.preFirDif(originalDatafirDif), 0, originalDatasecDif, 0,originalDatasecDif.length);	
	
				originalDatathiDif=new double[this.preFirDif(originalDatasecDif).length];
				System.arraycopy(this.preFirDif(originalDatasecDif), 0, originalDatathiDif, 0,originalDatathiDif.length);	
	
				originalDataforDif=new double[this.preFirDif(originalDatathiDif).length];
				System.arraycopy(this.preFirDif(originalDatathiDif), 0, originalDataforDif, 0,originalDataforDif.length);	
 
				stdoriginalData=new double[originalDataforDif.length];
				System.arraycopy(originalDataforDif, 0, stdoriginalData, 0,originalDataforDif.length);	
 
			}
		else if(typeofPredeal==5)
			{
				originalDatafirDif=new double[this.preFirDif(originalData).length];//原始数据一阶普通差分处理
				System.arraycopy(this.preFirDif(originalData), 0, originalDatafirDif, 0,originalDatafirDif.length);	
	
				originalDatasecDif=new double[this.preFirDif(originalDatafirDif).length];
				System.arraycopy(this.preFirDif(originalDatafirDif), 0, originalDatasecDif, 0,originalDatasecDif.length);	
	
				originalDatathiDif=new double[this.preFirDif(originalDatasecDif).length];
				System.arraycopy(this.preFirDif(originalDatasecDif), 0, originalDatathiDif, 0,originalDatathiDif.length);	
	
				originalDataforDif=new double[this.preFirDif(originalDatathiDif).length];
				System.arraycopy(this.preFirDif(originalDatathiDif), 0, originalDataforDif, 0,originalDataforDif.length);	
				
				originalDatafriDif=new double[this.preFirDif(originalDataforDif).length];
				System.arraycopy(this.preFirDif(originalDataforDif), 0, originalDatafriDif, 0,originalDatafriDif.length);	
				
				stdoriginalData=new double[originalDatafriDif.length];
				System.arraycopy(originalDatafriDif, 0, stdoriginalData, 0,originalDatafriDif.length);	
 
			}
		else//原始数据季节性差分处理	
			{
				stdoriginalData=new double[this.preDealDif(originalData).length];
				System.arraycopy(this.preDealDif(originalData), 0, stdoriginalData, 0,this.preDealDif(originalData).length);	
			}
		
		armaARMAcoe.clear();
		bestarmaARMAcoe.clear();
		
		if(bestmodel[0] == 0)
		{
			MA ma = new MA(stdoriginalData, bestmodel[1]);
			armaARMAcoe=ma.MAmodel(); //拿到ma模型的参数
			
		}
		else if(bestmodel[1] == 0)
		{
			AR ar = new AR(stdoriginalData, bestmodel[0]);
			armaARMAcoe=ar.ARmodel(); //拿到ar模型的参数
			
		}
		else
		{
			ARMA arma = new ARMA(stdoriginalData, bestmodel[0], bestmodel[1]);
			armaARMAcoe=arma.ARMAmodel();//拿到arma模型的参数
			
		}
		
		bestarmaARMAcoe = armaARMAcoe;
		ModelAndPara mp = new ModelAndPara(bestmodel, bestarmaARMAcoe);
		
		return mp;
 	}
	
	/**
	 * 计算ARMA模型的AIC
	 * @param para 装载模型的参数信息
	 * @param stdoriginalData   预处理过后的原始数据
	 * @param type 1：MA；2：AR；3：ARMA
	 * @return 模型的AIC值
	 */
	public double getmodelAIC(Vector<double[]> para, double[] stdoriginalData, int type)
	{
		double temp=0;
		double temp2=0;
		double sumerr=0;
		int p=0;//ar1,ar2,...,sig2
		int q=0;//sig2,ma1,ma2...
		int n=stdoriginalData.length;
		Random random=new Random();
		
		if(type==1)
		{
			double[] maPara=new double[para.get(0).length];
			System.arraycopy(para.get(0), 0, maPara, 0, para.get(0).length);
			
			q=maPara.length;
			double[] err=new double[q];  //error(t),error(t-1),error(t-2)...
			for(int k=q-1;k<n;k++)
			{
				temp=0;
				
				for(int i=1;i<q;i++)
				{
					temp+=maPara[i]*err[i];
				}
			
				//产生各个时刻的噪声
				for(int j=q-1;j>0;j--)
				{
					err[j]=err[j-1];
				}
				err[0]=random.nextGaussian()*Math.sqrt(maPara[0]);
				
				//估计的方差之和
				sumerr+=(stdoriginalData[k]-(temp))*(stdoriginalData[k]-(temp));
				
			}
			
			//return  (n-(q-1))*Math.log(sumerr/(n-(q-1)))+(q)*Math.log(n-(q-1));//AIC 最小二乘估计
			return (n-(q-1))*Math.log(sumerr/(n-(q-1)))+(q+1)*2;
		}
		else if(type==2)
		{
			double[] arPara=new double[para.get(0).length];
			System.arraycopy(para.get(0), 0, arPara, 0, para.get(0).length);
			
			p=arPara.length;
			for(int k=p-1;k<n;k++)
			{
				temp=0;
				for(int i=0;i<p-1;i++)
				{
					temp+=arPara[i]*stdoriginalData[k-i-1];
				}
				//估计的方差之和
				sumerr+=(stdoriginalData[k]-temp)*(stdoriginalData[k]-temp);
			}
		
			return (n-(q-1))*Math.log(sumerr/(n-(q-1)))+(p+1)*2;
			//return (n-(p-1))*Math.log(sumerr/(n-(p-1)))+(p)*Math.log(n-(p-1));//AIC 最小二乘估计
		}
		else
		{
			double[] arPara=new double[para.get(0).length];
			System.arraycopy(para.get(0), 0, arPara, 0, para.get(0).length);
			double[] maPara=new double[para.get(1).length];
			System.arraycopy(para.get(1), 0, maPara, 0, para.get(1).length);
				
			p=arPara.length;
			q=maPara.length;
			double[] err=new double[q];  //error(t),error(t-1),error(t-2)...
			
			for(int k=p-1;k<n;k++)
			{
				temp=0;
				temp2=0;
				for(int i=0;i<p-1;i++)
				{
					temp+=arPara[i]*stdoriginalData[k-i-1];
				}
			
				for(int i=1;i<q;i++)
				{
					temp2+=maPara[i]*err[i];
				}
			
				//产生各个时刻的噪声
				for(int j=q-1;j>0;j--)
				{
					err[j]=err[j-1];
				}
				//System.out.println("predictBeforeDiff="+1);
				err[0]=random.nextGaussian()*Math.sqrt(maPara[0]);
				//估计的方差之和
				sumerr+=(stdoriginalData[k]-(temp2+temp))*(stdoriginalData[k]-(temp2+temp));
			}
			
			return (n-(q-1))*Math.log(sumerr/(n-(q-1)))+(p+q)*2;
			//return (n-(p-1))*Math.log(sumerr/(n-(p-1)))+(p+q-1)*Math.log(n-(p-1));//AIC 最小二乘估计
		}
	}
	
	/**
	 * 对预测值进行反差分处理
	 * @param predictValue 预测的值
	 * @return 反差分过后的预测值
	 */
	public double aftDeal(double predictValue)
		{
			/*int temp=0;
			//System.out.println("predictBeforeDiff="+predictValue);
			if(typeofPredeal==0)
				temp=((int)predictValue);
			else if(typeofPredeal==1)
				temp=(int)(predictValue + originalData[originalData.length-1]);
			else if(typeofPredeal==2)	
				temp=(int)(predictValue+originalDatafirDif[originalDatafirDif.length-1]+originalData[originalData.length-1]);	
			else if(typeofPredeal==3)
				temp=(int)(predictValue+originalDatasecDif[originalDatasecDif.length-1]+originalDatafirDif[originalDatafirDif.length-1]+originalData[originalData.length-1]);			
			else if(typeofPredeal==4)
				temp=(int)(predictValue+originalDatathiDif[originalDatathiDif.length-1]+originalDatasecDif[originalDatasecDif.length-1]+originalDatafirDif[originalDatafirDif.length-1]+originalData[originalData.length-1]);			
			else if(typeofPredeal==5)
				temp=(int)(predictValue+originalDataforDif[originalDataforDif.length-1]+originalDatathiDif[originalDatathiDif.length-1]+originalDatasecDif[originalDatasecDif.length-1]+originalDatafirDif[originalDatafirDif.length-1]+originalData[originalData.length-1]);			
			else
				temp=(int)(predictValue+originalData[originalData.length-7]);	
				
			return temp>0?temp:0;*/
			double temp = 0.0;
			if(typeofPredeal==0){
				temp=predictValue;
			}
			else if(typeofPredeal==1){
				//System.out.println("predictBeforeDiff="+predictValue);
				temp = predictValue + originalData[originalData.length-1];
			}
			else if(typeofPredeal==2){
				temp = predictValue+originalDatafirDif[originalDatafirDif.length-1]+originalData[originalData.length-1];
			}
			else if(typeofPredeal==3){
				temp = predictValue+originalDatasecDif[originalDatasecDif.length-1]+originalDatafirDif[originalDatafirDif.length-1]+originalData[originalData.length-1];
			}
			else if(typeofPredeal==4){
				temp = predictValue+originalDatathiDif[originalDatathiDif.length-1]+originalDatasecDif[originalDatasecDif.length-1]+originalDatafirDif[originalDatafirDif.length-1]+originalData[originalData.length-1];
			}
			else if(typeofPredeal==5){
				temp = predictValue+originalDataforDif[originalDataforDif.length-1]+originalDatathiDif[originalDatathiDif.length-1]+originalDatasecDif[originalDatasecDif.length-1]+originalDatafirDif[originalDatafirDif.length-1]+originalData[originalData.length-1];
			}
			else{
				temp = predictValue+originalData[originalData.length-7];
			}
				
			return temp;
		}
		
	/**
	 * 进行一步预测
	 * @param p ARMA模型的AR的阶数
	 * @param q ARMA模型的MA的阶数
	 * @return 预测值
	 */
	public double predictValue(int p, int q, Vector<double[]> bestpara)
		{
			double[] stdOriginalData=null;
			if (typeofPredeal==0)
				{
					stdOriginalData=new double[originalData.length];
					System.arraycopy(originalData, 0, stdOriginalData, 0, originalData.length);
					
				}
			else if(typeofPredeal==1)
				{
					stdOriginalData=new double[originalDatafirDif.length];
					
					System.arraycopy(originalDatafirDif, 0, stdOriginalData, 0, originalDatafirDif.length);	
				}
			else if(typeofPredeal==2)
				{
					stdOriginalData=new double[originalDatasecDif.length];//普通二阶差分处理
					System.arraycopy(originalDatasecDif, 0, stdOriginalData, 0, originalDatasecDif.length);	
				}
				
			else if(typeofPredeal==3)
				{
					stdOriginalData=new double[originalDatathiDif.length];//普通三阶差分处理
					System.arraycopy(originalDatathiDif, 0, stdOriginalData, 0, originalDatathiDif.length);	
				}
				
			else if(typeofPredeal==4)
				{
					stdOriginalData=new double[originalDataforDif.length];//普通四阶差分处理
					System.arraycopy(originalDataforDif, 0, stdOriginalData, 0, originalDataforDif.length);	
				}
				
			else if(typeofPredeal==5)
				{
					stdOriginalData=new double[originalDatafriDif.length];//普通五阶差分处理
					System.arraycopy(originalDatafriDif, 0, stdOriginalData, 0, originalDatafriDif.length);	
				}
			else
				{
					stdOriginalData=new double[this.preDealDif(originalData).length];//季节性一阶差分
					System.arraycopy(this.preDealDif(originalData), 0, stdOriginalData, 0, this.preDealDif(originalData).length);	
				}
			//System.out.println("typeofPreDeal= "+typeofPredeal+typeofPredeal);
			
//			for(int i=0;i<originalDatafirDif.length;i++)
//				System.out.println(originalDatafirDif[i]);

			double predict=0;
			int n = stdOriginalData.length;
			double temp=0,temp2=0;
			double[] err=new double[q+1];
		
			Random random=new Random();
			
			if(p==0)
			{
				double[] maPara=bestpara.get(0); //第一个存放噪声参数 
				
				//产生各个时刻的噪声
				err[0]=random.nextGaussian()*Math.sqrt(maPara[0]);
				
				for(int j=1; j<=q; j++)
				{
					err[j]=random.nextGaussian()*Math.sqrt(maPara[0]);
				}
				temp = err[0];
				for(int i=1; i<=q; i++)
				{
					temp += maPara[i]*err[i];
				}
				
				predict= temp; //产生预测
			}
			else if(q==0)
			{
				double[] arPara = bestpara.get(0);
			
				temp=0;
				for(int i=0; i<p; i++)
				{
					temp += arPara[i]*stdOriginalData[n-i-1];
				}
				
				temp += arPara[arPara.length-1];
				predict=temp;
			}
			else
			{
				double[] arPara = bestpara.get(0);
				temp = 0;
				for(int i=0; i<p; i++)
				{
					temp += arPara[i]*stdOriginalData[n-i-1];
				}
				temp += arPara[arPara.length-1];
				
				double[] maPara = bestpara.get(1);
				//产生各个时刻的噪声
				err[0]=random.nextGaussian()*Math.sqrt(maPara[0]);
				
				for(int j=1; j<=q; j++)
				{
					err[j]=random.nextGaussian()*Math.sqrt(maPara[0]);
				}
				temp2 = err[0];
				for(int i=1; i<=q; i++)
				{
					temp2 += maPara[i]*err[i];
				}
				
				predict = (temp2+temp);
				//System.out.println("predict=p,q "+predict);
			}
			return predict;
		}
	 
	}