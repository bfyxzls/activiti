package com.lind.avtiviti.repository;

import com.lind.avtiviti.entity.ActReNode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActReNodeRepository extends JpaRepository<ActReNode, String> {
    ActReNode findByNodeIdAndProcessDefId(String nodeId, String processDefId);

    void removeByNodeIdAndProcessDefId(String nodeId, String processDefId);
}
