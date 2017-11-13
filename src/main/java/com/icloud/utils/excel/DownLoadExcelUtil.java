package com.icloud.utils.excel;

import com.icloud.utils.DateUtil;
import org.apache.poi.hssf.usermodel.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Excel导出工具类
 * 一个实体类对应一个sheet
 * 未对应一个实体类对应多个sheet的情况
 */
public class DownLoadExcelUtil {

    public <T> void downLoadExcel(HttpServletResponse response,
                                  final String fileName,
                                  final List<T> list) throws IOException {

        response.setContentType("text/html;charset=UTF-8");
        response.setContentType("application/x-excel");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename="+new String(fileName.getBytes("UTF-8"),"iso-8859-1"));
        getExcel(list,response.getOutputStream());

    }

    public <T>  void getExcel(final List<T> list, OutputStream outputStream) throws IOException {

        //创建workbook
        HSSFWorkbook wb = new HSSFWorkbook();

        //创建sheet
        HSSFSheet sheet = wb.createSheet("sheet");

        //创建表格头行
        creatHeadRow(wb, sheet, list);

        //插入数据
        createBodyRow(wb, sheet, list);

        //输出Excle
        wb.write(outputStream);
    }

    private <T> void creatHeadRow(HSSFWorkbook wb, HSSFSheet sheet, final List<T> list){

        //创建表格头行
        HSSFRow row = null;

        HSSFCell cell = null;

        List<Cell> cellList = getCells(list);

        HSSFCellStyle style = wb.createCellStyle();

        boolean flag = true;

        /**
         * 通过注解设置的index和cellName
         * 在excel对应的列设置名称
         */
        for(Cell c : cellList){
            if(flag){
                flag = false;
                /**
                 * 新建一个sheet
                 * 如果设置的sheetName为空则采用默认值sheet1
                 */
                wb.setSheetName(0, c.sheetName());
                row = sheet.createRow(0);

                style.setAlignment(HSSFCellStyle.ALIGN_CENTER);  //居中
            }
            cell = row.createCell(c.index());
            cell.setCellValue(c.cellName());
            cell.setCellStyle(style);
        }
    }

    private <T> void createBodyRow(HSSFWorkbook wb, HSSFSheet sheet, final List<T> list){

        Object[] objects = null;
        HSSFRow row = null;
        int i = 1;
        int j = 0;

        /**
         * 循环插入数据
         */
        for(Object obj : list){

            objects = getValue(obj);

            /**
             *通过反射获取对应属性的值，
             * 放入Object数组，数组的索引对应Cell的索引
             * 如果值为空则插入null，不为空调用toString插入值
             */
            if(objects.length > 0){
                row = sheet.createRow(i);
                for(Object o : objects){
                    if(o == null){
                        row.createCell(j).setCellValue("");
                    }else {
                        row.createCell(j).setCellValue(
                                o instanceof Date
                                ? DateUtil.format((Date) o, "yyyy-MM-dd HH:mm:ss")
                                : o.toString());
                    }
                    j++;
                }
                j=0;
            }
            i++;
        }
    }

    private <T> List<Cell> getCells(final List<T> list){

        Class<?> clazz = list.get(0).getClass();

        Field[] field = clazz.getDeclaredFields();

        List<Cell> cellList = new ArrayList<Cell>();

        for(Field f : field){
            Cell cell = f.getAnnotation(Cell.class);
            if(cell != null){
                cellList.add(cell);
            }
        }
        return cellList;
    }

    /**
     * 使用一个Object数组来存储当前实体属性的值
     * 注解使用次数最多与该实体的属性数量相同，故Object数组的长度就是属性数组的长度
     * 通过是否使用注解来取值，存储的数组索引是该属性对应的Cell注解index的值，
     * 在插入时可与已生成表格头相对应
     * @param obj
     * @return
     */
    private Object[] getValue(Object obj){

        Field[] fields = obj.getClass().getDeclaredFields();
        Object[] objects = new Object[fields.length];
        boolean access = true;
        for (Field field : fields)
        {
            try {
                access = field.isAccessible();
                if(!access) field.setAccessible(true);
                Cell cell = field.getAnnotation(Cell.class);
                if(cell != null) {
                    objects[cell.index()] = field.get(obj);
                }
                if(!access) field.setAccessible(false);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return objects;
    }
}
