package com.icloud.utils.excel;

import java.lang.annotation.*;

/**
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cell {

    /**
     * 所在sheet表
     * 默认是sheet1
     * @return
     */
    String sheetName() default "sheet1";

    /**
     * 单元格名称
     * @return
     */
    String cellName() default "";

    /**
     * 单元格所在列索引
     * 0对应Excel的A栏
     * @return
     */
    int index() default 0;
}
