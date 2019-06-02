package models;

import java.util.*;
import java.util.Map.Entry;

import utils.MathUtils;

public class ARIMApred {
		ModelAndPara               mp=null;
		int 	         bestModelNums=0;
		int[]				bestModel=null; //{p,q}
		double[]     dataArrayPredict=null;
		
		public double[] getDataArrayPredict() {
			return dataArrayPredict;
		}
		int                     range=1;
	    int 			originDataLen;
		double 				threshold;
		int  bestpreDictValue=0;
		int           bestDif=0;
		
		//train predict
		int 					predLen;
		int 					memory;
		double  	   predictErrtemp=0.0; 
		double         		 validate=0;
		double[]  		 preDataArray=null;
		double[] 	   traindataArray=null;
		HashMap<String, Double>    predictErr=new HashMap<>();
	 
		public ARIMApred(double[] dataArray, int initP, int initD, int initQ, int bestModelNums, int predLen, int memory)
		{
			this.originDataLen = dataArray.length;
			this.predLen = predLen;
			this.memory = memory;
			this.bestModelNums = bestModelNums;
			this.dataArrayPredict = new double[predLen];
			
			for(int p=0; p<=initP+this.range; p++){
				for(int q=0; q<=initQ+this.range; q++){
					//当p q 不同时为0
					if(p != 0 || q != 0){
						String model = p+"#"+initD+"#"+q;
						predictErr.put(model, 0.0);
						//System.out.println(predictErr.get(model));
					}
				}
			}
			
			/** 模型训练 */
			//System.out.println("begin to train...");
			Vector<int[]> trainResult = this.train(dataArray, initP, initQ, initD);
			
			/** 模型预测 */
			//System.out.println("begin to predict...");
			for(int index=0; index<predLen; index++){
				//System.out.println("predict index:" + index);
				double tempPredict=0;
				for(int i=0; i<trainResult.size(); i++)
				{ 
					//System.out.println("predict with model:"+ i +"/" + trainResult.size());
					
					tempPredict += this.predict(dataArray,
												trainResult.get(i),
												index);
				}
				//计算多个模型的平均预测值
				tempPredict = tempPredict/trainResult.size();
				if(tempPredict < 0){
					tempPredict = 0.0;
				}
//				  System.out.println(String.format("index/predLen:%d/%d, tempPredict:%f",
//						  			index, predLen, tempPredict));
//				  System.out.println("********************");
				
				dataArrayPredict[index] = tempPredict;
			}
		}
		
		/**
		 * 预测
		 * @param dataArray
		 * @param model
		 * @param index
		 * @param predLen
		 * @return
		 */
		private double predict(double[] dataArray,
							   int[] model,
							   int index) {
			
			if(predLen < 0)
				return (int)(dataArray[dataArray.length-1] + dataArray[dataArray.length-2])/2;
			
			this.preDataArray = new double[dataArray.length - index];
			
			System.arraycopy(dataArray, index, preDataArray, 0, preDataArray.length);
			
			//对待预测的原始数据做差分处理 
			ARIMA arima=new ARIMA(preDataArray, model[1]); 
			
			//参数初始化
			mp = arima.getARIMAmodel(new int[]{model[0], model[2]});
			double predictValuetemp = arima.aftDeal(arima.predictValue(mp.model[0], mp.model[1], mp.para));
		
			return predictValuetemp;
		}
		/**
		 * 训练模型
		 * @param dataArray
		 * @return
		 */
		public Vector<int[]> train(double[] dataArray, int initP, int initQ, int initD)
		{
				for(int p=0; p<=initP+this.range; p++){
					for(int q=0; q<=initQ+this.range; q++){
						//当p q 不同时为0
						if(p != 0 || q != 0){
							//每次训练的模型
							String model = p+"#"+initD+"#"+q;
							bestModel = new int[]{p,q};
							predictErrtemp = 0.0;
//							System.out.print(String.format("model:p=%d, d=%d, q=%d",
//									 						p, initD, q));
							//准备多组训练数据，计算误差选择最优model
							for(int datai=0; datai<memory; datai++){
								//准备训练数据 --->    训练集traindataArray[] + 验证 validate
								this.preData(dataArray, datai, memory);
								//对训练数据做几阶差分处理0,1,2,7
								ARIMA arima = new ARIMA(traindataArray, initD); 
								mp = arima.getARIMAmodel(bestModel);
								
								double predictValuetemp = arima.aftDeal(arima.predictValue(
										mp.model[0], mp.model[1], mp.para));
								
								predictErrtemp += 100*Math.abs((predictValuetemp-validate)/validate);
							}
							
							predictErrtemp /= memory;
							//记录每个模型的误差
							predictErr.put(model, predictErr.get(model)+predictErrtemp);
//							System.out.println(" predictErr=" + predictErr.get(model));
						}
					}
				}
			
			double minvalue=Double.MAX_VALUE;
			int tempP=0;
			int tempD=0;
			int tempQ=0;
			Vector<int[]> bestModelVector=new Vector<int[]>();
			
			HashMap<String, Boolean> flag = new HashMap<>();
			for(int p=0; p<=initP+this.range; p++){
				for(int q=0; q<=initQ+this.range; q++){
						String model = p+"#"+initD+"#"+q;
						flag.put(model, false);
					}
				}

			//System.out.println("select top bestModels: " + this.bestModelNum);
			
			for(int ii=0; ii<bestModelNums; ii++){
				minvalue = Double.MAX_VALUE;
				String key = null;
				for(Entry<String, Double> model_err : predictErr.entrySet()){
					if(flag.get(model_err.getKey()) == false)
					{
						if(model_err.getValue() < minvalue)
						{
							minvalue=model_err.getValue();
							key = model_err.getKey();
							String[] tmps = key.split("#");
							tempP=Integer.parseInt(tmps[0]);
							tempD=Integer.parseInt(tmps[1]);
							tempQ=Integer.parseInt(tmps[2]);
							
						}
					}
				}
				flag.put(key, true);
				bestModelVector.add(new int[]{tempP,tempD,tempQ});
//				String str=String.format("model %d :p=%d, d=%d, q=%d",
//													ii, tempP, tempD, tempQ);
//				System.out.println(str);
//				System.out.println("AverageMAPE=" + minvalue/predLen);
			}
			
			//System.out.println("----------------------------------------");
			
			return bestModelVector;
		}
		/**
		 * 准备训练数据
		 * @param dataArray
		 * @param dataIndex
		 * @param memory
		 */
		public void preData(double[] dataArray, int dataIndex, int memory)
		{
			this.traindataArray = new double[dataArray.length - memory]; //用于训练的数据总长度
			//dataIndex 0 ~ (momery-1)
			System.arraycopy(dataArray, dataIndex, traindataArray, 0, traindataArray.length);
			this.validate = dataArray[traindataArray.length + dataIndex];//最后一个值作为训练时候的验证值。
			if(this.validate == 0.0){
				//System.out.println("除数会有0出现");
				this.validate = 0.001;
			}
			
		}
}
