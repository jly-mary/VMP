package utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import entities.VM;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/** 
* @version 创建时间. 2017年3月3日 下午4. 03. 18 
* 创建excel表格
*/
public class XlsUtils  {
	
	public void testCreateExcel(){
		String[] titles={"Time","AllocatedVmAmount"};
		ArrayList<Integer> result = new ArrayList<>();
		for(int i=1; i<=10; i++){
			result.add(i);
		}
		String resultName = "AAA";
		try {
			createExcel(titles, result, resultName);
		} catch (RowsExceededException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    public static <T> void createExcel(String[] titles, ArrayList<T> result, String resultName) 
            throws IOException, RowsExceededException, WriteException {
        //1. 创建excel文件
        File file=new File("D:\\ProgramFiles\\java\\workspace\\VMP\\results\\"
        					+"result"+ resultName +".xls");
        file.createNewFile();
        
        //2. 创建工作簿
        WritableWorkbook workbook=Workbook.createWorkbook(file);
        
        //3. 创建sheet,设置第二三四..个sheet，依次类推即可
        WritableSheet sheet=workbook.createSheet(resultName, 0);
        
        //4. 设置titles
        //String[] titles={"Time","AllocatedVmAmount"};
        
        //5. 单元格
        Label label=null;
        
        //6. 给第一行设置列名
        for(int i=0;i<titles.length;i++){
            //x,y,第一行的列名
            label=new Label(i,0,titles[i]);
            //7. 添加单元格
            sheet.addCell(label);
        }
        
        //8. 模拟数据库导入数据
        for(int i=1; i<=result.size(); i++){
        	//添加Time，第一列 第二行
        	label=new Label(0, i, i+"");
        	sheet.addCell(label);
        	
        	//添加AllocatedVmAmount， 第二列第二行
        	label=new Label(1, i, result.get(i-1)+"");
        	sheet.addCell(label);
        }
        //9. 写入数据，一定记得写入数据，不然你都开始怀疑世界了，excel里面啥都没有
        workbook.write();
        
        //10. 最后一步，关闭工作簿
        workbook.close();
    }
    
    public static <T> void createExcel(String filePath, String fileName, String[] titles, 
    		ArrayList<T> resultMain, 
    		ArrayList<T> resultVice) 
            throws IOException, RowsExceededException, WriteException {
        //1. 创建excel文件
        /*File file=new File("D:\\ProgramFiles\\java\\workspace\\VMP\\results\\"
        					+"result"+ fileName +".xls");*/
    	/*File dir = new File(filePath);
    	dir.mkdirs();*/
    	
        File file=new File(filePath + "\\" + fileName +".xls");
        file.createNewFile();
        
        //2. 创建工作簿
        WritableWorkbook workbook=Workbook.createWorkbook(file);
        
        //3. 创建sheet,设置第二三四..个sheet，依次类推即可
        WritableSheet sheet=workbook.createSheet(fileName, 0);
        
        //4. 设置titles
        //String[] titles={"Time","AllocatedVmAmount"};
        
        //5. 单元格
        Label label=null;
        
        //6. 给第一行设置列名
        for(int i=0;i<titles.length;i++){
            //x,y,第一行的列名
            label=new Label(i,0,titles[i]);
            //7. 添加单元格
            sheet.addCell(label);
        }
        
        //8. 模拟数据库导入数据
        for(int i=1; i<=resultMain.size(); i++){
        	//添加Time，第一列 第二行
        	label=new Label(0, i, i+"");
        	sheet.addCell(label);
        	
        	//添加AllocatedVmAmount， 第二列第二行
        	label=new Label(1, i, resultMain.get(i-1)+"");
        	sheet.addCell(label);
        	
        	//添加AllocatedVmAmount， 第二列第二行
        	label=new Label(2, i, resultVice.get(i-1)+"");
        	sheet.addCell(label);
        	
        }
        //9. 写入数据，一定记得写入数据，不然你都开始怀疑世界了，excel里面啥都没有
        workbook.write();
        
        //10.最后一步，关闭工作簿
        workbook.close();
    }
    
    public static <T> void createExcel(String filePath, String fileName, String[] titles, 
    		ArrayList<T>... results)
            throws IOException, RowsExceededException, WriteException {
    	//比较几种结果
    	int resNum = results.length;
        //1. 创建excel文件
        File file=new File(filePath + "\\" + fileName +".xls");
        file.createNewFile();
        
        //2. 创建工作簿
        WritableWorkbook workbook=Workbook.createWorkbook(file);
        
        //3. 创建sheet,设置第二三四..个sheet，依次类推即可
        WritableSheet sheet=workbook.createSheet(fileName, 0);
        
        //4. 设置titles
        //String[] titles={"Time","AllocatedVmAmount"};
        
        //5. 单元格
        Label label=null;
        
        //6. 给第一行设置列名
        for(int i=0; i<titles.length; i++){
            //x,y,第一行的列名
            label=new Label(i,0,titles[i]);
            //7. 添加单元格
            sheet.addCell(label);
        }
        
        //8. 从第2行导入数据
        for(int i=1; i<=results[0].size(); i++){
        	//第一列
        	label=new Label(0, i, i+"");
        	sheet.addCell(label);
        	//第2到最后一列
        	for(int j=1; j<=resNum; j++){
        		label = new Label(j , i, results[j-1].get(i-1)+"");
        		sheet.addCell(label);
        	}
        }
        //9. 写入数据，一定记得写入数据
        workbook.write();
        
        //10.最后一步，关闭工作簿
        workbook.close();
    }
    @Test
    public void testCreateXls(){
    	//1. 创建excel文件
    	String fileName = "workload1.xls";
        File file = new File("E:\\files\\M\\workload\\" + fileName);
        try {
			file.createNewFile();
			//2. 创建工作簿
	        WritableWorkbook workbook=Workbook.createWorkbook(file);
	        
	        //3. 创建sheet,设置第二三四..个sheet，依次类推即可
	        WritableSheet sheet = workbook.createSheet(fileName, 0);
	        
	        //5. 单元格
	        Label label=null;
	   
	        VM vm = DataCenterUtils.createVM();
	        List<Double> cpuReList = vm.getVMHistory().get("cpu");

	        //8. 表导入数据
	        for(int i=0; i<cpuReList.size(); i++){
	        	//添加AllocatedVmAmount， 第二列第二行
	        	label=new Label(0, i, cpuReList.get(i)+"");
	        	sheet.addCell(label);
	        }
	        //9. 写入数据，一定记得写入数据，不然你都开始怀疑世界了，excel里面啥都没有
	        workbook.write();
	        
	        //10.最后一步，关闭工作簿
	        workbook.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		}    
    }
}