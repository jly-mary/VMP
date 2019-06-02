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
public class DataCenter3 extends DataCenter1{
	//预测时间间隔
	public int pred_N;
	
	//结果
//	public  ArrayList<Double> R_C_Real = new ArrayList<>();
	public  ArrayList<Double> R_C_PRED = new ArrayList<>();

	//全局时间轴
	public  volatile int TIME = 0;
	private  final Object lock = new Object();
	
	//构造方法
	public DataCenter3(String resultPath, double probability, int static_N, int pred_N, String workload){
		super(resultPath, probability, static_N);
		this.pred_N = pred_N;
		this.workload = workload;
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
//		Controller1 controllerReal = new Controller1("Real", DataCenterUtils.createPMs(Constants.PM_NUM));
		Controller1 controllerPred = new Controller1("Pred", DataCenterUtils.createPMs(Constants.PM_NUM));
		
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
								 if(controllerPred.getVmList().size() == 0){
									System.out.println(Thread.currentThread().getName() + "--创建VM--TIME:" + TIME);
									//创建指定时刻下的VM，按照时间到达的顺序进行分配资源
									List<VM> vmListMain= DataCenterUtils.createVMs(TIME, workload, probability, static_N);
									List<VM> vmListVice;
//									List<VM> vmListReal;
									List<VM> vmListPred;
									try {
										vmListVice = DataCenterUtils.deepCopy(vmListMain);
//										vmListReal = DataCenterUtils.deepCopy(vmListMain);
										vmListPred = DataCenterUtils.deepCopy(vmListMain);
										//将创建的vm需求加入队列
										controllerMain.addVMList(vmListMain);
										controllerVice.addVMList(vmListVice);
//										controllerReal.addVMList(vmListReal);
										controllerPred.addVMList(vmListPred);
										
										System.out.println("创建VM数量：" + controllerPred.getVmList().size());
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
								if(controllerPred.getVmList().size() >= 1){
									
									if(TIME >= static_N * Constants.interval){
										//按统计值更新当前生存的所有VM的分配资源量
										controllerMain.notifyPmUpdate(static_N, probability);
										//按下一时刻实际值分配
										//controllerReal.notifyPmUpdateWithReal(static_N, TIME);
									}
									if(TIME >= pred_N * Constants.interval){
										//按下一时刻预测值分配
										controllerPred.notifyPmUpdateWithPred(pred_N);
									}
									
									TIME += 5;
									System.out.println(Thread.currentThread().getName()+ "--调度--TIME:" + TIME);
									DecimalFormat df = new DecimalFormat("0.0000");
									
									/** main schedule 每5分钟调度一次 */
									//1. main schedule 按最高需求分配资源
									controllerMain.schedule(TIME);
									//2. 分配结束 ， 统计该时刻下违背需求的数量
									BigDecimal r_c_main = controllerMain.judge(TIME);
									//3. 使用一定概率分配资源后， 记录违背率
									R_C_STATISTIC.add(Double.parseDouble(df.format(r_c_main.doubleValue())));
									System.out.println("Statiistic:R/C: " + R_C_STATISTIC.get(R_C_STATISTIC.size() - 1));
									//4. 调度之后记录，从5分钟开始 controller通知各个PM记录他们前一时刻所承载的所有VM的动态需求
									controllerMain.notifyPmRecord(TIME);
									
									/** vice schedule 每5分钟调度一次 */	
									//1. vice schedule 按最高需求分配资源
									controllerVice.schedule(TIME);
									//2. 不使用一定概率分配资源, 违背率
									BigDecimal r_c_vice = controllerVice.judge(TIME);
									R_C_OVER.add(Double.parseDouble(df.format(r_c_vice.doubleValue())));
									System.out.println("Over:R/C: " + R_C_OVER.get(R_C_OVER.size() - 1));
									
									/** real schedule 每5分钟调度一次 *//*	
									//1. REAL schedule 按最高需求分配资源
									controllerReal.schedule(TIME);
									//2. 不使用一定概率分配资源, 违背率
									BigDecimal r_c_real = controllerReal.judge(TIME);
									R_C_Real.add(Double.parseDouble(df.format(r_c_real.doubleValue())));
									System.out.println("Real:R/C: " + R_C_Real.get(R_C_Real.size() - 1));
									*/
									
									/** pred schedule 每5分钟调度一次 */	
									//1. pred schedule 按最高需求分配资源
									controllerPred.schedule(TIME);
									//2. 分配结束 ， 统计该时刻下违背需求的数量
									BigDecimal r_c_pred = controllerPred.judge(TIME);
									//3. 使用一定概率分配资源后， 记录违背率
									R_C_PRED.add(Double.parseDouble(df.format(r_c_pred.doubleValue())));
									System.out.println("Pred:R/C: " + R_C_PRED.get(R_C_PRED.size() - 1));
									//4. 调度之后记录，从5分钟开始 controller通知各个PM记录他们前一时刻所承载的所有VM的动态需求
									controllerPred.notifyPmRecord(TIME);
									
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
		String[] Title = {"Time","StatisticR_C","OverR_C","PredR_C"};
		try {
			XlsUtils.createExcel(getResultPath(), "R_C", Title, R_C_STATISTIC, R_C_OVER, R_C_PRED);
		} catch (WriteException | IOException e) {
			e.printStackTrace();
		}
	}

	
}


