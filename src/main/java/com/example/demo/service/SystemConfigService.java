package com.example.demo.service;

import com.example.demo.entity.SystemConfig;
import java.util.List;

public interface SystemConfigService {

    List<SystemConfig> getAllConfigs();

    SystemConfig getConfigByKey(String configKey);

    SystemConfig updateConfig(String configKey, String configValue, String description, String adminUsername);

    SystemConfig createConfig(String configKey, String configValue, String description, String adminUsername);

    void deleteConfig(String configKey, String adminUsername);
}
