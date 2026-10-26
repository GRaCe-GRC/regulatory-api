CREATE TABLE files (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    framework_id uuid NOT NULL,
    title varchar(255) NOT NULL,
    source_url text,
    version_label varchar(255),
    publication_date date,
    created_at timestamptz NOT NULL DEFAULT now(),
    created_by varchar(255) NOT NULL,
    CONSTRAINT files_framework_id_fkey FOREIGN KEY (framework_id) REFERENCES frameworks (id) ON DELETE RESTRICT
);

CREATE INDEX files_framework_id_idx ON files (framework_id);
