package org.openwes.api.platform.utils.method;

import freemarker.template.DefaultListAdapter;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

import java.util.List;

public class DataTypeMethod implements TemplateMethodModelEx {

    @Override
    public String exec(List arguments) throws TemplateModelException {
        String dataType = "";
        Object object = arguments.get(0);
        if (object instanceof DefaultListAdapter) {
            dataType = "list";
        } else {
            dataType = "object";
        }
        return dataType;
    }
}
