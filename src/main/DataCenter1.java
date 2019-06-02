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
/**
 * Datacenter1 虚拟机的生存时间服从N的指数分布
 * @author Administrator
 *
 */
public class DataCenter1 {
	//保存的路径
	public String resultPath;
	//违反的概率
	public double probability;
	//统计的时间间隔
	public int static_N;
	//选择的负载文件
	public String workload = "20110309";
	
	public  ArrayList<Double> R_C_STATISTIC = new ArrayList<>();
	public  ArrayList<Double> R_C_OVER = new ArrayList<>();

	//全局时间轴
	public  volatile int TIME = 0;
	private  final Object lock = new Object();
	//构造方法
	public DataCenter1(String resultPath){
		this.setResultPath(resultPath);
	}
	public DataCenter1(String resultPath, double probability){
		this.setResultPath(resultPath);
		this.probability = probability;
	}
	public DataCenter1(String resultPath, double probability, int static_N){
		this.setResultPath(resultPath);
		this.probability = probability;
		this.static_N = static_N;
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
									List<VM> vmListMain = DataCenterUtils.createVMs(TIME, workload, probability, static_N);
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
									
									
									
									if(TIME > static_N * Constants.interval){
										//** 在足够的时间间隔下，通知各个PM根据历史数据更新每个VM的资源占用 
										//   并更新PM资源剩余 
										controllerMain.notifyPmUpdate(static_N, probability);//按统计值更新当前生存的所有VM的分配资源量
									}
//									controllerMain.notifyPmUpdateWithReal(N, TIME);
									
									TIME += 5;
									System.out.println(Thread.currentThread().getName()+ "--调度--TIME:" + TIME);
									DecimalFormat df = new DecimalFormat("0.0000");
									
									if(TIME <= static_N * Constants.interval){
										//在无法进行预测时
										
										controllerMain.notifyPmUpdate(static_N, probability);//按统计值更新当前生存的所有VM的分配资源量
									}
									//每5分钟调度一次
									//1. main schedule
//									controllerMain.schedule(TIME);
									controllerMain.scheduleMain(TIME);//分配时按该时刻实际值分配
									//2. 分配结束 ， 统计该时刻下违背需求的数量
									BigDecimal r_c_main = controllerMain.judge(TIME);
									
									//3. 使用一定概率分配资源后， 记录违背率
									R_C_STATISTIC.add(Double.parseDouble(df.format(r_c_main.doubleValue())));
									System.out.println("Main:R/C: " + R_C_STATISTIC.get(R_C_STATISTIC.size() - 1));
									//4. 调度之后记录，从5分钟开始 controller通知各个PM记录他们前一时刻所承载的所有VM的动态需求
									controllerMain.notifyPmRecord(TIME);

									//1. vice schedule
									controllerVice.schedule(TIME);
									//2. 不使用一定概率分配资源, 违背率
									BigDecimal r_c_vice = controllerVice.judge(TIME);
									R_C_OVER.add(Double.parseDouble(df.format(r_c_vice.doubleValue())));
									System.out.println("Vice:R/C: " + R_C_OVER.get(R_C_OVER.size() - 1));
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
			XlsUtils.createExcel(getResultPath(), "real_over", Title1, R_C_STATISTIC, R_C_OVER );
		} catch (WriteException | IOException e) {
			e.printStackTrace();
		}
	}

	
}


