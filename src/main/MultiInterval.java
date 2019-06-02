package main;

import java.io.File;
import java.math.BigDecimal;

import org.jfree.data.xy.XYDataset;

import utils.DrawUtils;

public class MultiInterval {
	/**
	 * 不同统计长度下的性能
	 * @param args
	 */
	public static void main(String[] args) {
		String filePath = "D:\\ProgramFiles\\java\\workspace\\VMP\\results\\P_N\\";
		File dir = new File(filePath);
		if(dir.listFiles().length == 0){
			//如果没有结果
			BigDecimal p = new BigDecimal(0.01d);
			//probability 0.01 - 0.1
			while(p.compareTo(new BigDecimal(0.1d)) <= 0){
				double probability = p.doubleValue();
				int N = 50;
				//N 50 - 120
				while(N <= 120){
					//创建DC
					DataCenter dc = new DataCenter(filePath+"\\p_"+probability+"N"+N, 
												   probability, 
												   N);
					dc.work();
					N += 10;
				}
				//System.out.println(p.doubleValue());
				p = p.add(new BigDecimal(0.01d));
			}
		}
		//如果有结果，将每个概率下的结果计算平均值
		String[] finalResults = {"OCCUPY_PMS_RATE.xls", "VOILATE_VMS_RATE.xls"};
		for(String str: finalResults){
			XYDataset dataset = DrawUtils.getDataset(dir, str);
			String yLabel = str.replace(".xls", "");
			DrawUtils.draw(dataset, filePath, yLabel, "Voilate Probability", "AVERAGE_"+yLabel);
		}
	}
}
