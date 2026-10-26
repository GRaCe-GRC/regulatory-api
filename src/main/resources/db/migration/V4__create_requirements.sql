CREATE TABLE requirements (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    file_id uuid NOT NULL,
    parent_requirement_id uuid,
    provision_ref varchar(255) NOT NULL,
    heading varchar(255),
    text text,
    created_at timestamptz NOT NULL DEFAULT now(),
    created_by varchar(255) NOT NULL,
    CONSTRAINT requirements_file_id_fkey FOREIGN KEY (file_id) REFERENCES files (id) ON DELETE RESTRICT,
    CONSTRAINT requirements_parent_requirement_id_fkey FOREIGN KEY (parent_requirement_id) REFERENCES requirements (id) ON DELETE RESTRICT,
    CONSTRAINT requirements_file_id_provision_ref_key UNIQUE (file_id, provision_ref)
);

CREATE INDEX requirements_parent_requirement_id_idx ON requirements (parent_requirement_id);
