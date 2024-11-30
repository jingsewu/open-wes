package org.openwes.api.platform.utils.method;

import org.openwes.api.platform.api.exception.TemplateBizException;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

import java.util.List;

public class ThrowExceptionMethod implements TemplateMethodModelEx {

    @Override
    public String exec(List args) throws TemplateModelException {
        throw new TemplateBizException(args.get(0).toString());
    }
}
