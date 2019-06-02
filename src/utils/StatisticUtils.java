package utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.junit.Test;

import entities.VM;

import java.util.TreeMap;

/**
 * 统计信息工具类
 * @author Administrator
 *
 */
public class StatisticUtils {
	
	/**
	 * 测试统计方法
	 */
	@Test
	public void testGetStatisticDemand(){
		List<Double> history = new ArrayList<>();
		for(int n=0; n<100; n++){
			//history.add(pointFormat(Math.random()));
		}
		System.out.println(history);
		Double result = getStatisticDemandOne(history, 0.5);
		System.out.println(result);
	}

	public static HashMap<String, Double>  getStatisticDemand(VM vm){
		//根据记录的资源项目数，转换为相应的历史数据分别统计
		//history 
		//			    1,   2,  3,  4,  ...
		//map    cpu   0.2 					----> record
		//       ram   0.3
		//       disk  0.2
		//       bw    0.3
		//给定的历史数据和概率
		HashMap<String, List<Double>> histories = vm.getVMHistory();
		double probability = vm.getP();
		
		HashMap<String, Double> statisticDemands = new HashMap<>();
		//将记录转换为histories
		for(String key : histories.keySet()){
			statisticDemands.put(key, 
					getStatisticDemandOne(histories.get(key), probability));
		}
		return statisticDemands;
	}
	private static Double getStatisticDemandOne(List<Double> history, double probability){
		//统计结果
		Double StatisticDemand = 0.0D;
		BigDecimal _1 = new BigDecimal("1.0");
		//历史数据的总次数
		double N = history.size();
		//转换probability
		BigDecimal voilateP = new BigDecimal(probability+"");
		BigDecimal p = _1.subtract(voilateP);
		
		//保存需求以及相应出现的频次（需求是从小到大排列的）
		/*TreeMap<BigDecimal, BigDecimal> demandFreqMap = new TreeMap<>();
		BigDecimal demand = new BigDecimal("0");
		BigDecimal demandDelta = new BigDecimal("0.01");
		while(demand.compareTo(new BigDecimal("1.0")) <= 0){
			BigDecimal value = new BigDecimal(0.0);
			demandFreqMap.put(demand, value);
			demand = demand.add(demandDelta);
		}
		
		BigDecimal _1_N = new BigDecimal(0.0);
		try{
			_1_N =  new BigDecimal(Double.toString(perFreq));
			
		}catch(NumberFormatException e){
			System.out.println(Double.toString(perFreq));
			e.printStackTrace();
		}
		for(double demandH: history){
			BigDecimal key = new BigDecimal(demandH+"");
			//将每个需求转换为整数
			BigDecimal freq = demandFreqMap.get(key).add(_1_N);
			//如果出现过的需求量
			demandFreqMap.put(key, freq);
		}
		//根据概率统计出最小需求
		BigDecimal sumFreq = new BigDecimal(Double.toString(0.00));
		for(Entry<BigDecimal, BigDecimal> demandFreq : demandFreqMap.entrySet()){
			sumFreq = sumFreq.add(demandFreq.getValue());
			if(sumFreq.compareTo(p) >= 0){
				StatisticDemand = demandFreq.getKey().doubleValue();
				break;
			}	
		}*/
		TreeMap<Double, Integer> demandFreqMap = new TreeMap<>();
		
		for(double demand: history){
			//BigDecimal demandH = new BigDecimal(demand+"");
			if(demandFreqMap.keySet().contains(demand)){
				//将每个需求转换为整数
				//BigDecimal freq = demandFreqMap.get(demandH).add(_1);
				//如果出现过的需求量
				demandFreqMap.put(demand, demandFreqMap.get(demand) + 1);
			}else{
				demandFreqMap.put(demand, 1);
			}
		}
		//根据概率统计出最小需求
		int sum = 0;
		BigDecimal sumFreq;
		for(Entry<Double, Integer> demandFreq : demandFreqMap.entrySet()){
			sum += demandFreq.getValue();
			String num = pointFormat(sum/N);
			sumFreq = new BigDecimal(num);
			if(sumFreq.compareTo(p) >= 0){
				StatisticDemand = demandFreq.getKey();
				break;
			}	
		}
		return StatisticDemand;
	}
	
	/**
	 * 控制double小数点后的位数
	 * @param d
	 * @return
	 */
	public static String pointFormat(double d){
		DecimalFormat df = new DecimalFormat("0.0000"); 
		return df.format(d);
	}
	
	public static double format(double d){
		DecimalFormat df = new DecimalFormat("0.00");
		double ret = Double.parseDouble(df.format(d));
		return ret;
	}
}
