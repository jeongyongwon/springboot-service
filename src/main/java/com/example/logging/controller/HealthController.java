package com.example.logging.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {
    
    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);
    private final long startTime = System.currentTimeMillis();
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            long uptime = (System.currentTimeMillis() - startTime) / 1000;
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            long usedMemory = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
            long maxMemory = memoryBean.getHeapMemoryUsage().getMax() / (1024 * 1024);
            
            health.put("status", "healthy");
            health.put("uptime_seconds", uptime);
            health.put("timestamp", Instant.now().toString());
            
            Map<String, Object> memory = new HashMap<>();
            memory.put("used_mb", usedMemory);
            memory.put("max_mb", maxMemory);
            health.put("memory", memory);
            
            logger.info("Health check performed - status: healthy, uptime: {}s", uptime);
            
            return ResponseEntity.ok(health);
        } catch (Exception ex) {
            logger.error("Health check failed", ex);
            health.put("status", "unhealthy");
            health.put("error", ex.getMessage());
            return ResponseEntity.status(503).body(health);
        }
    }
}
