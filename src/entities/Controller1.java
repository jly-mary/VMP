package entities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
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
public class Controller1 {
	public String name;
	private int vmId;
	private List<VM> vmList;
	private List<PM> pmList;

	public List<VM> getVmList() {
		return vmList;
	}
	public void setVmList(ArrayList<VM> vmList) {
		this.vmList = vmList;
	}
	public List<PM> getPmList() {
		return pmList;
	}
	public void setPmList(List<PM> pmList) {
		this.pmList = pmList;
	}
	
	//初始化Controller 已知底层各个PM的资源使用情况
	public Controller1(String name, List<PM> pmList){
		this.name = name;
		this.vmId = 0;
		this.pmList = pmList;
		this.vmList = new ArrayList<VM>();
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
	
	//Controller1 调度
	public void schedule(int curTime){
		//先进行资源的释放
		for(PM pm: this.getPmList()){
			if(!pm.getVmList().isEmpty()){
				//根据当前时刻 释放其资源				
				pm.release(curTime);
			}
		}
		//资源分配
		while(!this.getVmList().isEmpty()){
			//找到首个满足该VM各维度资源需求的PM
			VM vm = this.getVmList().remove(0);
			for(PM pmFirst: this.getPmList()){
				if(pmFirst.isSuitable(vm)){
					pmFirst.placeVM(vm);
					break;
				}
			}
		}
	}
	
	public void scheduleMain(int time){
		//先进行资源的释放
		for(PM pm: this.getPmList()){
			if(!pm.getVmList().isEmpty()){
				//根据当前时刻 释放其资源				
				pm.release(time);
			}
		}
		//资源分配
		while(!this.getVmList().isEmpty()){
			//找到首个满足该VM各维度资源需求的PM
			VM vm = this.getVmList().remove(0);
			for(PM pmFirst: this.getPmList()){
				if(pmFirst.isSuitableMain(vm, time)){
					pmFirst.placeVMMain(vm, time);
					break;
				}
			}
		}
	}
	/**
	 * 通知各个PM记录其承载的VM的资源占用
	 */
	public void notifyPmRecord(int time) {
//		System.out.println(TIME + "记录");
		for(PM pm: this.getPmList()){
			if(!pm.getVmList().isEmpty()){
				pm.record(time);
			}
		}
		
	}
	/**
	 * 通知各个PM更新其资源剩余
	 */
	public void notifyPmUpdate(int N) {
		System.out.println(this.name + "-----开始统计更新占用");
		for(PM pm: this.getPmList()){
			if(!pm.getVmList().isEmpty()){
				pm.updateVmOccupy(N);
			}
		}	
	}
	public void notifyPmUpdate(int N, double probability) {
		System.out.println(this.name + "-----开始统计更新占用");
		for(PM pm: this.getPmList()){
			if(!pm.getVmList().isEmpty()){
				pm.updateVmOccupy(N, probability);
			}
		}	
	}
	public void notifyPmUpdateWithReal(int time) {
		System.out.println(this.name + "-----开始根据实际值更新占用");
		for(PM pm: this.getPmList()){
			if(!pm.getVmList().isEmpty()){
				pm.updateVmOccupyWithReal(time);
			}
		}	
	}
	/**
	 * 按预测值更新
	 * @param N
	 * @param time
	 */
	public void notifyPmUpdateWithPred(int Pred_N) {
		System.out.println(this.name + "-----开始根据预测更新占用");
		for(PM pm: this.getPmList()){
			if(!pm.getVmList().isEmpty()){
				pm.updateVmOccupyWithPred(Pred_N);
			}
		}	
	}
	/**
	 * 统计各个已经分配资源的VM在当前时刻是否
	 * @param voilateVMs
	 * @param time
	 * @return
	 */
	public BigDecimal judge(int time) {
		
		//统计本次调度开启的PM数量
		BigDecimal allocateCpuResource = BigDecimal.ZERO;
		BigDecimal allocateRamResource = BigDecimal.ZERO;
		BigDecimal allocateDiskResource = BigDecimal.ZERO;
		BigDecimal allocateBwResource = BigDecimal.ZERO;
		
		//BigDecimal pmOccupyResource = BigDecimal.ZERO;
		int occupyPms = 0;
		for(PM pm: this.getPmList()){
			if(pm.isActive()){
				occupyPms++;
				//if the PM contains VM , it will be regarded as an open PM
				for(VM vm : pm.getVmList()){
					vm.judge(time);
					if(vm.voilate == false){
							BigDecimal cpu = new BigDecimal(Collections.max(vm.getCpuDemand()));
							BigDecimal ram = new BigDecimal(Collections.max(vm.getRamDemand()));
							BigDecimal disk = new BigDecimal(Collections.max(vm.getDiskDemand()));
							BigDecimal bw = new BigDecimal(Collections.max(vm.getBwDemand()));
							allocateCpuResource = allocateCpuResource.add(cpu);
							allocateRamResource = allocateRamResource.add(ram);
							allocateDiskResource = allocateDiskResource.add(disk);
							allocateBwResource = allocateBwResource.add(bw);
					}else{
						if("Vice".equals(this.name)){
							System.out.println(this.name + vm.getId() + "--voilate");
						}
					}	
				}
			}	
		}
		//PM total used resource -> 4 dimension
		BigDecimal pmOccupyResource = new BigDecimal(occupyPms*4);
		if(time > 500){
			System.out.println(this.name + "--occupyPms:" + occupyPms);
		}
		
		//total VM
		BigDecimal R_C = allocateCpuResource
				.add(allocateRamResource)
				.add(allocateDiskResource).
				add(allocateBwResource).divide(pmOccupyResource, 4);
		
		return R_C;
		/*return (allocateCpuResource + 
				allocateRamResource + 
				allocateDiskResource + 
				allocateBwResource )/(occupyPms*4);*/
	}
}
