CREATE TABLE simulation_job (
    id UUID PRIMARY KEY,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    finished_at TIMESTAMP,
    total_simulations INT NOT NULL DEFAULT 0,
    success_count INT NOT NULL DEFAULT 0,
    error_count INT NOT NULL DEFAULT 0,
    requester_name VARCHAR(255) NOT NULL,
    requester_email VARCHAR(255) NOT NULL
);

CREATE TABLE simulation_result (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL,
    input JSONB,
    output JSONB,
    status VARCHAR(20) NOT NULL,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_simulation_result_job
        FOREIGN KEY (job_id)
        REFERENCES simulation_job(id)
        ON DELETE CASCADE
);