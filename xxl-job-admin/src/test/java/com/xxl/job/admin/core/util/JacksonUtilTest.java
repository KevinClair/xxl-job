package com.xxl.job.admin.core.util;

import com.xxl.job.admin.core.model.XxlJobRegistry;
import com.xxl.job.common.enums.RegistryConstants;
import com.xxl.job.common.utils.JacksonUtil;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.xxl.job.common.utils.JacksonUtil.writeValueAsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JacksonUtilTest {

    @Test
    public void shouldWriteValueAsString() {
        //given
        Map<String, String> map = new HashMap<>();
        map.put("aaa", "111");
        map.put("bbb", "222");

        //when
        String json = writeValueAsString(map);

        //then
        assertEquals(json, "{\"aaa\":\"111\",\"bbb\":\"222\"}");
    }

    @Test
    public void shouldReadValueAsObject() {
        //given
        String jsonString = "{\"aaa\":\"111\",\"bbb\":\"222\"}";

        //when
        Map result = JacksonUtil.readValue(jsonString, Map.class);

        //then
        assertEquals(result.get("aaa"), "111");
        assertEquals(result.get("bbb"), "222");

    }

    @Test
    public void testMap() {
        List<XxlJobRegistry> list = new ArrayList<>();
        XxlJobRegistry xxlJobRegistry = new XxlJobRegistry();
        xxlJobRegistry.setRegistryGroup(RegistryConstants.RegistryType.EXECUTOR.name());
        xxlJobRegistry.setRegistryKey("111");
        xxlJobRegistry.setRegistryValue("111");

        list.add(xxlJobRegistry);

        XxlJobRegistry xxlJobRegistry1 = new XxlJobRegistry();
        xxlJobRegistry1.setRegistryGroup(RegistryConstants.RegistryType.EXECUTOR.name());
        xxlJobRegistry1.setRegistryKey("111");
        xxlJobRegistry1.setRegistryValue("111");

        list.add(xxlJobRegistry1);

        XxlJobRegistry xxlJobRegistry11 = new XxlJobRegistry();
        xxlJobRegistry11.setRegistryGroup(RegistryConstants.RegistryType.EXECUTOR.name());
        xxlJobRegistry11.setRegistryKey("111");
        xxlJobRegistry11.setRegistryValue("111");

        list.add(xxlJobRegistry11);

        XxlJobRegistry xxlJobRegistry2 = new XxlJobRegistry();
        xxlJobRegistry2.setRegistryGroup(RegistryConstants.RegistryType.EXECUTOR.name());
        xxlJobRegistry2.setRegistryKey("222");
        xxlJobRegistry2.setRegistryValue("111");

        list.add(xxlJobRegistry2);

        XxlJobRegistry xxlJobRegistry3 = new XxlJobRegistry();
        xxlJobRegistry3.setRegistryGroup(RegistryConstants.RegistryType.ADMIN.name());
        xxlJobRegistry3.setRegistryKey("222");
        xxlJobRegistry3.setRegistryValue("111");

        list.add(xxlJobRegistry3);

        list.stream()
                .filter(each -> RegistryConstants.RegistryType.EXECUTOR.name().equals(each.getRegistryGroup()))
                .collect(Collectors.toMap(XxlJobRegistry::getRegistryKey, registryValues -> {
                    List<String> values = new ArrayList<>();
                    values.add(registryValues.getRegistryValue());
                    return values;
                }, (v1, v2) -> {
                    v1.addAll(v2);
                    return v1;
                }));
    }
}
