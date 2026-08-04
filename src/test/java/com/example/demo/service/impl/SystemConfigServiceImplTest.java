package com.example.demo.service.impl;

import com.example.demo.entity.SystemConfig;
import com.example.demo.producer.EventPublisher;
import com.example.demo.repository.SystemConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SystemConfigServiceImplTest {

    @Mock
    private SystemConfigRepository systemConfigRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private SystemConfigServiceImpl systemConfigService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetConfigByKey() {
        SystemConfig config = new SystemConfig("MAX_LIMIT", "1000", "Maximum transaction limit");
        when(systemConfigRepository.findByConfigKey("MAX_LIMIT")).thenReturn(Optional.of(config));

        SystemConfig result = systemConfigService.getConfigByKey("MAX_LIMIT");

        assertNotNull(result);
        assertEquals("1000", result.getConfigValue());
    }

    @Test
    void testUpdateConfig() {
        SystemConfig config = new SystemConfig("MAX_LIMIT", "1000", "desc");
        when(systemConfigRepository.findByConfigKey("MAX_LIMIT")).thenReturn(Optional.of(config));
        when(systemConfigRepository.save(any(SystemConfig.class))).thenReturn(config);

        SystemConfig result = systemConfigService.updateConfig("MAX_LIMIT", "2000", null, "admin");

        assertEquals("2000", result.getConfigValue());
        verify(systemConfigRepository).save(config);
        verify(eventPublisher).publishSystemConfigUpdated(any());
    }
}
