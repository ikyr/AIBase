package com.datang.aibase.skill.service;

import com.datang.aibase.skill.entity.SkillDef;
import com.datang.aibase.skill.entity.SkillExecutionLog;
import com.datang.aibase.skill.entity.SkillVersion;

import java.util.List;
import java.util.Map;

public interface SkillService {

    List<SkillDef> listAll();

    SkillDef getById(String id);

    SkillDef create(SkillDef skill);

    SkillDef update(String id, SkillDef skill);

    void delete(String id);

    List<SkillVersion> getVersions(String skillId);

    SkillVersion addVersion(SkillVersion version);

    List<SkillExecutionLog> listExecutionLogs(int limit);

    List<SkillDef> discover(String query);

    Map<String, Object> execute(Map<String, Object> request);
}
