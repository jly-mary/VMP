package entities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import utils.Constants;
import utils.StatisticUtils;
/**
 * 资源管理者，已知上层到达的VM需求信息和下层各个PM资源使用情况
 * @author Administrator
 *
 */
public class Controller {
	public String name;
	private int vmId;
	private LinkedBlockingQueue<VM> vmList;
	private List<PM> pmList;

	public Queue<VM> getVmList() {
		return vmList;
	}
	public void setVmList(LinkedBlockingQueue<VM> vmList) {
		this.vmList = vmList;
	}
	public List<PM> getPmList() {
		return pmList;
	}
	public void setPmList(List<PM> pmList) {
		this.pmList = pmList;
	}
	
	//初始化Controller 已知底层各个PM的资源使用情况
	public Controller(String name, List<PM> pmList){
		this.name = name;
		this.vmId = 0;
		this.pmList = pmList;
		this.vmList = new LinkedBlockingQueue<VM>();
	}
	//加入创建的VM列表
	public void addVMList(List<VM> vmList){
		for(VM vm : vmList){
			vm.setId(++vmId);
			this.getVmList().add(vm);
		}		
	}
	//加入一个VM(未分配资源后排序)
	public void addVM(VM vm){
		vm.setId(++vmId);
		this.getVmList().add(vm);
	}
	
	//Controller 调度
	public ArrayList<BigDecimal> schedule(int curTime){
//		BigDecimal time = new BigDecimal((double)curTime);
		//先进行资源的释放
		for(PM pm: this.getPmList()){
			if(!pm.getVmList().isEmpty()){
				//根据当前时刻 释放其资源
				/*int vmNum =  pm.getVmList().size();
				double cpu = pm.getCpuRest();
				double ram = pm.getRamRest();
				double disk = pm.getDiskRest();
				double bw = pm.getBwRest();*/
				
				pm.release(curTime);
				
				/*for(VM vm :pm.getVmList()){
					BigDecimal vmLeaveTime = new BigDecimal(vm.leaveTime);
					if(vmLeaveTime.compareTo(time) < 0){
						System.out.println("存在未被释放的资源");
					}
				}*/
				/*if(vmNum != pm.getVmList().size()){
					System.out.println(this.name + "释放前：pm"+ pm.getId() +
							" 包含VM："+ vmNum+						
							" cpu剩余："+ cpu +
							" ram剩余："+ ram +
							" disk剩余："+ disk +
							" Bw剩余："+ bw);
					System.out.println(this.name + "释放后：pm"+ pm.getId() +
							" 包含VM："+ pm.getVmList().size()+
							" cpu剩余："+ pm.getCpuRest() +
							" ram剩余："+ pm.getRamRest() +
							" disk剩余："+ pm.getDiskRest() +
							" Bw剩余："+ pm.getBwRest() );
				}*/
			}
		}
		
		//判断VM队列中是否还存在需求
		int allocatedVms = 0;
		//总到达的VM的数量
		int totalVms = this.getVmList().size();
		//资源分配
		while(!this.getVmList().isEmpty()){
			//找到首个满足该VM个维度资源需求的PM
			VM vm = this.getVmList().poll();
			for(PM pmFirst: this.getPmList()){
				if(pmFirst.isSuitable(vm)){
					vm.setPM(pmFirst);
					pmFirst.placeVM(vm);
					allocatedVms++;
					break;
				}
			}
		}
		/*String allocatedRate = StatisticUtils.pointFormat((double)allocatedVms/totalVms);
		BigDecimal AR = new BigDecimal(allocatedRate);
		BigDecimal one = BigDecimal.ONE;*/
		BigDecimal unAllocatedVMs = new BigDecimal((totalVms - allocatedVms));
		//统计本次调度开启的PM数量
		int occupyPms = 0;
		for(PM pm: this.getPmList()){
			if(!pm.getVmList().isEmpty()){
				occupyPms ++;
				/*for(VM vm :pm.getVmList()){
					BigDecimal vmLeaveTime = new BigDecimal(vm.leaveTime);
					if(vmLeaveTime.compareTo(time) < 0){
						System.out.println("存在未被释放的资源");
					}
				}*/
			}
		}
		BigDecimal OR = new BigDecimal(StatisticUtils.pointFormat((double)occupyPms/Constants.PM_NUM));
		
		//返回结果
		ArrayList<BigDecimal> res = new ArrayList<>();
		//VM接受率
//		res.add(AR);
		res.add(unAllocatedVMs);
		//PM占用率
		res.add(OR);
		//VM违背率
		//res.add(one.subtract(AR));
		
		return res;
	}

	/**
	 * 通知各个PM记录其承载的VM的资源占用
	 */
	public void notifyPmRecord(int time) {
//		System.out.println(TIME + "记录");
		for(PM pm: this.getPmList()){
			if(!pm.getVmList().isEmpty()){
//				System.out.println("PM" + pm.getId() + "记录");
				pm.record(time);
			}
		}
		
	}
	/**
	 * 通知各个PM更新其资源剩余
	 */
	public void notifyPmUpdate(int N) {
		for(PM pm: this.getPmList()){
			if(!pm.getVmList().isEmpty()){
				pm.updateVmOccupy(N);
			}
		}	
	}
	/**
	 * 统计各个已经分配资源的VM在当前时刻是否
	 * @param voilateVMs
	 * @param time
	 * @return
	 */
	public ArrayList<Integer> judgeVMs(int curTime) {
		ArrayList<Integer> res = new ArrayList<Integer>();
		//已分配资源的VM的总数量
		int existTotalVMs = 0;
		//违反需求的VM的数量
		int voilatedVMs = 0;
		
		for(PM pm: this.getPmList()){
//			System.out.println("PM-" + pm.getId() + " 包含VM数量：" +pm.getVmList().size());
			if(pm.getVmList().size() != 0){
				for(VM vm : pm.getVmList()){
					existTotalVMs ++;
					vm.judge(curTime);
					if(vm.voilate == true){
						voilatedVMs ++;	
					}
				}
			}	
		}
		
		//return (double)voilatedVMs/existTotalVMs;
		res.add(voilatedVMs);
		res.add(existTotalVMs);
		return res;
	}

	
}
