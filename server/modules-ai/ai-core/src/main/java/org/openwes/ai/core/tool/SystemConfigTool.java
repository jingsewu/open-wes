package org.openwes.ai.core.tool;

import lombok.RequiredArgsConstructor;
import org.openwes.wes.api.config.ISystemConfigApi;
import org.openwes.wes.api.config.dto.SystemConfigDTO;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SystemConfigTool implements ITool{

    private final ISystemConfigApi systemConfigApi;

    @Tool(name = "getSystemConfig", description = "Retrieves the current system configuration settings, including basic, EMS, inbound, outbound, stock, and algorithm configurations. The returned {@link SystemConfigDTO} provides a comprehensive snapshot of all configurable system parameters.")
    public SystemConfigDTO getSystemConfig() {
        return systemConfigApi.getSystemConfig();
    }

    @Tool(name = "updateSystemConfig", description = """
        更新系统配置，使用以下JSON结构：
        {
          "basicConfig": {
            "transferContainerReleaseMethod": "AUTOMATED|MANUAL",  // 容器释放方式
            "autoReleaseDelayTimeMin": number                      // 自动释放延迟时间(分钟)
          },
          "inboundConfig": {
            "checkRepeatedCustomerOrderNo": boolean,  // 入库检查重复客户订单号
            "checkRepeatedLpnCode": boolean           // 入库检查重复LPN码
          },
          "outboundConfig": {
            "checkRepeatedCustomerOrderNo": boolean   // 出库检查重复客户订单号
          },
          "stockConfig": {
            "stockAbnormalAutoCreateAdjustmentOrder": boolean,  // 库存异常自动创建调整单
            "zeroStockSavedDays": number                        // 零库存保存天数
          },
          "emsConfig": {
            "allowBatchCreateContainerTasks": boolean           // 允许批量创建容器任务
          },
          "outboundAlgoConfig": {
            "useLocalAlgorithm": boolean,        // 使用本地算法
            "cutoffTime": number,                // 算法超时时间
            "mode": "string",                    // 算法模式
            "orderDispatchStrategy": "string"    // 订单分配策略
          }
        }
        示例：
        - "关闭入库重复订单检查" → {"inboundConfig":{"checkRepeatedCustomerOrderNo":false}}
        - "设置零库存保存天数为3天" → {"stockConfig":{"zeroStockSavedDays":3}}
        - "同时修改容器释放方式和算法超时时间" → {
          "basicConfig":{"transferContainerReleaseMethod":"MANUAL"},
          "outboundAlgoConfig":{"cutoffTime":5000}
        }
        """)
    public void updateSystemConfig(@ToolParam SystemConfigDTO systemConfigDTO) {
        systemConfigApi.update(systemConfigDTO);
    }
}
