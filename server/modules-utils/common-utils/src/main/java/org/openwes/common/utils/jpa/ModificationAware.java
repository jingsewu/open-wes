package org.openwes.common.utils.jpa;

public interface ModificationAware {
    boolean isModified();

    void setModified(boolean modified);
}
