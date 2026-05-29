package de.hablijack.greenhouse.ai.rag.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "plant_knowledge_document", schema = "greenhouse",
    indexes = {
        @Index(name = "idx_doc_plant_type", columnList = "plant_type"),
        @Index(name = "idx_doc_category", columnList = "category")
    })
public class PlantKnowledgeDocument extends PanacheEntity {

  @Column(name = "plant_type", nullable = false)
  public String plantType;

  @Column(name = "title")
  public String title;

  @Column(name = "content", columnDefinition = "TEXT", nullable = false)
  public String content;

  @Column(name = "category")
  public String category;

  @Column(name = "created_at", nullable = false)
  public Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  public Instant updatedAt;

  public PlantKnowledgeDocument() {
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
  }

  public PlantKnowledgeDocument(String plantType, String title, String content, String category) {
    this();
    this.plantType = plantType;
    this.title = title;
    this.content = content;
    this.category = category;
  }
}
