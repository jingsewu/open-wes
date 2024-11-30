package org.openwes.common.utils.exception;

import org.openwes.common.utils.exception.code_enum.IBaseError;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.text.MessageFormat;
import java.util.function.Supplier;

/**
 * wms exception
 *
 * @author sws
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WmsException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -8192643659977363045L;

    /**
     * 代码
     */
    private String code;

    /**
     * 消息
     */
    private String message;

    private Object[] args;

    private IBaseError baseError;

    public WmsException(String message) {
        this.message = message;
    }

    public WmsException(String message, Object... args) {
        this.message = message;
        this.args = args;
    }

    public WmsException(IBaseError baseError) {
        this.baseError = baseError;
        this.code = baseError.getCode();
        this.message = baseError.getDesc();
    }

    public WmsException(IBaseError baseError, Object[] args) {
        this.baseError = baseError;
        this.code = baseError.getCode();
        this.args = args;
        this.message = MessageFormat.format(baseError.getDesc(), args);
    }

    public String getCode() {
        if (code == null && baseError != null) {
            code = baseError.getCode();
        }
        return code;
    }

    /**
     * 抛出wms异常
     *
     * @param baseError 基本错误
     * @return {@link WmsException}
     */
    public static WmsException throwWmsException(IBaseError baseError) {
        return new WmsException(baseError);
    }

    /**
     * 抛出wms异常，带有具体字段错误信息
     *
     * @param baseError 基本错误
     * @param args      具体参数
     * @return {@link WmsException}
     */
    public static WmsException throwWmsException(IBaseError baseError, Object... args) {
        return new WmsException(baseError, args);
    }

    /**
     * 抛出wms异常
     *
     * @param baseError 基本错误
     * @return {@link Supplier}<{@link WmsException}>
     */
    public static Supplier<WmsException> throwWmsExceptionSup(IBaseError baseError) {
        return () -> new WmsException(baseError);
    }

    /**
     * 抛出wms异常,带有具体字段错误信息
     *
     * @param baseError 基本错误
     * @param args      参数
     * @return {@link Supplier}<{@link WmsException}>
     */
    public static Supplier<WmsException> throwWmsExceptionSup(IBaseError baseError, Object... args) {
        return () -> new WmsException(baseError, args);
    }

}
