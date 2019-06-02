package main;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.jfree.data.xy.XYDataset;

import entities.VM;
import utils.DataCenterUtils;
import utils.DrawUtils;

public class MultiProbability {
	/**
	 * 不同违反概率下的结果
	 * @param args
	 */
	public static void main(String[] args) {
		String filePath = "D:\\ProgramFiles\\java\\workspace\\VMP\\results\\multiProbability\\";
		String workload = "20110322";
		File dir = new File(filePath);
		if(dir.listFiles().length == 0){
			//如果没有结果
			BigDecimal p = new BigDecimal("0.01");
			int N = 100;
			//probability 0.01 - 0.1
			//同一批需求
			ArrayList<ArrayList<VM>> VMs = new  ArrayList<ArrayList<VM>>(288);
			for(int TIME=0; TIME<1440; TIME += 5){
				VMs.add(new ArrayList<VM>());
				VMs.get(TIME/5).addAll(DataCenterUtils.createVMs(TIME, workload, N));
			}
			while(p.compareTo(new BigDecimal("0.5")) <= 0){
				System.out.println("违背概率p：" + p);
				/**
				 * 不同概率下调度同一批需求
				 */
				ArrayList<ArrayList<VM>> VMsCopy;
				try {
					VMsCopy = DataCenterUtils.deepCopy(VMs);
					double probability = p.doubleValue();
					//创建DC
					DataCenter2 dc = new DataCenter2(filePath,
													probability,
													N,
													VMsCopy);
					dc.work();
					// m
					p = p.add(new BigDecimal("0.07"));
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		}
		//如果有结果，将每个概率下的结果计算平均值
		if(dir.listFiles().length !=0){
			XYDataset dataset = DrawUtils.createAvergaeDataset(dir);
			String yLabel = "AVERAGE_R_C";
			DrawUtils.draw(dataset, filePath, yLabel+"_Probability", "probability", yLabel);
		}
	}
}
