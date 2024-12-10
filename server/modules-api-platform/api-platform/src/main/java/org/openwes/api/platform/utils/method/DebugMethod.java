package org.openwes.api.platform.utils.method;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class DebugMethod implements TemplateMethodModelEx {

    @Override
    public Object exec(List arguments) throws TemplateModelException {
        return "";
    }

}
