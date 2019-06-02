package utils;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;
import org.junit.Test;

import entities.VM;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

/**
 * 读取结果画图
 * @author Administrator
 *
 */
public class DrawUtils {
	/**
	 * 获取数据集
	 * @param result
	 * @return
	 */
    public static XYDataset createDataset(File result) {      

        XYSeries xyseriesMain = new XYSeries("MAIN");      
        XYSeries xyseriesVice = new XYSeries("VICE"); 
        XYSeriesCollection dataSet = new XYSeriesCollection();  
        
        //创建结果的输入流
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(result);
			Workbook rwb = null;
			try {
				//读取excel文件
				rwb = Workbook.getWorkbook(fis);
				Sheet[] sheets = rwb.getSheets(); 
				for(Sheet sheet : sheets){
					//不管表头 从第一行开始
					for (int j = 1; j < sheet.getRows(); j++) {   
						   //获取一行
		                   Cell[] rowCells = sheet.getRow(j);  
		                   xyseriesMain.add(Double.parseDouble(rowCells[0].getContents()),
		                		   Double.parseDouble(rowCells[1].getContents()));
		                   xyseriesVice.add(Double.parseDouble(rowCells[0].getContents()), 
		                		   Double.parseDouble(rowCells[2].getContents()));
		             }
				}
			} catch (BiffException | IOException e) {
				e.printStackTrace();
			}finally{
				if(rwb != null){
					rwb.close();
				}
			}      
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}finally{
			if(fis != null){
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
        dataSet.addSeries(xyseriesMain);      
        dataSet.addSeries(xyseriesVice);  
        
        return dataSet;      

    }
    public static XYDataset createDatasetWithMultiRes(File result, String... strings ) {      
    	int n = strings.length;
    	ArrayList<XYSeries> seriesList = new ArrayList<>();
    	for(int i=0; i<n; i++){
    		seriesList.add(new XYSeries(strings[i])); 
    	}
//        XYSeries xyseriesMain = new XYSeries(strings[0]) 
//        XYSeries xyseriesVice = new XYSeries(strings[1]); 
//        XYSeries xyseriesReal = new XYSeries(strings[2]); 
        XYSeriesCollection dataSet = new XYSeriesCollection();  
        
        //创建结果的输入流
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(result);
			Workbook rwb = null;
			try {
				//读取excel文件
				rwb = Workbook.getWorkbook(fis);
				Sheet[] sheets = rwb.getSheets(); 
				for(Sheet sheet : sheets){
					//不管表头 从第一行开始
					for (int j = 1; j < sheet.getRows(); j++) {   
						   //获取一行
		                   Cell[] rowCells = sheet.getRow(j); 
		                   for(int i=1; i<=n; i++){
		                	   seriesList.get(i-1).add(Double.parseDouble(rowCells[0].getContents()),
			                		   Double.parseDouble(rowCells[i].getContents()));
		                   }
//		                   xyseriesMain.add(Double.parseDouble(rowCells[0].getContents()),
//		                		   Double.parseDouble(rowCells[1].getContents()));
//		                   xyseriesVice.add(Double.parseDouble(rowCells[0].getContents()), 
//		                		   Double.parseDouble(rowCells[2].getContents()));
//		                   xyseriesReal.add(Double.parseDouble(rowCells[0].getContents()), 
//		                		   Double.parseDouble(rowCells[3].getContents()));
		             }
				}
			} catch (BiffException | IOException e) {
				e.printStackTrace();
			}finally{
				if(rwb != null){
					rwb.close();
				}
			}      
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}finally{
			if(fis != null){
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		for(XYSeries series: seriesList){
			dataSet.addSeries(series);
		}
//        dataSet.addSeries(xyseriesMain);      
//        dataSet.addSeries(xyseriesVice);  
//        dataSet.addSeries(xyseriesReal);  
        
        return dataSet;      

    }
    /**
     * 读取文件夹（dir）下中各文件夹中包含（str）的数据的平均值
     * @param result
     * @return
     */
    public static XYDataset createAvergaeDataset(File resultDir) {      

        XYSeries xyseriesMain = new XYSeries("MAIN");      
        XYSeries xyseriesVice = new XYSeries("VICE"); 
        XYSeriesCollection dataSet = new XYSeriesCollection();  
        
        FileInputStream fis = null;
        Workbook rwb = null;
        for(File dirP : resultDir.listFiles()){
        	String name = dirP.getName().replace(".xls", "");
        	//System.out.println(dirP.getName());
        	//指定的概率
        	double p = Double.valueOf(name.replace("p_", ""));
        	try {
	        	fis = new FileInputStream(dirP);
				rwb = Workbook.getWorkbook(fis);
				Sheet[] sheets = rwb.getSheets(); 
					for(Sheet sheet : sheets){
						//计算对应表的平均值
						HashMap<String, Double> map = calAverage(sheet);
						 xyseriesMain.add(p, map.get("MAIN"));
						 xyseriesVice.add(p, map.get("VICE"));
					}
        	}catch (FileNotFoundException e) {
				e.printStackTrace();
			}catch (BiffException | IOException e) {
				e.printStackTrace();
			}finally{
				if(fis != null){
					try {
						fis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(rwb != null){
					rwb.close();
				}
			}
        }
       
        dataSet.addSeries(xyseriesMain);      
        dataSet.addSeries(xyseriesVice);  
        
        return dataSet;      

    }
    @Test
    public void testCalAverage(){ 
    	File file = new File("D:\\ProgramFiles\\java\\workspace\\VMP\\results\\demo6\\OCCUPY_PM_RATE.xls");
    	//VOILATE_VM_RATE OCCUPY_PM_RATE
        //创建结果的输入流
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			Workbook rwb = null;
			try {
				//读取excel文件
				rwb = Workbook.getWorkbook(fis);
				Sheet[] sheets = rwb.getSheets(); 
				for(Sheet sheet : sheets){
					HashMap<String, Double> map = calAverage(sheet);
					DecimalFormat df = new DecimalFormat("#.#####%");
					System.out.println("main PM OR_average: " + df.format(map.get("MAIN")));
					System.out.println("vice PM OR_average: " + df.format(map.get("VICE")));
				}
			} catch (BiffException | IOException e) {
				e.printStackTrace();
			}finally{
				if(rwb != null){
					rwb.close();
				}
			}      
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}finally{
			if(fis != null){
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
    }
    private static HashMap<String, Double> calAverage(Sheet sheet) {
    	DecimalFormat df = new DecimalFormat("0.00000");
		
    	//计算表的平均值
    	HashMap<String, Double> res = new HashMap<>();
    	ArrayList<Double> colMain = new ArrayList<>();
    	ArrayList<Double> colVice = new ArrayList<>();
    	int total = sheet.getRows() - 1;
//    	System.out.println(total);
    	
    	for (int j = 1; j < sheet.getRows(); j++) {   
			//获取一行
            Cell[] rowCells = sheet.getRow(j);  
            colMain.add(Double.parseDouble(rowCells[1].getContents()));
            colVice.add(Double.parseDouble(rowCells[2].getContents()));
        }
    	//计算colMain的和
    	double sumMain = 0.0D;
    	for(double num: colMain){
    		sumMain += num;
    	}
//    	System.out.println("sumMain: " + sumMain);
    	res.put("MAIN", Double.parseDouble(df.format(sumMain/total)));
    	
    	//计算colVice的和
    	double sumVice = 0.0D;
    	for(double num: colVice){
    		sumVice += num;
    	}
//    	System.out.println("sumVice: " + sumVice);
    	res.put("VICE", Double.parseDouble(df.format(sumVice/total)));
    	//res.put("VICE", StatisticUtils.format(sumVice/total));
    	
		return res;
	}
	
    /**
     * 读取文件夹（dir）下中各文件夹中包含item 的数据的平均值
     * @param result
     * @return
     */
    public static XYDataset getDataset(File resultDir, String str) {
    	/*//y坐标名称
    	String yLabel = str.replace(".xls", "");*/
    	
    	//该列表保存不同的统计最小时间间隔
    	LinkedHashMap<Integer, XYSeries> map = new LinkedHashMap<>();
    	for(int N=50; N<=120; N+=10){
    		map.put(N, new XYSeries(N+"_main"));
    	}
    	double averageVice = 0.0D;
    	int cnt = 0;
    	//返回最终数据集
        XYSeriesCollection dataset = new XYSeriesCollection();  
        
        FileInputStream fis = null;
        Workbook rwb = null;
        File[] p_N_results = resultDir.listFiles();
        int N = 0;
        double p = 0;
        HashSet<Double> pSet = new HashSet<>();
        //获取P_N文件下的每一个文件夹
        for(File p_N_result : p_N_results){
        	//只看文件夹
        	if(!p_N_result.isDirectory() || "pics".equals(p_N_result.getName()) ){
        		continue;
        	}
        	//文件夹名称获取间隔N
        	N = getN(p_N_result.getName());
        	//文件夹名称获取概率p
        	p = getProbability(p_N_result.getName());
        	pSet.add(p);
        	//每一个xls文件
        	for(File xls : p_N_result.listFiles()){
        		if(xls.getName().equals(str)){
        			try {
						fis = new FileInputStream(xls);
						rwb = Workbook.getWorkbook(fis);
						Sheet[] sheets = rwb.getSheets(); 
	     				for(Sheet sheet : sheets){
	     					//计算对应表中对应列的的平均值
	     					double average = calAverage(sheet, 1);
	     					map.get(N).add(p, average);
	     					//vice
	     					averageVice += calAverage(sheet, 2);
	     					cnt++;	     					
	    				}
					}catch (FileNotFoundException e) {
						e.printStackTrace();
					}catch (BiffException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}finally{
						if(fis != null){
							try {
								fis.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						if(rwb != null){
							rwb.close();
						}
					}
        		}
        	}
        	
        }
        //
        XYSeries Vice  = new XYSeries("vice");
        for(double pp : pSet){
        	Vice.add(pp, averageVice/cnt);
        }
        dataset.addSeries(Vice);
        //
       for(XYSeries series : map.values()){
    	   dataset.addSeries(series);
       }
       return dataset;      
    }

    /**
     * 根据数据集画图
     * @param Dataset
     * @param pictureName
     */
	public static void draw(XYDataset Dataset,
			String outputPath,
			String pictureName, 
			String xLabel, String yLabel) {
		
		//设置图像风格
	    StandardChartTheme ChartTheme = new StandardChartTheme("CN");
	    
	    ChartTheme.setLargeFont(new Font("宋体", Font.PLAIN, 15));
	    ChartTheme.setExtraLargeFont(new Font("宋体", Font.PLAIN, 15));
	    ChartTheme.setRegularFont(new Font("宋体", Font.PLAIN, 15));
	    ChartFactory.setChartTheme(ChartTheme);	

	    //创建图形
	    JFreeChart chart = ChartFactory.createXYLineChart(
	    	pictureName,       //图名字
	    	xLabel,   //横坐标
	        yLabel,   //纵坐标
	        Dataset,//数据集
	        PlotOrientation.VERTICAL,
	        false, // 显示图例
	        true, // 采用标准生成器 
	        false);// 不生成超链接

	    //自定义图例
	    LegendTitle legend = new LegendTitle(chart.getPlot());//创建图例
	    legend.setBorder(0.5,0.5,0.5,0.5);//设置四周的边距，带线框. （不显示边框）
	    legend.setPosition(RectangleEdge.RIGHT);//设置图例的位置
	    legend.setMargin(0,0,50,10);//这样就只是距离右边有距离 margin 5
	    chart.addLegend(legend);//图表中添加图例 

	    // 使用CategoryPlot设置各种参数。以下设置可以省略。      
        XYPlot plot = (XYPlot) chart.getPlot();      
        // 背景色 透明度      
        plot.setBackgroundAlpha(0.5f);      
        // 前景色 透明度      
        plot.setForegroundAlpha(0.5f);
        
        /*if(pictureName.contains("VOILATE")){
        	NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        	yAxis.setRange(0, 0.1);
        }*/
        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setRange(0.65, 1);
        
	    //图边框
	    ChartFrame chartFrame = new ChartFrame("ChartFrame", chart);
	    chartFrame.pack();
	    chartFrame.setVisible(true);
	    
	    //保存数据图
	    System.out.println("保存至："+ outputPath + pictureName);
	    saveAsFile(chart, outputPath, pictureName+".png", 800, 500);
	    
	  }
	/**
	 * 保存为png图片
	 * @param chart
	 * @param outputPath
	 * @param pictureName
	 * @param weight
	 * @param height
	 */
    private static void saveAsFile(JFreeChart chart, String outputPath, String pictureName, int weight, int height) {
    	
        FileOutputStream out = null;      
        try {      
            File outFile = new File(outputPath + pictureName); 
            out = new FileOutputStream(outFile);
            // 保存为PNG      
            ChartUtilities.writeChartAsPNG(out, chart, weight, height);      
            // 保存为JPEG      
            // ChartUtilities.writeChartAsJPEG(out, chart, weight, height);      
            out.flush();      

        } catch (FileNotFoundException e) {      
            e.printStackTrace();      
        } catch (IOException e) {      
            e.printStackTrace();      
        } finally {      
            if (out != null) {      
                try {      
                    out.close();      
                } catch (IOException e) {      
                    // do nothing      
                }      
            }      
        }      
    } 
	
	/**
	 * 根据结果画图
	 * @param result
	 * @return
	 * @throws Exception
	 */
	public void testDraw() {
		//设置图像风格
	    StandardChartTheme ChartTheme = new StandardChartTheme("CN");
	    
	    ChartTheme.setLargeFont(new Font("宋体", Font.PLAIN, 15));
	    ChartTheme.setExtraLargeFont(new Font("宋体", Font.PLAIN, 15));
	    ChartTheme.setRegularFont(new Font("宋体", Font.PLAIN, 15));
	    ChartFactory.setChartTheme(ChartTheme);	

	    //生成数据集
	    File result = new File("D:\\ProgramFiles\\java\\workspace\\VMP\\results\\" + 
	    		"OCCUPY_PMS_RATE.xls");
	    //CategoryDataset Dataset = getDataset(result);
	    XYDataset  Dataset = createDataset(result);

	    //创建图形
	    JFreeChart chart = ChartFactory.createXYLineChart(
	        "折线图",//图名字
	        "时间",//横坐标
	        "分配VM比例",//纵坐标
	        Dataset,//数据集
	        PlotOrientation.VERTICAL,
	        false, // 显示图例
	        true, // 采用标准生成器 
	        false);// 不生成超链接
	    
//	    chart.getLegend().setItemFont(new Font("宋体", Font.PLAIN, 12));//设置图例的字体(防止乱码)
	    LegendTitle legend = new LegendTitle(chart.getPlot());//创建图例
	    legend.setBorder(0.5,0.5,0.5,0.5);//设置四周的边距，带线框. （不显示边框）
	    legend.setPosition(RectangleEdge.RIGHT);//设置图例的位置
	    legend.setMargin(0,0,50,10);//这样就只是距离右边有距离 margin 5
	    chart.addLegend(legend);//图表中添加图例 

	    // 使用CategoryPlot设置各种参数。以下设置可以省略。      
        XYPlot plot = (XYPlot) chart.getPlot();      
        // 背景色 透明度      
        plot.setBackgroundAlpha(0.5f);      
        // 前景色 透明度      
        plot.setForegroundAlpha(0.5f);      

        /*// 其它设置可以参考XYPlot类  
	    CategoryPlot plot = (CategoryPlot)Chart.getPlot();
	    plot.setBackgroundPaint(Color.LIGHT_GRAY);//背景底部
	    plot.setRangeGridlinePaint(Color.WHITE); 	//背景底部线
	    plot.setOutlinePaint(Color.RED);       	//边界线
*/
	    //图边框
	    ChartFrame chartFrame = new ChartFrame("ChartFrame", chart);
	    chartFrame.pack();
	    chartFrame.setVisible(true);
	    
	    //保存数据结构图
	    String outputPath =  "D:\\ProgramFiles\\java\\workspace\\VMP\\results\\";
	    String pictureName = "aa.png";
	    saveAsFile(chart, outputPath, pictureName, 800, 500);
	  }
    
	private static int getN(String name) {
		//文件夹名称获取概率
		int positionN = name.indexOf('N');
		StringBuilder sb = new StringBuilder();
		for(int i= positionN + 1; i<name.length(); i++){
			char c = name.charAt(i);
			sb.append(c);
		}
		return Integer.parseInt(sb.toString());
	}
	private static double calAverage(Sheet sheet, int colNum) {
		//计算sheet某一列的平均值
    	ArrayList<Double> column = new ArrayList<>();
    	int total = sheet.getRows() - 1;
    	//int total = 0;
    	
    	for (int j = 1; j < sheet.getRows(); j++) {   
			//获取一行
            Cell[] rowCells = sheet.getRow(j);  
            column.add(Double.parseDouble(rowCells[colNum].getContents()));
        }
    	
    	//计算colMain的和
    	double sumMain = 0.0D;
    	for(double num: column){
    		/*if(num != 0.0D){
    			total ++;
    		}*/
    		sumMain += num;
    	}
//    	System.out.println("sumMain: " + sumMain);
    	return StatisticUtils.format(sumMain/total);
	}
	@Test
	public void testGetProbability() {
		String name = "p_0.02N120";
//		String name = "p_0.05N120";
		//文件夹名称获取概率
				int position_ = name.indexOf('_');
				StringBuilder sb = new StringBuilder();
				for(int i=position_+1; i<name.length(); i++){
					char c = name.charAt(i);
					if(c != 'N'){
						sb.append(c);
					}else{
						break;
					}
				}
		System.out.println(sb.toString());
	}
    public static double getProbability(String name){
	   	//文件夹名称获取概率
		int position_ = name.indexOf('_');
		StringBuilder sb = new StringBuilder();
		for(int i=position_+1; i<name.length(); i++){
			char c = name.charAt(i);
			if(c != 'N'){
				sb.append(c);
			}else{
				break;
			}
		}
		return Double.parseDouble(sb.toString());
   }

    //前N个时间间隔的资源需求量的分布
    @Test
    public void testPdf(){
          VM vm = DataCenterUtils.createVM();
          List<Double> cpuReList = vm.getCpuDemand();
          double N = (double) cpuReList.size();
  		  //小于key的需求概率
          TreeMap<Integer, Integer> demandFreqMap = new TreeMap<>();
          for(int i=0; i<=100; i++){
        	  demandFreqMap.put(i, 0);
          }
          for(double demand: cpuReList){
        	  int id = (int)((demand - 0)*100);
        	  demandFreqMap.put(id, demandFreqMap.get(id)+1);
  		  }
          
	      // 使用普通数据集  
	      DefaultCategoryDataset chartDate = new DefaultCategoryDataset(); 
	      for(int i=0; i<=100; i++){
	    	  double iFormat = StatisticUtils.format(i*0.01);
	    	  //概率分布， 
//	    	  chartDate.addValue(demandFreqMap.get(i)/(double)cnt, "demand prabability", String.valueOf(iFormat));  
	    	  chartDate.addValue(demandFreqMap.get(i)/N, 
	    			  			"demand prabability",
	    			  			String.valueOf(iFormat));  
	      }
	      
          try {  
              // 从数据库中获得数据集  
              DefaultCategoryDataset data = chartDate;  
                
              // 使用ChartFactory创建3D柱状图，不想使用3D，直接使用createBarChart  
              JFreeChart chart = ChartFactory.createBarChart(  
                      "资源需求-频次", // 图表标题  
                      "demand", // x目录轴的显示标签  
                      "frequency", // y 数值轴的显示标签  
                      data, // 数据集  
                      PlotOrientation.VERTICAL, // 图表方向，此处为垂直方向  
                      // PlotOrientation.HORIZONTAL, //图表方向，此处为水平方向  
                      false, // 是否显示图例  
                      true, // 是否生成工具  
                      false // 是否生成URL链接  
                      );    
              // 设置图片有边框  
              chart.setBorderVisible(true);  
              //Font kfont = new Font("宋体", Font.PLAIN, 12);    // 底部  
              Font titleFont = new Font("宋体", Font.PLAIN, 12); // 图片标题  
              // 图片标题  
              chart.setTitle(new TextTitle(chart.getTitle().getText(), titleFont));  
              // 底部  
              //chart.getLegend().setItemFont(kfont);  
              
              // 得到坐标设置字体解决乱码  
              CategoryPlot categoryplot = (CategoryPlot) chart.getPlot();  
              categoryplot.setDomainGridlinesVisible(true);  
              categoryplot.setRangeCrosshairVisible(true);  
              categoryplot.setRangeCrosshairPaint(Color.blue);  
              
              BarRenderer barrenderer = (BarRenderer) categoryplot.getRenderer();
              barrenderer.setMaximumBarWidth(0.1); //设置柱子宽度 
              barrenderer.setMinimumBarLength(0.1); //设置柱子高度 
              barrenderer.setSeriesPaint(0,Color.ORANGE); //设置柱的颜色 
              barrenderer.setBaseItemLabelFont(new Font("宋体", Font.PLAIN, 12));  
              barrenderer.setSeriesItemLabelFont(1, new Font("宋体", Font.PLAIN, 12));  
              
              CategoryAxis xAxis = categoryplot.getDomainAxis();
              xAxis.setTickLabelsVisible(true);
              /*------设置X轴坐标上的文字-----------*/  
              xAxis.setTickLabelFont(new Font("sans-serif", Font.PLAIN, 5));  
              /*------设置X轴的标题文字------------*/  
              xAxis.setLabelFont(new Font("宋体", Font.PLAIN, 12));  
              xAxis.setTickMarksVisible(true);
              xAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_90);
              
              NumberAxis yAxis = (NumberAxis) categoryplot.getRangeAxis();  
              /*------设置Y轴坐标上的文字-----------*/  
              yAxis.setTickLabelFont(new Font("sans-serif", Font.PLAIN, 12));  
              /*------设置Y轴的标题文字------------*/  
              yAxis.setLabelFont(new Font("宋体", Font.PLAIN, 12));  
              yAxis.setRange(0, 0.5);
              
		      ChartFrame chartFrame = new ChartFrame("ChartFrame", chart);
		      chartFrame.pack();
		      chartFrame.setVisible(true);
		      
              // 生成图片并输出  
		      String outputPath =  "D:\\ProgramFiles\\java\\workspace\\VMP\\results\\";
			  String pictureName = "aa.png";
		      saveAsFile(chart, outputPath, pictureName, 800, 500);  
          } catch (Exception e) {  
              e.printStackTrace();  
          }  
      } 
  	//@Test
    public void testTimeSeries(){
  		String workload = "20110309";
		String path = "D:\\ProgramFiles\\java\\workspace\\VMP\\workload\\planetlab\\";
		File workloadPath = new File(path + workload);
		
		/** 每个文件代表一个VM负载，生存时隙个数均为288*/
		File[] files = workloadPath.listFiles();
		
		int workloadLen = files.length;
		int num = (int) (workloadLen*Math.random());
		File selectedWorkload = files[num];
		//获取需求
		ArrayList<Double> demand = new ArrayList<>();
		try{				
			BufferedReader input = new BufferedReader(new FileReader(selectedWorkload.getAbsolutePath()));
			for(int n=0; n<288; n++){
				int aa = Integer.valueOf(input.readLine());
				demand.add(aa / 100.0);
			}
			input.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		//根据需求画出时间序列图
		XYSeries timeSeries = new XYSeries("demand");      
        XYSeriesCollection dataSet = new XYSeriesCollection();  
        for(int i=1; i<=288; i++){
        	timeSeries.add((double)(i*5), demand.get(i-1));
        }
        
        dataSet.addSeries(timeSeries); 
        String outPath = "D:\\ProgramFiles\\java\\workspace\\VMP\\results\\timeSeries\\";
        draw(dataSet, outPath, "demand_t", "t/(mins)", "demand");        
  	}
    /**
     * 根据时序数据画图时间序列图
     * @param outPath
     * @param demand
     * @param index 图的索引
     */
    public static void drawTimeSeries(double[] demand){
		//根据需求画出时间序列图
		XYSeries timeSeries = new XYSeries("demand");      
        XYSeriesCollection dataSet = new XYSeriesCollection();  
        for(int i=1; i<=demand.length; i++){
        	timeSeries.add((double)(i*5), demand[i-1]);
        }
        double[] xAxis = {0, 1440};
        double[] yAxis = {MathUtils.getMin(demand), MathUtils.getMax(demand)};
        
        dataSet.addSeries(timeSeries); 
        //String outPath = "D:\\ProgramFiles\\java\\workspace\\VMP\\results\\timeSeries\\";
       /* draw(dataSet,
    		"demand_t",
    		"t/(mins)", "demand",
    		xAxis, yAxis);*/        
  	}
    /**
     * 根据数据集画图
     * @param Dataset
     * @param pictureName
     */
	public static void draw(XYDataset Dataset,
			String pictureName, 
			String xLabel, String yLabel,
			double[] xRange, double[] yRange) {
		
			//设置图像风格
			StandardChartTheme ChartTheme = new StandardChartTheme("CN");
			
			ChartTheme.setLargeFont(new Font("宋体", Font.PLAIN, 15));
			ChartTheme.setExtraLargeFont(new Font("宋体", Font.PLAIN, 15));
			ChartTheme.setRegularFont(new Font("宋体", Font.PLAIN, 15));
			ChartFactory.setChartTheme(ChartTheme);	
			
			//创建图形
			JFreeChart chart = ChartFactory.createXYLineChart(
			pictureName,       //图名字
			xLabel,   //横坐标
			yLabel,   //纵坐标
			Dataset,//数据集
			PlotOrientation.VERTICAL,
			false, // 显示图例
			true, // 采用标准生成器 
			false);// 不生成超链接
			
			//自定义图例
			LegendTitle legend = new LegendTitle(chart.getPlot());//创建图例
			legend.setBorder(0.5,0.5,0.5,0.5);//设置四周的边距，带线框. （不显示边框）
			legend.setPosition(RectangleEdge.RIGHT);//设置图例的位置
			legend.setMargin(0,0,50,10);//这样就只是距离右边有距离 margin 5
			chart.addLegend(legend);//图表中添加图例 
			
			// 使用CategoryPlot设置各种参数。以下设置可以省略。      
			XYPlot plot = (XYPlot) chart.getPlot();      
			// 背景色 透明度      
			plot.setBackgroundAlpha(0.5f);      
			// 前景色 透明度      
			plot.setForegroundAlpha(0.5f);
			//x轴y轴范围
			NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
			xAxis.setRange(xRange[0], xRange[1]);
			NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
			yAxis.setRange(yRange[0], yRange[1]);
			
			//设置曲线是否显示数据点
			XYLineAndShapeRenderer xylineandshaperenderer = (XYLineAndShapeRenderer)plot.getRenderer();
			xylineandshaperenderer.setBaseShapesVisible(true);
			xylineandshaperenderer.setSeriesShapesVisible(0, true);
			xylineandshaperenderer.setSeriesShapesVisible(1, true);
			
			//设置曲线显示各数据点的值
			XYItemRenderer xyitem = plot.getRenderer();  
			xyitem.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_LEFT));
			xyitem.setBaseItemLabelGenerator(new StandardXYItemLabelGenerator());
			xyitem.setBaseItemLabelFont(new Font("Dialog", 1, 140));
			xyitem.setBaseItemLabelsVisible(false);
			plot.setRenderer(xyitem);//将修改后的属性值保存到图中
			
			//图边框
			ChartFrame chartFrame = new ChartFrame("ChartFrame", chart);
			chartFrame.pack();
			chartFrame.setVisible(false);
	  }
}


