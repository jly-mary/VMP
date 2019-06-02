package utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jfree.data.category.DefaultCategoryDataset;
import org.junit.Test;

import entities.PM;
import entities.VM;

/**
 * 工具类
 * @author Administrator
 *
 */
public class DataCenterUtils {
	@Test
	public void testExpDistribution(){
		double x;
		double sum = 0.0;
		for(int i=0; i<20; i++){
			double z = Math.random();
//			System.out.println(z);
			x =  -(Math.log(z)) / Constants.lamda;
//			System.out.println((int)Math.ceil(x));
			sum += x;
		}
		System.out.println(sum);
		//return (int) x;
	}
	@Test 
	public void testListCopy(){
		List<Integer> srcDemands = new ArrayList<>();
		List<Integer> destDemands = new ArrayList<>();
		for(int i=0; i<15; i++){
			srcDemands.add(i);
		}
		int start = 5;
		destDemands = srcDemands.subList(start, srcDemands.size());
		System.out.println("destDemands:" + destDemands + " size: "+destDemands.size());
	}
	/**
	 * 实现数组的深复制
	 * @param src
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static List<VM> deepCopy(List<VM> src) throws IOException, ClassNotFoundException {  
	    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();  
	    ObjectOutputStream out = new ObjectOutputStream(byteOut);  
	    out.writeObject(src);  

	    ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());  
	    ObjectInputStream in = new ObjectInputStream(byteIn);  
	    @SuppressWarnings("unchecked")  
	    List<VM> dest = (List<VM>) in.readObject();  
	    return dest;  
	}
	/**
	 * 产生指数分布随机数
	 * @return
	 */
	public static double expDistribution(){
			double z = Math.random();
			double x =  -(Math.log(z) / Constants.lamda);
 		    return x;
	}
	 /** 产生指数分布随机数
	  * @return
	  */
	public static double expDistribution(double lambda){
			double z = Math.random();
			double x =  -(Math.log(z) / lambda);
 		    return x;
	}
	/**
	 * 创建给定数量的PM
	 * @param PMNums
	 * @return
	 */
	public static ArrayList<PM> createPMs(int PMNums){
		ArrayList<PM> pmList = new ArrayList<>();
		for(int id=0; id<PMNums; id++){
			PM pm = new PM(id, (double)1.0, (double)1.0, (double)1.0, (double)1.0);
			pmList.add(pm);
		}
		return pmList;
	}
	
	/**
	 * 获取VM资源需求的最大值
	 * @param cpuDemand
	 * @return
	 */
	public static double getMaxDemand(List<Double> demand) {
		return Collections.max(demand);
		/*List<Double> vmDemands = new ArrayList<>();
		vmDemands.addAll(demand);
		//从大->小排序
		vmDemands.sort(new Comparator<Double>(){
			@Override
            public int compare(Double d1, Double d2) {
				BigDecimal o1 = new BigDecimal(d1+"");
				BigDecimal o2 = new BigDecimal(d2+"");
               if (o2.compareTo(o1) > 0){
            	   return 1;
               }else if(o2.compareTo(o1) < 0){
            	   return -1;
               }else{
            	   return 0;
               }
            }
		});
		
		if(!vmDemands.get(0).equals(max)){
			System.out.println("false");
		}
		return vmDemands.get(0);*/
	}
	
	/**
	 * 获取在给定时间之后的最大需求
	 * @param demand
	 * @param arrivalTime
	 * @return
	 */
	public static Double getMaxDemand(List<Double> demand, double arrivalTime) {
		List<Double> vmDemands = new ArrayList<>();
		vmDemands.addAll(demand.subList((int) (arrivalTime/Constants.interval), demand.size())); 
		//从大->小排序
		vmDemands.sort(new Comparator<Double>() {
			@Override
            public int compare(Double d1, Double d2) {
               if (d2.equals(d1)){
            	   return 0;
               }else if((d2 - d1) > 0){
            	   return 1;
               }else{
            	   return -1;
               }
            }
        });
		
		return vmDemands.get(0);
	}
	
	/**
	 * 创建VM集合，服从泊松分布的VM到达
	 * @param curTime
	 * @param workload
	 * @param probability
	 * @return
	 */
	public static List<VM> createVMs(int curTime, String workload, double probability, int N){
		double arrivalTime = curTime; 
		int allocateTime = curTime + 5;
		
		List<VM> vmList = new ArrayList<>();
		while(true){
			//指数分布时间间隔
			double randomDelta = DataCenterUtils.expDistribution();
			//每次产生 Constants.interval内的需求
			if(arrivalTime + randomDelta >= (double)(curTime + Constants.interval)){
				break;
			}else{
				double leaveMoment = 0.0;
				//VM请求到达时刻
				arrivalTime += randomDelta; //double
				//在负载文件
				String path = "D:\\ProgramFiles\\java\\workspace\\VMP\\workload\\planetlab\\";
				File workloadPath = new File(path + workload);
				
				/** 每个文件代表一个VM负载，生存时隙个数均为288*/
				File[] files = workloadPath.listFiles();
				int filesLen = 288;
				//生存时长  >= N
				//int lifeNum = (int)(Math.random()*(filesLen - N + 1) + N);
				//生存时间服从指数分布
				int lifeNum = (int)Math.ceil(expDistribution(1.0/N));
				if(lifeNum >= filesLen){
					lifeNum = 288;
				}
				/*System.out.println("lifeNum: " + lifeNum);
				if(lifeNum == 0){
					System.out.println("生存时间为0");
				}*/
				leaveMoment = arrivalTime + lifeNum*Constants.interval;
				
				//四种需求的动态负载
				ArrayList<ArrayList<Double>> demand4 = new ArrayList<ArrayList<Double>>(4);
				int n = allocateTime/5;
				
				for(int i=0; i<4; i++){
					//负载文件中随机选一个
					int num = (int) (filesLen*Math.random());
					File selectedWorkload = files[num];
					demand4.add(new ArrayList<Double>());
					try{				
						BufferedReader input = new BufferedReader(new FileReader(selectedWorkload.getAbsolutePath()));
						int lineNum = 0;
						int cnt = 0;
						while(lineNum < filesLen){
							String demand = input.readLine();
							lineNum ++;
							//从指定行开始记录需求
							if(lineNum >= n){
								demand4.get(i).add(Integer.valueOf(demand) / 100.0);
								cnt ++;
								if(cnt == lifeNum){
									break;
								}
							}
				
						}
						input.close();
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				VM vm = new VM(arrivalTime,
								leaveMoment,
								demand4.get(0), 
								demand4.get(1),
								demand4.get(2),
								demand4.get(3),
								probability);
				vmList.add(vm);
			}			
		}		
		return vmList;
	}
	
	public static List<VM> createVMs(int curTime, String workload, int N){
		double arrivalTime = curTime; 
		int allocateTime = curTime + 5;
		
		List<VM> vmList = new ArrayList<>();
		while(true){
			//指数分布时间间隔
			double randomDelta = DataCenterUtils.expDistribution();
			//每次产生 Constants.interval内的需求
			if(arrivalTime + randomDelta >= (double)(curTime + Constants.interval)){
				break;
			}else{
				double leaveMoment = 0.0;
				//VM请求到达时刻
				arrivalTime += randomDelta; //double
				//在负载文件
				String path = "D:\\ProgramFiles\\java\\workspace\\VMP\\workload\\planetlab\\";
				File workloadPath = new File(path + workload);
				
				/** 每个文件代表一个VM负载，生存时隙个数均为288*/
				File[] files = workloadPath.listFiles();
				int len = 288;
				//生存时长  均匀分布
				//int lifeNum = (int)(Math.random()*(filesLen - N + 1) + N);
				//生存时间服从指数分布
				int lifeNum = (int)Math.ceil(expDistribution(1.0/N));
				if(lifeNum >= len){
					lifeNum = 288;
				}
				/*System.out.println("lifeNum: " + lifeNum);
				if(lifeNum == 0){
					System.out.println("生存时间为0");
				}*/
				leaveMoment = arrivalTime + lifeNum*Constants.interval;
				
				//四种需求的动态负载
				ArrayList<ArrayList<Double>> demand4 = new ArrayList<ArrayList<Double>>(4);
				int n = allocateTime/5;
				
				for(int i=0; i<4; i++){
					//负载文件中随机选一个
					int num = (int) (len*Math.random());
					File selectedWorkload = files[num];
					demand4.add(new ArrayList<Double>());
					try{				
						BufferedReader input = new BufferedReader(new FileReader(selectedWorkload.getAbsolutePath()));
						int lineNum = 0;
						int cnt = 0;
						while(lineNum < len){
							String demand = input.readLine();
							lineNum ++;
							//从指定行开始记录需求
							if(lineNum >= n){
								demand4.get(i).add(Integer.valueOf(demand) / 100.0);
								cnt ++;
								if(cnt == lifeNum){
									break;
								}
							}
				
						}
						input.close();
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				VM vm = new VM(arrivalTime,
								leaveMoment,
								demand4.get(0), 
								demand4.get(1),
								demand4.get(2),
								demand4.get(3)
								);
				vmList.add(vm);
			}			
		}		
		return vmList;
	}
	
	@Test
	public static VM createVM(){
		double leaveTime = 0.0;
		//在负载文件中随机选一个
		int arrivalTime = 0;
		String workload = "20110309";
		String path = "D:\\ProgramFiles\\java\\workspace\\VMP\\workload\\planetlab\\";
		File workloadPath = new File(path + workload);
		
		/** 每个文件代表一个VM负载，生存时隙个数均为288*/
		File[] files = workloadPath.listFiles();
		int workloadLen = files.length;
		ArrayList<ArrayList<Double>> demand4 = new ArrayList<ArrayList<Double>>();
		//用选择的负载创建一个具有动态资源需求的虚拟机
		for(int i=0; i<4; i++){
			int num = (int) (workloadLen*Math.random());
			File selectedWorkload = files[num];
			demand4.add(new ArrayList<Double>());
			ArrayList<Double> di = demand4.get(i);
			try{				
				BufferedReader input = new BufferedReader(new FileReader(selectedWorkload.getAbsolutePath()));
				for(int n=0; n<288; n++){
					int aa = Integer.valueOf(input.readLine());
					di.add(aa / 100.0);
				}
				input.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		VM vm = new VM(arrivalTime, leaveTime,  demand4.get(0), demand4.get(1), 
				demand4.get(2), demand4.get(3), 0.01);
		return vm;
	}
	public static ArrayList<ArrayList<VM>> deepCopy(ArrayList<ArrayList<VM>> src) throws IOException, ClassNotFoundException {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();  
	    ObjectOutputStream out = new ObjectOutputStream(byteOut);  
	    out.writeObject(src);  

	    ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());  
	    ObjectInputStream in = new ObjectInputStream(byteIn);  
	    @SuppressWarnings("unchecked")  
	    ArrayList<ArrayList<VM>> dest = (ArrayList<ArrayList<VM>>) in.readObject();  
	    return dest; 
	}
}