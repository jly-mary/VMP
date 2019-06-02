package utils;

import java.util.List;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.ArrayList;
import java.util.HashMap;

import entities.VM;
import models.ARIMApred;

/**
 * 预测工具类
 * @author Administrator
 *
 */
public class PredUtils {
	public static int predLen = 1;
	public static int bestModelsNum = 5;
	
	public static HashMap<String, Double> pred(VM vm) {
		HashMap<String, Double> predResults = new HashMap<String, Double>();
		//获取vm的历史需求
		HashMap<String, List<Double>> historys = vm.getVMHistory();
		
		for(String key : historys.keySet()){
			ArrayList<Double> demand = (ArrayList<Double>) historys.get(key);
			int len = demand.size();
			double[] dataArray = new double[demand.size()];
			for(int i=0; i<dataArray.length; i++){
				dataArray[i] = demand.get(i);
			}
			//单个模型训练的次数
			int memory = 20;
			
			//阶数准备
			double[] diffData = MathUtils.diff1(dataArray);
			int d = 1;
			DrawUtils.drawTimeSeries(diffData);
			int p = Pacf(diffData, memory);
			int q = Acf(diffData, memory);
			
			/*if(p == memory && q == memory){
				return null;
			}*/
			//训练模型
			ARIMApred myarima = new ARIMApred(dataArray, p, d, q, bestModelsNum, predLen, memory);

			//实际预测值
			double[] pred = myarima.getDataArrayPredict();
			//修正预测值
			predResults.put(key, pred[0] + 2*MathUtils.stderrData(dataArray));
//			predResults.put(key, pred[0] + MathUtils.avgData(dataArray));
//			predResults.put(key, pred[0]);
		}
		
		//预测输出
		return predResults;
	}
	
	public static int Acf(double[] data, int order){
  		
  		double[] acfData = MathUtils.autoCorData(data, order);
  		int len = acfData.length;
  		
  		XYSeries acfSeries = new XYSeries("acf");
        XYSeriesCollection dataSet = new XYSeriesCollection();  
        
        for(int i=0; i<len; i++){
        	acfSeries.add((double)(i), acfData[i]);
        }
        
        dataSet.addSeries(acfSeries);
        
        double[] xAxis = {0, order+1};
        double[] yAxis = {-1, 1};
        
        /*DrawUtils.draw(dataSet,
						"acf", 
						"order", "acf",
						xAxis, yAxis);*/
        
        int q = 0;
        double acf=0.0;
        for(int i=0; i<len; i++){
        	acf = acfData[i];
        	if(Math.abs(acf) < 0.05){
        		q = i;
        		break;
        	}
        }
        return q;
  	}
  	public static int Pacf( double[] data, int order){
  	
  		double[] pacfData = MathUtils.parAutoCorData(data, order);
  		int len = pacfData.length;
  		
  		XYSeries pacfSeries = new XYSeries("acf");
        XYSeriesCollection dataSet = new XYSeriesCollection();  
        
        for(int i=0; i<len; i++){
        	pacfSeries.add((double)(i), pacfData[i]);
        }
        
        dataSet.addSeries(pacfSeries);
        double[] xAxis = {0, order+1};
        double[] yAxis = {-1, 1};
        
        /*DrawUtils.draw(dataSet,
						"pacf", 
						"order", "pacf",
						xAxis, yAxis);*/
        
        int p = 0;
        double pacf=0.0;
        for(int i=0; i<len; i++){
        	pacf = pacfData[i];
        	if(Math.abs(pacf) < 0.05 ){
        		p = i;
        		break;
        	}
        }
        return p;
  	}
}
