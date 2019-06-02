package main;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import entities.Controller1;
import entities.PM;
import entities.VM;
import jxl.write.WriteException;
import utils.Constants;
import utils.DataCenterUtils;
import utils.StatisticUtils;
import utils.XlsUtils;

public class DataCenter2 {
	//保存的路径
	public String resultPath;
	//违反的概率
	public double probability;
	//统计的时间间隔
	public int static_N;
	
	public  ArrayList<ArrayList<VM>> VMs;
	public  ArrayList<Double> R_C_Main = new ArrayList<>();
	public  ArrayList<Double> R_C_VICE = new ArrayList<>();

	//全局时间轴
	public  volatile int TIME = 0;
	private  final Object lock = new Object();
	//构造方法
	public DataCenter2(String resultPath){
		this.setResultPath(resultPath);
	}
	public DataCenter2(String resultPath, double probability){
		this.setResultPath(resultPath);
		this.probability = probability;
	}
	public DataCenter2(String resultPath, double probability, int N, ArrayList<ArrayList<VM>> VMs){
		this.setResultPath(resultPath);
		this.probability = probability;
		this.static_N = N;
		this.VMs = VMs;
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
		
		
		//创建控制者
		Controller1 controllerMain = new Controller1("Main", DataCenterUtils.createPMs(Constants.PM_NUM));
		Controller1 controllerVice = new Controller1("Vice", DataCenterUtils.createPMs(Constants.PM_NUM));
		
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
									List<VM> vmListMain = VMs.get(TIME/5);
									List<VM> vmListVice;
									try {
										vmListVice = DataCenterUtils.deepCopy(vmListMain);
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
									if(TIME >= static_N * Constants.interval){
										//** 在足够的时间间隔下，通知各个PM根据历史数据更新每个VM的资源占用 
										//   并更新其资源剩余 
										controllerMain.notifyPmUpdate(static_N, probability);
									}
									
									DecimalFormat df = new DecimalFormat("0.0000");
									TIME += 5;
									//每5分钟调度一次
									System.out.println(Thread.currentThread().getName()+ "--调度--TIME:" + TIME);
									//1. main schedule
									controllerMain.schedule(TIME);
									//2. 统计违背需求的数量
									BigDecimal r_c_main = controllerMain.judge(TIME);
									//3. 使用一定概率分配资源后， 记录违背率
									R_C_Main.add(Double.parseDouble(df.format(r_c_main.doubleValue())));
									System.out.println("Main:R/C: " + R_C_Main.get(R_C_Main.size() - 1));
									//4. 调度之后记录，从5分钟开始 controller通知各个PM记录他们前一时刻所承载的所有VM的动态需求
									controllerMain.notifyPmRecord(TIME);

									//1. vice schedule
									controllerVice.schedule(TIME);
									//2. 不使用一定概率分配资源, 违背率
									BigDecimal r_c_vice = controllerVice.judge(TIME);
									R_C_VICE.add(Double.parseDouble(df.format(r_c_vice.doubleValue())));
									System.out.println("Vice:R/C: " + R_C_VICE.get(R_C_VICE.size() - 1));
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
		String[] Title1 = {"Time","MainR_C","ViceR_C"};
		try {
			XlsUtils.createExcel(getResultPath(), "pred_over", Title1, R_C_Main, R_C_VICE );
		} catch (WriteException | IOException e) {
			e.printStackTrace();
		}
	}

	
}


