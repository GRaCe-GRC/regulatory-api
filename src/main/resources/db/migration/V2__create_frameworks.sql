CREATE TABLE frameworks (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    authority_id uuid NOT NULL,
    name varchar(255) NOT NULL,
    short_name varchar(255),
    citation varchar(255),
    enabled boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL DEFAULT now(),
    created_by varchar(255) NOT NULL,
    CONSTRAINT frameworks_authority_id_fkey FOREIGN KEY (authority_id) REFERENCES authorities (id) ON DELETE RESTRICT,
    CONSTRAINT frameworks_authority_id_name_key UNIQUE (authority_id, name)
);
