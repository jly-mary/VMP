package main;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import entities.Controller;
import entities.VM;
import jxl.write.WriteException;
import utils.Constants;
import utils.DataCenterUtils;
import utils.StatisticUtils;
import utils.XlsUtils;

public class DataCenter {
	//保存的路径
	private String resultPath;
	//违反的概率
	public double probability;
	//统计的时间间隔
	public int N;
	//全局时间轴
	public  ArrayList<Double> MAIN_VOILATE_RATE = new ArrayList<>();//统计违反需求的VM比例
	//public  ArrayList<Double> MAIN_ALLOCATE_RATE = new ArrayList<>();//统计分配的VM比例
	public  ArrayList<Double> MAIN_OCCUPY_RATE = new ArrayList<>();//统计占用PM的比例
	
	public  ArrayList<Double> VICE_VOILATE_RATE = new ArrayList<>();//统计违反需求的VM比例
	//public  ArrayList<Double> VICE_ALLOCATE_RATE= new ArrayList<>();//统计分配的VM比例
	public  ArrayList<Double> VICE_OCCUPY_RATE = new ArrayList<>();//统计占用PM的比例

	public  volatile int TIME = 0;
	private  final Object lock = new Object();
	//构造方法
	public DataCenter(String resultPath){
		this.setResultPath(resultPath);
	}
	public DataCenter(String resultPath, double probability){
		this.setResultPath(resultPath);
		this.probability = probability;
	}
	public DataCenter(String resultPath, double probability, int N){
		this.setResultPath(resultPath);
		this.probability = probability;
		this.N = N;
	}
	public String getResultPath() {
		return resultPath;
	}

	public void setResultPath(String resultPath) {
		this.resultPath = resultPath;
	}
	
	@Test
	public void test(){
		String wokloadPath = "D:\\ProgramFiles\\java\\workspace\\VMP\\workload\\planetlab\\";
		File workloadFiles = new File(wokloadPath + "20110309");
		System.out.println(workloadFiles.listFiles().length);
		/*Calendar time = Calendar.getInstance();
		System.out.println(time.getTimeInMillis());
		time.add(Calendar.MINUTE, (int) (Constants.interval/60));
		System.out.println(time.getTimeInMillis());*/
	}
    //数据中心工作，分两个线程，一个用于创建需求，一个用于需求的调度
	public void work(){
		//设置阀门，锁住子线程
		CountDownLatch latch = new CountDownLatch(2);
		//选择的负载文件
		String workload = "20110309";
		
		//创建控制者
		Controller controllerMain = new Controller("Main", DataCenterUtils.createPMs(Constants.PM_NUM));
		Controller controllerVice = new Controller("Vice", DataCenterUtils.createPMs(Constants.PM_NUM));
		
		/** 创建线程生成VM需求 */
		Thread createVMsThread = new Thread(
				//实现Runnable 生成VM需求
				new Runnable(){
					@Override
					public  void run() {
						while(true){
							 synchronized (lock){
								//大于最长调度时间则停止调度
								if(TIME >=  Constants.MAX_TIME){
									latch.countDown();
									lock.notify();
									return;
								}
								 //如果没有请求
								 if(controllerMain.getVmList().size() == 0){
									System.out.println(Thread.currentThread().getName() + "--创建VM--TIME:" + TIME);
									//创建指定时刻下的VM，按照时间到达的顺序进行分配资源
									List<VM> vmListMain = DataCenterUtils.createVMs(TIME, workload, probability, N);
									
									List<VM> vmListVice;
									try {
										vmListVice = DataCenterUtils.deepCopy(vmListMain);
										/*System.out.println("main:" + vmListMain.get(0).getId());
										vmListVice.get(0).setId(2000);
										System.out.println("main:" + vmListMain.get(0).getId());*/
										
										//将创建的vm需求加入队列
										controllerMain.addVMList(vmListMain);
										controllerVice.addVMList(vmListVice);
										System.out.println("创建VM数量：" + controllerMain.getVmList().size());
									} catch (ClassNotFoundException e) {
										e.printStackTrace();
									} catch (IOException e) {
										e.printStackTrace();
									}
									
								 }else{
								 //如果有请求
								 // 创建需求通知调度
							         lock.notify();
								 }
								
							 }
						}
					}
				}, "CreateVMs");	
	
		/** 主线程  调度 */
		Thread scheduleThread = new Thread(
				//实现Runnable 生成VM需求
				new Runnable(){
					@Override
					public void run() {
						while(true){
 							synchronized (lock){
								//1. 如果有需求，则ScheduleThread调度
								if(controllerMain.getVmList().size() >= 1){
									if(TIME >= N * Constants.interval){
										//** 在足够的时间间隔下，通知各个PM根据历史数据更新每个VM的资源占用 
										//   并更新其资源剩余 
										//System.out.println("TIME: " + TIME);
										System.out.println("-----开始统计更新占用 TIME:" + TIME);
										controllerMain.notifyPmUpdate(N);
									}
									
									TIME += 5;
									
									//每5分钟调度一次
									System.out.println(Thread.currentThread().getName()+ "--调度--TIME:" + TIME);
									ArrayList<BigDecimal>  MAIN_RESULTS = controllerMain.schedule(TIME);
									
									//MAIN_ALLOCATE_RATE.add(MAIN_RESULTS.get(0).intValue());
									MAIN_OCCUPY_RATE.add(MAIN_RESULTS.get(1).doubleValue());
									
									//统计违背需求的数量
									ArrayList<Integer> voilate_exist = controllerMain.judgeVMs(TIME);
									//使用一定概率分配资源后， 记录违背率
									DecimalFormat df = new DecimalFormat("0.0000");
									double voilateRate = (MAIN_RESULTS.get(0).intValue() + voilate_exist.get(0))/
											(double)voilate_exist.get(1);
									if(TIME < N * Constants.interval){
										//在不使用一定概率分配资源前违背率为0
										MAIN_VOILATE_RATE.add(0.0);
									}else{
										MAIN_VOILATE_RATE.add(Double.parseDouble(df.format(voilateRate)));
									}
									
									//System.out.println("Main:VM接收率: " + MAIN_ALLOCATE_RATE.get(MAIN_ALLOCATE_RATE.size() - 1));
									System.out.println("Main:PM占用率: " + MAIN_OCCUPY_RATE.get(MAIN_OCCUPY_RATE.size() - 1));
									System.out.println("Main:VM违背率: " + MAIN_VOILATE_RATE.get(MAIN_VOILATE_RATE.size() - 1));
									
									//调度之后记录，从5分钟开始 controller通知各个PM记录他们前一时刻所承载的所有VM的动态需求
									controllerMain.notifyPmRecord(TIME);
									
									ArrayList<BigDecimal>  VICE_RESULTS  = controllerVice.schedule(TIME);
									//VICE_ALLOCATE_RATE.add(VICE_RESULTS.get(0).doubleValue());
									VICE_OCCUPY_RATE.add(VICE_RESULTS.get(1).doubleValue());
									/*if(!MAIN_RESULTS.get(0).equals(VICE_RESULTS.get(0))){
										System.out.println("ERROR");
									}*/
									//不使用一定概率分配资源, 违背率为0
									ArrayList<Integer> voilate_exist_vice = controllerVice.judgeVMs(TIME);
									//使用一定概率分配资源后， 记录违背率
									double voilateRateVice = (VICE_RESULTS.get(0).intValue())/
											(double)voilate_exist_vice.get(1);
									VICE_VOILATE_RATE.add(Double.parseDouble(df.format(voilateRateVice)));
									
									//VICE_VOILATE_RATE.add(0.0);
									
									//System.out.println("Vice:VM接收率: " + VICE_ALLOCATE_RATE.get(VICE_ALLOCATE_RATE.size() - 1));
									System.out.println("Vice:PM占用率: " + VICE_OCCUPY_RATE.get(VICE_OCCUPY_RATE.size() - 1));
									System.out.println("Vice:VM违背率: " + VICE_VOILATE_RATE.get(VICE_VOILATE_RATE.size() - 1));
								}else{	
								//1. 如果当前没有需求，则ScheduleThread阻塞
									try {
										lock.wait();
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
								//2. 大于最长调度时间则停止调度
								if(TIME >= Constants.MAX_TIME){
									System.out.println("调度完成！" + TIME);
									latch.countDown();
									return;
								}
								//3. 调度完成
								lock.notify();
							}
						}
					}
				}, "ScheduleVMs");	
		
		//启动创建VM的线程
		createVMsThread.start();
		//启动调度线程
		scheduleThread.start();
		
		try {
			//主线程等待两个子线程执行完毕后再保存结果
			latch.await();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		//1.保存占用PM比例的结果
		String[] Title1 = {"Time","MainOccupyRate","ViceOccupyRate"};
		try {
			XlsUtils.createExcel(getResultPath(), "OCCUPY_PM_RATE", Title1, MAIN_OCCUPY_RATE, VICE_OCCUPY_RATE );
		} catch (WriteException | IOException e) {
			e.printStackTrace();
		}
		//2.保存分配VM的结果
		/*String[] Title2 = {"Time","MainAllocatedRate", "ViceAllocatedRate"};
		try {
			XlsUtils.createExcel(getResultPath(), "ALLOCATE_VM_RATE", Title2, MAIN_ALLOCATE_RATE, VICE_ALLOCATE_RATE);
		} catch (WriteException | IOException e) {
			e.printStackTrace();
		}*/
		//3. 违反需求的VMs
		String[] Title3 = {"Time","MainVoilateRate","ViceVoilateRate"};
		try {
			XlsUtils.createExcel(getResultPath(), "VOILATE_VM_RATE", Title3, MAIN_VOILATE_RATE, VICE_VOILATE_RATE);
		} catch (WriteException | IOException e) {
			e.printStackTrace();
		}
	}

	
}


