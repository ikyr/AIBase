package com.datang.aibase.skill.controller;

import com.datang.aibase.common.dto.ApiResponse;
import com.datang.aibase.skill.entity.SkillDef;
import com.datang.aibase.skill.entity.SkillExecutionLog;
import com.datang.aibase.skill.entity.SkillVersion;
import com.datang.aibase.skill.service.SkillService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/skill")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @GetMapping
    public ApiResponse<List<SkillDef>> listAll() {
        return ApiResponse.ok(skillService.listAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<SkillDef> getById(@PathVariable String id) {
        return ApiResponse.ok(skillService.getById(id));
    }

    @PostMapping
    public ApiResponse<SkillDef> create(@RequestBody SkillDef skill) {
        return ApiResponse.ok(skillService.create(skill));
    }

    @GetMapping("/{id}/versions")
    public ApiResponse<List<SkillVersion>> getVersions(@PathVariable String id) {
        return ApiResponse.ok(skillService.getVersions(id));
    }

    @PostMapping("/{id}/versions")
    public ApiResponse<SkillVersion> addVersion(@PathVariable String id, @RequestBody SkillVersion version) {
        version.setSkillId(id);
        return ApiResponse.ok(skillService.addVersion(version));
    }

    @GetMapping("/logs")
    public ApiResponse<List<SkillExecutionLog>> listLogs(@RequestParam(defaultValue = "50") int limit) {
        return ApiResponse.ok(skillService.listExecutionLogs(limit));
    }

    @PostMapping("/execute")
    public ApiResponse<Map<String, Object>> execute(@RequestBody Map<String, Object> request) {
        return ApiResponse.ok(skillService.execute(request));
    }

    @PostMapping("/execute/{skillId}")
    public ApiResponse<Map<String, Object>> executeById(@PathVariable String skillId, @RequestBody Map<String, Object> input) {
        Map<String, Object> request = new java.util.LinkedHashMap<>();
        request.put("skillId", skillId);
        request.put("context", input);
        return ApiResponse.ok(skillService.execute(request));
    }

    @GetMapping("/discover")
    public ApiResponse<List<SkillDef>> discover(@RequestParam String query) {
        return ApiResponse.ok(skillService.discover(query));
    }
}
