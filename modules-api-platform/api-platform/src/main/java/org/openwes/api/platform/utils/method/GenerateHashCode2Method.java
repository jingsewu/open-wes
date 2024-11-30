package org.openwes.api.platform.utils.method;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class GenerateHashCode2Method implements TemplateMethodModelEx {

    @Override
    public Integer exec(List arguments) throws TemplateModelException {
        if (CollectionUtils.isEmpty(arguments)) {
            return null;
        }
        try {
            String str = "";
            for (Object o : arguments) {
                str += String.valueOf(o);
            }
            return str.hashCode();
        } catch (Exception e) {
            throw new TemplateModelException("GenerateHashCode2 exception");
        }
    }
}
