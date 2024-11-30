package org.openwes.common.imp;

import java.util.List;

public interface ImportService<T> {

    void doImport(List<T> data);
}
