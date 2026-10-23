CREATE TABLE authorities (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name varchar(255) NOT NULL,
    jurisdiction varchar(255) NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    enabled boolean NOT NULL DEFAULT true,
    created_by varchar(255) NOT NULL,
    CONSTRAINT authorities_name_key UNIQUE (name)
);
