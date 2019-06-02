package entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import utils.Constants;

/**
 * VM类, 
 * @author Administrator
 *
 */
public class VM implements Serializable {
	/** 到达时间 */
	public double arrivalTime;
	/** 离开时刻 */
	public double leaveTime;
	/** VM id*/
	private long id;
	public long getId() {
		return id;
	}
	public void setId(long vmId) {
		this.id = vmId;
	}
	/** 计算资源需求. */
	private List<Double> cpuDemand;
	/** 内存资源需求. */
	private List<Double> ramDemand;
	/** 存储资源需求. */
	private List<Double> diskDemand;
	/** 带宽资源需求. */
	private List<Double> bwDemand;
	/**
	 * 获取cpu资源量
	 * @return
	 */
	public List<Double> getCpuDemand() {
		return cpuDemand;
	}
	public void setCpuDemand(List<Double> cpuDemand) {
		this.cpuDemand = cpuDemand;
	}
	/**
	 * 获取ram资源量
	 * @return
	 */
	public  List<Double> getRamDemand() {
		return ramDemand;
	}
	public void setRamDemand( List<Double> ramDemand) {
		this.ramDemand = ramDemand;
	}	 
	/**
	 * 获取disk资源量
	 * @return
	 */
	public  List<Double> getDiskDemand() {
		return diskDemand;
	}
	public void setDiskDemand( List<Double> diskDemand) {
		this.diskDemand = diskDemand;
	}
	/**
	 * 获取bw资源量
	 * @return
	 */
	public  List<Double> getBwDemand() {
		return bwDemand;
	}
	public void setBwDemand( List<Double> bwDemand) {
		this.bwDemand = bwDemand;
	}
	/** 承载的PM. */
	private PM PM;
	public void setPM(PM pm) {
		this.PM = pm;
	}
	public PM getPM() {
		return PM;
	}
	/** 在(1-p) 的概率下满足需求  probability*/
	private Double probability;
	/** 当前资源量是否违反 */
	public boolean voilate; 
	public Double getP() {
		return probability;
	}
	public void setP(Double p) {
		this.probability = p;
	}

	/** 根据历史记录改变当前实际占用的物理资源量，VM的历史资源分配情况，Constants.interval记录一次. */
	private HashMap<String, List<Double>> VMHistory;
	public HashMap<String, List<Double>> getVMHistory() {
		return VMHistory;
	}
	public void setVMHistory(Record record) {
//		System.out.println("VM" + this.getId() + "---记录");
		//将一条记录追加到VMHistory中
		for(String type : record.getRecord().keySet()){
			this.getVMHistory().get(type).add(record.getRecord().get(type));
		}
	}
	
	/** 当前分配的计算资源 */
	private Double currentOccupyCPU;
	/** 当前分配的存储资源 */
	private Double currentOccupyDisk;
	/** 当前分配的内存资源 */
	private Double currentOccupyRam;
	/** 当前分配的带宽资源 */
	private Double currentOccupyBw;
	public Double getCurrentOccupyCPU() {
		return currentOccupyCPU;
	}
	public void setCurrentOccupyCPU(Double currentOccupyCPU) {
		this.currentOccupyCPU = currentOccupyCPU;
	}
	public Double getCurrentOccupyDisk() {
		return currentOccupyDisk;
	}
	public void setCurrentOccupyDisk(Double currentOccupyDisk) {
		this.currentOccupyDisk = currentOccupyDisk;
	}
	public Double getCurrentOccupyRam() {
		return currentOccupyRam;
	}
	public void setCurrentOccupyRam(Double currentOccupyRam) {
		this.currentOccupyRam = currentOccupyRam;
	}
	public Double getCurrentOccupyBw() {
		return currentOccupyBw;
	}
	public void setCurrentOccupyBw(Double currentOccupyBw) {
		this.currentOccupyBw = currentOccupyBw;
	}
	
	//构造方法
	public VM(double arrivalTime, double leaveTime, 
			List<Double> cpuDemand,
			List<Double> ramDemand, 
			List<Double> diskDemand,
			List<Double> bwDemand,
			Double probability) {
		super();
		this.PM = null;
		this.arrivalTime = arrivalTime;
		this.leaveTime = leaveTime;
		setCpuDemand(cpuDemand);
		setRamDemand(ramDemand);
		setBwDemand(diskDemand);
		setDiskDemand(bwDemand);
		setP(probability);
		//初始化VMHisatory
		this.VMHistory = new HashMap<String, List<Double>>();
		for(String type : Constants.RESOURCE_TYPES){
			VMHistory.put(type, new ArrayList<Double>());
		}
	}
	
	public VM(double arrivalTime, double leaveTime, 
			List<Double> cpuDemand,
			List<Double> ramDemand, 
			List<Double> diskDemand,
			List<Double> bwDemand) {
		super();
		this.PM = null;
		this.arrivalTime = arrivalTime;
		this.leaveTime = leaveTime;
		setCpuDemand(cpuDemand);
		setRamDemand(ramDemand);
		setBwDemand(diskDemand);
		setDiskDemand(bwDemand);
		//初始化VMHisatory
		this.VMHistory = new HashMap<String, List<Double>>();
		for(String type : Constants.RESOURCE_TYPES){
			VMHistory.put(type, new ArrayList<Double>());
		}
	}
	/**
	 * 判断当前时刻 VM的资源占用是否小于分配的资源量
	 * @param time
	 * @return
	 */
	public void judge(int time) {
		
		//需求位置
		int allocateIndex = (int)Math.floor(this.arrivalTime/5);
		int index = time/5 - allocateIndex - 1;
		try{
			// PM分配的资源量
			BigDecimal allocateCpu = new BigDecimal(this.getCurrentOccupyCPU()+"");
			BigDecimal allocateRam = new BigDecimal(this.getCurrentOccupyRam()+"");
			BigDecimal allocateDisk = new BigDecimal(this.getCurrentOccupyDisk()+"");
			BigDecimal allocateBw = new BigDecimal(this.getCurrentOccupyBw()+"");
			
			//如果当前需求量
			BigDecimal curCpu = new BigDecimal(this.getCpuDemand().get(index)+"");
			BigDecimal curRam = new BigDecimal(this.getRamDemand().get(index)+"");
			BigDecimal curDisk = new BigDecimal(this.getDiskDemand().get(index)+"");
			BigDecimal curBw = new BigDecimal(this.getBwDemand().get(index)+"");
			
			//如果当前需求量  <= PM分配的资源量
			if(curCpu.compareTo(allocateCpu) <= 0 && 
					curRam.compareTo(allocateRam) <= 0  &&
							curDisk.compareTo(allocateDisk) <=0  &&
									curBw.compareTo(allocateBw) <= 0){
				//没有违背
				this.voilate = false;
			}else{
				//如果任一维度需求量  > 当前分配的资源量
				//违背
				this.voilate = true;
			}
		}catch(IndexOutOfBoundsException e){
			e.printStackTrace();
		}		
	}
}

