package entities;

import java.util.HashMap;

public class Record {
	//每个维度资源和对应的历史数据
	private HashMap<String, Double> record;

	public Record(VM vm, int time){
		this.record = new HashMap<String, Double>();
		int allocateIndex = (int)Math.floor(vm.arrivalTime/5);
		int index = time/5 - allocateIndex - 1;
		//如果未记录过,先创建再记录
		try{
			this.record.put("cpu", vm.getCpuDemand().get(index));
			this.record.put("ram", vm.getRamDemand().get(index));
			this.record.put("disk", vm.getDiskDemand().get(index));
			this.record.put("bw", vm.getBwDemand().get(index));
		}catch(IndexOutOfBoundsException e){
			e.printStackTrace();
		}
	}

	public HashMap<String, Double> getRecord() {
		return record;
	}

	public void setRecord(HashMap<String, Double> record) {
		this.record = record;
	}
	
}
