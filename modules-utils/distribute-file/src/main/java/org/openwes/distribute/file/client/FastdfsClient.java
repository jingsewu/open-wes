package org.openwes.distribute.file.client;

import com.github.tobato.fastdfs.domain.fdfs.MetaData;
import com.github.tobato.fastdfs.domain.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class FastdfsClient {

    private final FastFileStorageClient storageClient;

    public byte[] download(String filePath) {
        DownloadByteArray callback = new DownloadByteArray();
        String group = filePath.substring(0, filePath.indexOf("/"));
        String path = filePath.substring(filePath.indexOf("/") + 1);
        return storageClient.downloadFile(group, path, callback);
    }

    public String updateFile(InputStream inputStream, long fileSize, String fileExtName, Set<MetaData> metaDataSet) {
        return storageClient.uploadFile(inputStream, fileSize, fileExtName, metaDataSet).getFullPath();
    }
}
