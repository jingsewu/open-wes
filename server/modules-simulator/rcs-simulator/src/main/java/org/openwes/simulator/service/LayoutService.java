package org.openwes.simulator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.openwes.simulator.domain.WarehouseLayout;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
public class LayoutService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private WarehouseLayout currentLayout;

    public WarehouseLayout loadFromClasspath(String path) {
        try {
            Resource resource = new ClassPathResource(path);
            return loadFromStream(resource.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load layout from classpath: " + path, e);
        }
    }

    public WarehouseLayout loadFromFile(String path) {
        try {
            Resource resource = new FileSystemResource(path);
            return loadFromStream(resource.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load layout from file: " + path, e);
        }
    }

    private WarehouseLayout loadFromStream(InputStream stream) throws IOException {
        WarehouseLayout layout = objectMapper.readValue(stream, WarehouseLayout.class);
        layout.buildLocationIndex();
        return layout;
    }

    public WarehouseLayout load(String locationSpec) {
        if (locationSpec.startsWith("classpath:")) {
            currentLayout = loadFromClasspath(locationSpec.substring("classpath:".length()));
        } else {
            currentLayout = loadFromFile(locationSpec);
        }
        log.info("Loaded warehouse layout: {}x{}, {} shelves, {} workstations, {} robots",
                currentLayout.getWarehouse().getWidth(),
                currentLayout.getWarehouse().getHeight(),
                currentLayout.getShelves().size(),
                currentLayout.getWorkstations().size(),
                currentLayout.getRobots().size());
        return currentLayout;
    }

    public WarehouseLayout getCurrentLayout() {
        return currentLayout;
    }

    public void updateLayout(WarehouseLayout layout) {
        layout.buildLocationIndex();
        this.currentLayout = layout;
        log.info("Updated warehouse layout");
    }
}
