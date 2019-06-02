package main;

import java.io.File;

import org.jfree.data.xy.XYDataset;

import utils.DrawUtils;

public class DataCenterWork {
	public static void main(String[] args) {
		String filePath = "D:\\ProgramFiles\\java\\workspace\\VMP\\results\\Real_Stat_Vice_Pred\\";
		String workload = "20110322";
		File dir = new File(filePath);
		dir.mkdir();
		if(dir.listFiles().length == 0){
			double probability = 0.01;
			int static_N = 50;
			int pred_N = 50;
			//创建DC
			DataCenter4 dc = new DataCenter4(filePath, 
										   probability, 
										   static_N,
										   pred_N,
										   workload);
			dc.work();
		}
		//如果有结果，将每个概率下的结果计算平均值
		for(File result: dir.listFiles()){
			XYDataset dataset = DrawUtils.createDatasetWithMultiRes(result, "Real", "Statistic", "Over", "Pred");
			String yLabel = result.getName().replace(".xls", "");
			DrawUtils.draw(dataset, filePath, yLabel, "time/(mins)", "R/C");
		}
	}
}
