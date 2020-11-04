package com.lind.avtiviti.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ProcessNodeVo {
    /**
     * nodeId.
     */
    private String nodeId;
    /**
     * 节点名称.
     */
    private String title;
    /**
     * 节点分配人.
     */
    private String assignee;
    /**
     * 是否显示驳回.
     */
    private Integer rejectFlag;
}