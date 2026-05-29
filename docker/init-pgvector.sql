-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Set schema search path
SET search_path TO greenhouse, public;
