package org.openwes.common.imp.annotation;

import cn.afterturn.easypoi.handler.inter.IExcelDataHandler;
import org.openwes.common.imp.ImportService;
import org.openwes.common.imp.ExcelSymbol;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Importable {

    /**
     * 表示注释的类生成的导入模版的唯一标识
     * 示例：
     *      international_entry: 词条导入模版的名称
     *      position: 库位导入模版
     * @return
     */
    ExcelSymbol symbol();

    /**
     * 自定义的数据处理器，在导入的时候会自动的调用该接
     * 口的 importHandler 方法对每一条数据进行处理
     * @return
     */
    Class<? extends IExcelDataHandler> dataHandler();

    /**
     * 提供数据的 Service
     * @return
     */
    Class<? extends ImportService> service();
}
