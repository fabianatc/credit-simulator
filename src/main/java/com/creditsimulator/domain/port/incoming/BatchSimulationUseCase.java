package com.creditsimulator.domain.port.incoming;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public interface BatchSimulationUseCase {
    UUID process(MultipartFile file, String requesterName, String requesterEmail) throws IOException;
}
