package com.lind.avtiviti.repository;

import com.lind.avtiviti.entity.ActReNode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActReNodeRepository extends JpaRepository<ActReNode, String> {
    ActReNode findByNodeIdAAndProcessDefId(String nodeId,String processDefId);
    void deleteActReNodeByNodeIdAndProcessDefId(String nodeId,String processDefId);
}
