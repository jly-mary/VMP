package entities;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import utils.Constants;
import utils.DataCenterUtils;
import utils.PredUtils;
import utils.StatisticUtils;
/**
 * 
 * @author Administrator
 *
 */
public class PM {
	/** The id of the PM. */
	private int id;
	
	/** 计算资源容量. */
	private Double cpuCapacity;
	
	/** 内存资源容量. */
	private Double ramCapacity;
	
	/** 存储资源容量. */
	private Double diskCapacity;

	/** 带宽资源容量. */
	private Double bwCapacity;

	/** 计算资源剩余量. */
	private Double cpuRest;
	
	/** 内存资源剩余量. */
	private Double ramRest;
	
	/** 存储资源剩余量. */
	private Double diskRest;

	/** 带宽资源剩余量. */
	private Double bwRest;

	/** PM所承载的VM列表. */
	private List<VM> vmList = new ArrayList<>();
	
	/** 构造方法 */	
	public PM(int id, Double cpuCapacity, Double ramCapacity, Double diskCapacity, Double bwCapacity) {
		super();
		this.id = id;
		this.cpuCapacity = cpuCapacity;
		this.ramCapacity = ramCapacity;
		this.diskCapacity = diskCapacity;
		this.bwCapacity = bwCapacity;
		//初始化剩余量均为PM容量
		this.cpuRest = cpuCapacity;
		this.ramRest = ramCapacity;
		this.diskRest = diskCapacity;
		this.bwRest = bwCapacity;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the cpuCapacity
	 */
	public Double getCpuCapacity() {
		return cpuCapacity;
	}

	/**
	 * @param cpuCapacity the cpuCapacity to set
	 */
	public void setCpuCapacity(Double cpuCapacity) {
		this.cpuCapacity = cpuCapacity;
	}

	/**
	 * @return the ramCapacity
	 */
	public Double getRamCapacity() {
		return ramCapacity;
	}

	/**
	 * @param ramCapacity the ramCapacity to set
	 */
	public void setRamCapacity(Double ramCapacity) {
		this.ramCapacity = ramCapacity;
	}

	/**
	 * @return the diskCapacity
	 */
	public Double getDiskCapacity() {
		return diskCapacity;
	}

	/**
	 * @param diskCapacity the diskCapacity to set
	 */
	public void setDiskCapacity(Double diskCapacity) {
		this.diskCapacity = diskCapacity;
	}

	/**
	 * @return the bwCapacity
	 */
	public Double getBwCapacity() {
		return bwCapacity;
	}

	/**
	 * @param bwCapacity the bwCapacity to set
	 */
	public void setBwCapacity(Double bwCapacity) {
		this.bwCapacity = bwCapacity;
	}

	/**
	 * @return the vmList
	 */
	public List<VM> getVmList() {
		return vmList;
	}

	/**
	 * @param vmList the vmList to set
	 */
	public void setVmList(List<VM> vmList) {
		this.vmList = vmList;
	}
	
	public double getCpuRest() {
		return cpuRest;
	}

	public void setCpuRest(double cpuRest) {
		if(cpuRest < 0){
			System.out.println("Error!");
		}
		this.cpuRest = cpuRest;
	}

	public double getRamRest() {
		return ramRest;
	}

	public void setRamRest(double ramRest) {
		if(ramRest < 0){
			System.out.println("Error!");
		}
		this.ramRest = ramRest;
	}

	public double getDiskRest() {
		return diskRest;
	}

	public void setDiskRest(double diskRest) {
		if(diskRest < 0){
			System.out.println("Error!");
		}
		this.diskRest = diskRest;
	}

	public double getBwRest() {
		return bwRest;
	}

	public void setBwRest(double bwRest) {
		if(bwRest < 0){
			System.out.println("Error!");
		}
		this.bwRest = bwRest;
	}
	/**
	 * 判断是否开启
	 * @return
	 */
	public boolean isActive(){
		//如果各个维度的资源剩余均小于其容量，则为激活状态
		BigDecimal cpuRest = new BigDecimal(this.getCpuRest() + "");
		BigDecimal ramRest = new BigDecimal(this.getRamRest() + "");
		BigDecimal diskRest = new BigDecimal(this.getDiskRest() + "");
		BigDecimal bwRest = new BigDecimal(this.getBwRest() + "");
		BigDecimal _1 = BigDecimal.ONE;
		return !(cpuRest.compareTo(_1) == 0 &&
				ramRest.compareTo(_1) == 0 &&
				diskRest.compareTo(_1) == 0 &&
				bwRest.compareTo(_1) == 0);
	}
	/**
	 * PM判断自身资源是否满足vm的需求
	 * @param vm
	 * @return
	 */
	public boolean isSuitable(VM vm){
		return (DataCenterUtils.getMaxDemand(vm.getCpuDemand()) <= this.getCpuRest() &&
					DataCenterUtils.getMaxDemand(vm.getRamDemand()) <= this.getRamRest() &&
						DataCenterUtils.getMaxDemand(vm.getDiskDemand()) <= this.getDiskRest()&&
								DataCenterUtils.getMaxDemand(vm.getBwDemand()) <= this.getBwRest()
								);
	}
	/**
	 * 初始，将给定的VM放置在PM中
	 * @param vm
	 * @pre boker已经保证该VM可以放置在该PM中
	 */
	public void placeVM(VM vm){
		//1. 该VM列入承载list中
		this.getVmList().add(vm);
		vm.setPM(this);
		vm.voilate = false;

		// 更新		
		vm.setCurrentOccupyCPU(DataCenterUtils.getMaxDemand(vm.getCpuDemand()));
		this.setCpuRest(this.getCpuRest() - vm.getCurrentOccupyCPU());
		
		vm.setCurrentOccupyRam(DataCenterUtils.getMaxDemand(vm.getRamDemand()));
		this.setRamRest(this.getRamRest() - vm.getCurrentOccupyRam());
		
		vm.setCurrentOccupyDisk(DataCenterUtils.getMaxDemand(vm.getDiskDemand()));
		this.setDiskRest(this.getDiskRest() - vm.getCurrentOccupyDisk());
		
		vm.setCurrentOccupyBw(DataCenterUtils.getMaxDemand(vm.getBwDemand()));
		this.setBwRest(this.getBwRest() - vm.getCurrentOccupyBw());

		if( vm.getCurrentOccupyCPU() < Collections.max(vm.getCpuDemand()) ){
			System.out.println("cpu");
			System.out.println(vm.getCurrentOccupyCPU());
			System.out.println(Collections.max(vm.getCpuDemand()));
		}
		if( vm.getCurrentOccupyRam() < Collections.max(vm.getRamDemand()) ){
			System.out.println("ram");
			System.out.println(vm.getCurrentOccupyRam());
			System.out.println(Collections.max(vm.getRamDemand()) );
		}
		if( vm.getCurrentOccupyDisk() < Collections.max(vm.getDiskDemand()) ){
			System.out.println("disk");
			System.out.println(vm.getCurrentOccupyDisk());
			System.out.println(Collections.max(vm.getDiskDemand()));
		}
		if( vm.getCurrentOccupyBw()  < Collections.max(vm.getBwDemand()) ){
			System.out.println("bw");
			System.out.println(vm.getCurrentOccupyBw());
			System.out.println(Collections.max(vm.getBwDemand()));
		}
	}
	
	
	
	/**
	 * 初始，按照下一时刻确定的资源量 在PM中分配资源给VM
	 * @param vm
	 * @pre boker已经保证该VM可以放置在该PM中
	 */
	public void placeVMMain(VM vm, int time){
		//1. 该VM列入承载list中
		this.getVmList().add(vm);
		vm.setPM(this);
		vm.voilate = false;
		
		// 更新
		int arrivalIndex = (int)Math.floor(vm.arrivalTime/5);
		int current = time/5 - arrivalIndex -1;
		
		double nextCpu = vm.getCpuDemand().get(current);
		double nextRam = vm.getRamDemand().get(current);
		double nextDisk = vm.getDiskDemand().get(current);
		double nextBw = vm.getBwDemand().get(current);
		
		vm.setCurrentOccupyCPU(nextCpu);
		this.setCpuRest(this.getCpuRest() - nextCpu);
		
		vm.setCurrentOccupyRam(nextRam);
		this.setRamRest(this.getRamRest() - nextRam);
		
		vm.setCurrentOccupyDisk(nextDisk);
		this.setDiskRest(this.getDiskRest() - nextDisk);
		
		vm.setCurrentOccupyBw(nextBw);
		this.setBwRest(this.getBwRest() - nextBw);
	}
	public boolean isSuitableMain(VM vm, int time){
		int arrivalIndex = (int)Math.floor(vm.arrivalTime/5);
		int current = time/5 - arrivalIndex - 1;
		
		return (vm.getCpuDemand().get(current) <= this.getCpuRest() &&
					vm.getRamDemand().get(current) <= this.getRamRest() &&
						vm.getDiskDemand().get(current) <= this.getDiskRest()&&
								vm.getBwDemand().get(current)  <= this.getBwRest()
								);
	}
	
	/**
	 * 隔N*interval时间间隔 更新PM中各个VM占用的资源量
	 */
	public void updateVmOccupy(int N){
		// 查看每个VM的历史数据，由统计信息和概率p来更新每个VM的资源分配量
		for(VM vm : this.getVmList()){
			int historyLen = vm.getVMHistory().get("cpu").size(); 
			if(historyLen >= N){
				//实际各个维度资源的实际分配量
				HashMap<String, Double> statisticDemands = StatisticUtils.getStatisticDemand(vm);
				double cpuRest = this.getCpuRest() + vm.getCurrentOccupyCPU() - statisticDemands.get("cpu");
				double ramRest = this.getRamRest() + vm.getCurrentOccupyRam() - statisticDemands.get("ram");
				double diskRest = this.getDiskRest() + vm.getCurrentOccupyDisk() - statisticDemands.get("disk");
				double BwRest = this.getBwRest() + vm.getCurrentOccupyBw() - statisticDemands.get("bw");
				
				if(cpuRest >=0 && ramRest >=0 && diskRest >=0 && BwRest >= 0){
					this.setCpuRest(cpuRest);
					vm.setCurrentOccupyCPU(statisticDemands.get("cpu"));
					
					this.setRamRest(ramRest);
					vm.setCurrentOccupyRam(statisticDemands.get("ram"));
					
					this.setDiskRest(diskRest);
					vm.setCurrentOccupyDisk(statisticDemands.get("disk"));
					
					this.setBwRest(BwRest);
					vm.setCurrentOccupyBw(statisticDemands.get("bw"));
				}
			}
		}
	}
	public void updateVmOccupy(int N, double probability){
		// 查看每个VM的历史数据，由统计信息和概率p来更新每个VM的资源分配量
		for(VM vm : this.getVmList()){
			vm.setP(probability);
			int historyLen = vm.getVMHistory().get("cpu").size(); 
			if(historyLen >= N){
				//实际各个维度资源的实际分配量
				HashMap<String, Double> statisticDemands = StatisticUtils.getStatisticDemand(vm);
				double cpuRest = this.getCpuRest() + vm.getCurrentOccupyCPU() - statisticDemands.get("cpu");
				double ramRest = this.getRamRest() + vm.getCurrentOccupyRam() - statisticDemands.get("ram");
				double diskRest = this.getDiskRest() + vm.getCurrentOccupyDisk() - statisticDemands.get("disk");
				double BwRest = this.getBwRest() + vm.getCurrentOccupyBw() - statisticDemands.get("bw");
				if(cpuRest >=0 && ramRest >=0 && diskRest >=0 && BwRest >= 0){
					this.setCpuRest(cpuRest);
					vm.setCurrentOccupyCPU(statisticDemands.get("cpu"));
					
					this.setRamRest(ramRest);
					vm.setCurrentOccupyRam(statisticDemands.get("ram"));
					
					this.setDiskRest(diskRest);
					vm.setCurrentOccupyDisk(statisticDemands.get("disk"));
					
					this.setBwRest(BwRest);
					vm.setCurrentOccupyBw(statisticDemands.get("bw"));
				}
			}
		}
		//更新后判断是否为激活状态
		if(!this.isActive()){
			this.setCpuRest(this.cpuCapacity);
			this.setRamRest(this.ramCapacity);
			this.setDiskRest(this.diskCapacity); 
			this.setBwRest(this.bwCapacity);
		}
	}
	public void updateVmOccupyWithReal(int time){

		for(VM vm : this.getVmList()){
			int arrivalIndex = (int)Math.floor(vm.arrivalTime/5);
			int next = time/5 - arrivalIndex;
			int len = vm.getBwDemand().size();
			
			if(next < len){
				double cpuNext = vm.getCpuDemand().get(next);
				double ramNext = vm.getRamDemand().get(next);
				double diskNext = vm.getDiskDemand().get(next);
				double BwNext = vm.getBwDemand().get(next);

				double cpuRest = this.getCpuRest() + vm.getCurrentOccupyCPU() - cpuNext;
				double ramRest = this.getRamRest() + vm.getCurrentOccupyRam() - ramNext;
				double diskRest = this.getDiskRest() + vm.getCurrentOccupyDisk() - diskNext;
				double BwRest = this.getBwRest() + vm.getCurrentOccupyBw() - BwNext;
				//如果当前所在PM的剩余资源支持波动
				if(cpuRest >=0 && ramRest >=0 && diskRest >=0 && BwRest >= 0){
					this.setCpuRest(cpuRest);
					vm.setCurrentOccupyCPU(cpuNext);
					
					this.setRamRest(ramRest);
					vm.setCurrentOccupyRam(ramNext);
					
					this.setDiskRest(diskRest);
					vm.setCurrentOccupyDisk(diskNext);
					
					this.setBwRest(BwRest);
					vm.setCurrentOccupyBw(BwNext);
				}else{
					//System.out.println("该VM由于资源剩余不足而没有更新");
				}
			}
			//更新后判断是否为激活状态
			if(!this.isActive()){
				this.setCpuRest(this.cpuCapacity);
				this.setRamRest(this.ramCapacity);
				this.setDiskRest(this.diskCapacity); 
				this.setBwRest(this.bwCapacity);
			}
		}
	}
	public void updateVmOccupyWithPred(int Pred_N){
		//1. 更新该PM的资源
		for(VM vm : this.getVmList()){
			int historyLen = vm.getVMHistory().get("cpu").size();
			if(historyLen >= Pred_N){
				HashMap<String, Double> predDemands = null;
				try{
					/** 根据历史记录预测下一时刻各维度需求 */
					predDemands = PredUtils.pred(vm);
				}catch(Exception e){
			    	continue;
			    }
				double cpuNext = predDemands.get("cpu");
				double ramNext = predDemands.get("ram");
				double diskNext = predDemands.get("disk");
				double bwNext = predDemands.get("bw");
				
				double cpuRest = this.getCpuRest() + vm.getCurrentOccupyCPU() - cpuNext;
				double ramRest = this.getRamRest() + vm.getCurrentOccupyRam() - ramNext;
				double diskRest = this.getDiskRest() + vm.getCurrentOccupyDisk() - diskNext;
				double BwRest = this.getBwRest() + vm.getCurrentOccupyBw() - bwNext;
				//如果当前所在PM的剩余资源支持波动
				if(cpuRest >=0 && ramRest >=0 && diskRest >=0 && BwRest >= 0){
					this.setCpuRest(cpuRest);
					vm.setCurrentOccupyCPU(cpuNext);
					
					this.setRamRest(ramRest);
					vm.setCurrentOccupyRam(ramNext);
					
					this.setDiskRest(diskRest);
					vm.setCurrentOccupyDisk(diskNext);
					
					this.setBwRest(BwRest);
					vm.setCurrentOccupyBw(bwNext);
				}else{
					//System.out.println("该VM由于资源剩余不足而没有更新");
				}
			}
		}
			
		//2. 更新后判断是否为激活状态
		if(!this.isActive()){
			this.setCpuRest(this.cpuCapacity);
			this.setRamRest(this.ramCapacity);
			this.setDiskRest(this.diskCapacity); 
			this.setBwRest(this.bwCapacity);
		}
	}
	
	@SuppressWarnings("unused")
	private List<String> getResourceTypes(VM vm){
	    Field[] fields = vm.getClass().getDeclaredFields();
		List<String> resourceTypes = new ArrayList<>();
		for(int i=0; i<fields.length; i++){
			String attr = fields[i].getName();
			if(attr.contains("Demand")){
				resourceTypes.add(attr.split("D")[0]);
			}
		} 
		return resourceTypes;
	}
	/**
	 * PM负责记录VM的资源利用的历史信息
	 */
	public void record(int time) {
		for(VM vm : this.getVmList()){
			vm.setVMHistory(new Record(vm, time));
		}
	}
	/**
	 * PM根据当前时刻释放VM占用的资源
	 * @param time
	 */
	public void release(int time) {
		BigDecimal curTime = new BigDecimal((double)time);
		Iterator<VM> vmListIter = this.getVmList().iterator();
		VM vm = null;
		while(vmListIter.hasNext()){
			vm = vmListIter.next();
			BigDecimal vmLeaveTime = new BigDecimal(vm.leaveTime);
			//如果vm离开
			if(curTime.compareTo(vmLeaveTime) >= 0){
				//更新PM剩余资源的占用
				this.setCpuRest(this.getCpuRest() + vm.getCurrentOccupyCPU());
				this.setRamRest(this.getRamRest() + vm.getCurrentOccupyRam());
				this.setDiskRest(this.getDiskRest() + vm.getCurrentOccupyDisk());
				this.setBwRest(this.getBwRest() + vm.getCurrentOccupyBw());
				//删除VM
				vmListIter.remove();
			}
		}
	}	
}
