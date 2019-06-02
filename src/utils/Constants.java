package utils;
/**
 * 参数设置的一些常量
 * @author Administrator
 *
 */
public class Constants {
	
	public static int PM_NUM = 2000; //创建PM的个数
	public static double probability = 0.01; //违反需求的概率
	public static double lamda = 10.0; //服从指数分布的需求到达时间间隔
	public static int interval = 5; //记录需求时间间隔  5 minute
	public static int MIN_INTERVAL = 100; //统计的最小时间间隔
	public static int MAX_TIME = 1440; //最长执行时间
	public static String[] RESOURCE_TYPES = {"cpu", "ram", "disk", "bw"};//4种资源类型
	
}
