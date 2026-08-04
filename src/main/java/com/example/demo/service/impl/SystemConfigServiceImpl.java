package com.example.demo.service.impl;

import com.example.demo.entity.SystemConfig;
import com.example.demo.events.SystemConfigUpdatedEvent;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.producer.EventPublisher;
import com.example.demo.repository.SystemConfigRepository;
import com.example.demo.service.SystemConfigService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;
    private final EventPublisher eventPublisher;

    public SystemConfigServiceImpl(SystemConfigRepository systemConfigRepository, EventPublisher eventPublisher) {
        this.systemConfigRepository = systemConfigRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SystemConfig> getAllConfigs() {
        return systemConfigRepository.findAll();
    }

    @Override
    @Cacheable(value = "systemConfigs", key = "#configKey")
    @Transactional(readOnly = true)
    public SystemConfig getConfigByKey(String configKey) {
        return systemConfigRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new ResourceNotFoundException("Config not found: " + configKey));
    }

    @Override
    @CacheEvict(value = "systemConfigs", key = "#configKey")
    @Transactional
    public SystemConfig updateConfig(String configKey, String configValue, String description, String adminUsername) {
        SystemConfig config = systemConfigRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new ResourceNotFoundException("Config not found: " + configKey));
        config.setConfigValue(configValue);
        if (description != null) {
            config.setDescription(description);
        }
        SystemConfig saved = systemConfigRepository.save(config);
        eventPublisher.publishSystemConfigUpdated(new SystemConfigUpdatedEvent(configKey, configValue, adminUsername));
        return saved;
    }

    @Override
    @Transactional
    public SystemConfig createConfig(String configKey, String configValue, String description, String adminUsername) {
        if (systemConfigRepository.findByConfigKey(configKey).isPresent()) {
            throw new IllegalArgumentException("Config key already exists: " + configKey);
        }
        SystemConfig config = new SystemConfig(configKey, configValue, description);
        SystemConfig saved = systemConfigRepository.save(config);
        eventPublisher.publishSystemConfigUpdated(new SystemConfigUpdatedEvent(configKey, configValue, adminUsername));
        return saved;
    }

    @Override
    @CacheEvict(value = "systemConfigs", key = "#configKey")
    @Transactional
    public void deleteConfig(String configKey, String adminUsername) {
        SystemConfig config = systemConfigRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new ResourceNotFoundException("Config not found: " + configKey));
        systemConfigRepository.delete(config);
    }
}
