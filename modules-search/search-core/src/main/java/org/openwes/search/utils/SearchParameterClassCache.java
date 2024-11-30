package org.openwes.search.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
public class SearchParameterClassCache {

    /**
     * the name of the class
     */
    private String className;
    private Class<?> classObject;

    /**
     * if there is more than one class with the same name, this will be used to identify the class
     */
    private int classVersion;

    public SearchParameterClassCache(String className, Class<?> classObject) {
        this.className = className;
        this.classObject = classObject;
    }

    /**
     * if the class is detached from the classpath
     */
    private boolean detached;

    public void detach() {
        this.detached = true;
    }

}
