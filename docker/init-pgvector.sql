-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Set schema search path
SET search_path TO greenhouse, public;

-- Ensure the embedding column exists on the plant_knowledge_document table
ALTER TABLE greenhouse.plant_knowledge_document
  ADD COLUMN IF NOT EXISTS embedding vector;
