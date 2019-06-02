package models;

import java.util.Vector;
/**
 * 相应的模型及参数
 * @author Administrator
 *
 */
public class ModelAndPara {
	int[] model;//{p,q}
	Vector<double[]> para;
	
	public ModelAndPara(int[] model, Vector<double[]> para)
	{
		this.model=model;
		this.para=para;
	}
}
