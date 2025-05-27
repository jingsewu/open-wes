package org.openwes.plugin.sdk.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openwes.distribute.file.client.FastdfsClient;
import org.openwes.plugin.api.IPluginApi;
import org.openwes.plugin.api.constants.TenantPluginStatusEnum;
import org.openwes.plugin.api.dto.PluginManageDTO;
import org.openwes.plugin.api.dto.TenantPluginConfigDTO;
import org.openwes.plugin.sdk.domain.entity.TenantPlugin;
import org.openwes.plugin.sdk.domain.repository.TenantPluginRepository;
import org.openwes.plugin.sdk.domain.service.TenantPluginService;
import org.openwes.plugin.sdk.domain.transfer.TenantPluginConfigTransfer;
import org.pf4j.AbstractPluginManager;
import org.pf4j.PluginManager;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PluginApiImpl implements IPluginApi {

    private static final String JAR = ".jar";

    private final PluginManager pluginManager;
    private final FastdfsClient fastdfsClient;
    private final TenantPluginRepository tenantPluginRepository;
    private final TenantPluginService tenantPluginService;
    private final TenantPluginConfigTransfer tenantPluginConfigTransfer;

    public void install(PluginManageDTO pluginManageDTO) throws IOException {

        TenantPlugin tenantInstallPlugin = TenantPlugin.builder()
                .pluginUniqueKey(pluginManageDTO.getPluginUniqueKey())
                .status(TenantPluginStatusEnum.STARTED)
                .build();
        tenantPluginRepository.save(tenantInstallPlugin);

        Path path = Paths.get(generateLocalFilePath(pluginManageDTO.getPluginUniqueKey(), pluginManageDTO.getVersion()));

        //0. check whether plugin exist first
        String pluginId = pluginManager.loadPlugin(path);
        if (pluginId == null) {
            //1. download file from  file server
            String installFilePath = downloadJar(pluginManageDTO, pluginManageDTO.getPluginJarPath());

            //2. install plugin
            pluginId = pluginManager.loadPlugin(Paths.get(installFilePath));
        }

        if (pluginId == null) {
            throw new IllegalArgumentException("plugin: " + path + " can not be found");
        }

        pluginManager.startPlugin(pluginId);
    }

    private String downloadJar(PluginManageDTO pluginManageDTO, String remoteFilePath) throws IOException {

        if (StringUtils.isEmpty(remoteFilePath)) {
            return remoteFilePath;
        }

        byte[] fileBytes = fastdfsClient.download(remoteFilePath);

        String localFilePath = generateLocalFilePath(pluginManageDTO.getPluginUniqueKey(), pluginManageDTO.getVersion());
        File file = createFile(localFilePath);
        FileUtils.writeByteArrayToFile(file, fileBytes);
        return file.getAbsolutePath();
    }

    private String generateLocalFilePath(String pluginId, String version) {
        String fileName = pluginId + "-" + version + JAR;
        return Paths.get(getPluginDir() + File.separator + fileName).toAbsolutePath().toString();
    }

    private String getPluginDir() {
        return (pluginManager.isDevelopment() ? AbstractPluginManager.DEVELOPMENT_PLUGINS_DIR : AbstractPluginManager.DEFAULT_PLUGINS_DIR);
    }

    private static File createFile(String path) throws FileSystemException {

        try {
            File file = new File(path);
            if (file.exists()) {
                return file;
            } else {
                File parentFile = file.getParentFile();
                if (!parentFile.exists() && !parentFile.mkdirs()) {
                    throw new FileNotFoundException("file: " + parentFile.getAbsolutePath() + "create failed.");
                } else if (file.createNewFile()) {
                    return file;
                } else {
                    throw new FileSystemException("file: " + path + " create error");
                }
            }
        } catch (Exception e) {
            log.error("create file error: ", e);
            throw new FileSystemException("file: " + path + " create error");
        }
    }

    @Override
    public List<String> getStartedTenantPluginIds() {
        return tenantPluginService.getStartedTenantPluginIds();
    }

    @Override
    public TenantPluginConfigDTO getPluginConfig(String pluginUniqueKey) {
        return tenantPluginConfigTransfer.toDTO(tenantPluginService.get(pluginUniqueKey));
    }

    @Override
    public TenantPluginConfigDTO getPluginConfig(String tenantId, String pluginUniqueKey) {
        return tenantPluginConfigTransfer.toDTO(tenantPluginService.get(pluginUniqueKey));
    }

}
