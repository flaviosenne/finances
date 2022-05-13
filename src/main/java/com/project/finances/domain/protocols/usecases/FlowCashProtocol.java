package com.project.finances.domain.protocols.usecases;

import com.project.finances.domain.entity.Release;
import com.project.finances.app.usecases.release.dto.ReleaseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;


public interface FlowCashProtocol {

    ReleaseDto createRelease(ReleaseDto release, String userId);

    void deleteRelease(String id, String userId);

    ReleaseDto updateRelease(ReleaseDto release, String userId);

    Page<Release> listReleases(String userId, Specification specification, Pageable pageable);
}