package org.openwes.api.platform.utils;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateModelException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openwes.api.platform.api.constants.MethodConstant;
import org.openwes.api.platform.api.constants.TemplateConstant;
import org.openwes.api.platform.api.exception.TemplateBizException;
import org.openwes.api.platform.api.exception.error_code.ApiPlatformErrorCodeEnum;
import org.openwes.api.platform.utils.method.*;
import org.openwes.common.utils.constants.MarkConstant;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class FreeMarkerHelper {

    private static final Configuration cfg = new Configuration(Configuration.VERSION_2_3_30);
    private static final Map<String, Object> iwmsSharedVariable = new HashMap<>();

    {
        //默认配置
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        //数字格式，防止数字被自动转换成xx,xxx格式
        cfg.setNumberFormat(MarkConstant.POUND_KEY);

        try {
            putIwmsSharedVariable();
        } catch (TemplateModelException e) {
            log.error("TemplateHelper#init. Failed to set shared variable (method, constant)!", e);
        } catch (Exception e) {
            log.error("TemplateHelper#init. Not found template path.", e);
        }
    }

    public static String convertByTemplate(byte[] templateFileBytes, Object dataObj, Map<String, Object>... otherParam) {
        Map<String, Object> dataMap = new HashMap<>();
        //主数据
        dataMap.put(TemplateConstant.DATA_OBJECT, dataObj);
        //其它数据
        if (otherParam != null) {
            for (Map<String, Object> obj : otherParam) {
                dataMap.putAll(obj);
            }
        }

        //最顶级统一变量名为root
        Map<String, Object> paras = new HashMap<>();
        paras.put(TemplateConstant.DATA_ROOT, dataMap);

        String result = null;
        try (StringWriter out = new StringWriter()) {
            Template temp;
            //模板数据模式
            StringTemplateLoader stringLoader = new StringTemplateLoader();
            //加载模板数据，如db等
            stringLoader.putTemplate("dataTemplate", new String(templateFileBytes, StandardCharsets.UTF_8));
            cfg.setTemplateLoader(stringLoader);
            temp = cfg.getTemplate("dataTemplate");
            temp.process(paras, out);
            result = out.toString();
        } catch (Exception e) {
            log.error("TemplateHelper#convertByTemplate error.", e);
            if (e instanceof TemplateBizException templateException) {
                AssertUtils.throwTemplateBizException(templateException);
            }
            AssertUtils.throwBizException(ApiPlatformErrorCodeEnum.API_TEMPLATE_PARSE_ERROR, e.getMessage());
        }

        return result;
    }

    /**
     * 上传自定义模板文件
     *
     * @param templateFile 模板文件
     * @return 返回文件名
     */
    public static String uploadTemplate(MultipartFile templateFile) throws Exception {
        // 获取文件的后缀名
        String fileName = templateFile.getOriginalFilename();
        String suffixName = fileName.substring(fileName.lastIndexOf(MarkConstant.FULL_STOP_MARK) + 1);
        try {
            //检查文件格式是否合法
            if (StringUtils.isEmpty(suffixName) || !TemplateConstant.TEMPLATE_SUFFIX_NAME.equalsIgnoreCase(suffixName)) {
                AssertUtils.throwBizException(ApiPlatformErrorCodeEnum.API_API_TYPE_ERROR, "Illegal type, please upload .ftl");
            }
            //兼容：判断指定挂载目录
            String templatePath = TemplateConstant.TEMPLATE_PATH;
            File file = FileUtils.getFile(templatePath + fileName);
            FileUtils.writeByteArrayToFile(file, templateFile.getBytes());
            return file.getPath();
        } catch (Exception e) {
            log.error("TemplateHelper#updateTemplate error.fileName:[{}]", fileName, e);
            throw e;
        }
    }

    /**
     * 移除自定义模板文件
     *
     * @param templateName 模板名称
     * @return
     */
    public static boolean removeTemplate(String templateName) {
        //兼容：判断指定挂载目录
        //删除persistence目录下的模板
        File file = FileUtils.getFile(TemplateConstant.TEMPLATE_PATH + templateName);
        return file.delete();
    }

    /**
     * 模板共享变量
     */
    private void putIwmsSharedVariable() throws TemplateModelException {
        // methods
        //自定义函数：时间戳
        iwmsSharedVariable.put(MethodConstant.METHOD_TIMESTAMP, new TimestampMethod());
        //自定义函数：参数校验，校验不通过抛出异常
        iwmsSharedVariable.put(MethodConstant.METHOD_VALIDATE, new ValidateMethod());
        //自定义函数：抛出异常
        iwmsSharedVariable.put(MethodConstant.METHOD_THROWEXCEPTION, new ThrowExceptionMethod());
        //自定义函数：json转换，输出整个json内容
        iwmsSharedVariable.put(MethodConstant.METHOD_TO_JSON, new ToJsonMethod());
        //自定义函数：列表分组汇总部分字段，返回List
        iwmsSharedVariable.put(MethodConstant.METHOD_GROUP_BY, new GroupByMethod());
        //自定义函数：列表分组，返回Map<String, List<Object>>
        iwmsSharedVariable.put(MethodConstant.METHOD_GROUP_BY_TO_MAP, new GroupByToMapMethod());
        //自定义函数：json的key转小写
        iwmsSharedVariable.put(MethodConstant.METHOD_JSONKEYS_TO_LOWERCASE, new JsonKeysToLowerCaseMethod());
        //自定义函数：列表分组统计记录数，返回int
        iwmsSharedVariable.put(MethodConstant.METHOD_GROUP_COUNT, new GroupCountMethod());
        //自定义函数：生成请求唯一号，返回long
        iwmsSharedVariable.put(MethodConstant.METHOD_TO_MAP, new ToMapMethod());
        //自定义函数：生成hashCode 返回int
        iwmsSharedVariable.put(MethodConstant.HASH_CODE, new GenerateHashCodeMethod());

        iwmsSharedVariable.put(MethodConstant.HASH_CODE2, new GenerateHashCode2Method());
        //自定义函数：检查数据格式，返回string
        iwmsSharedVariable.put(MethodConstant.DATA_TYPE, new DataTypeMethod());
        //自定义函数：对象转string，返回string
        iwmsSharedVariable.put(MethodConstant.TO_STRING, new ToStringMethod());
        // 自定义函数：debug，用于断点调试flt模板，返回空字符串
        iwmsSharedVariable.put(MethodConstant.DEBUG, new DebugMethod());
        // 自定义函数：去重并分组统计数量，返回Map<String, Long>
        iwmsSharedVariable.put(MethodConstant.DISTINCT_GROUP_BY_COUNT, new DistinctGroupByCountMethod());
        // constants
        // 常量：参数校验
        iwmsSharedVariable.put(MethodConstant.CONSTANT_PATTERN_NOT_EMPTY, ValidateMethod.PATTERN_NOT_EMPTY);
        iwmsSharedVariable.put(MethodConstant.CONSTANT_PATTERN_NUMBER, ValidateMethod.PATTERN_NUMBER);
        iwmsSharedVariable.put(MethodConstant.CONSTANT_PATTERN_DATE, ValidateMethod.PATTERN_DATE);
        iwmsSharedVariable.put(MethodConstant.CONSTANT_PATTERN_REG, ValidateMethod.PATTERN_REG);

        // 设置iwms模板共享变量
        cfg.setSharedVariable(TemplateConstant.IWMS_SHARED_VARIABLE, iwmsSharedVariable);
    }
}
