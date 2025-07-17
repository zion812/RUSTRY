package com.rio.rustry.domain.model

/**
 * Family tree models for breeding functionality
 */
data class FamilyTree(
    val rootNode: FamilyTreeNode,
    val nodes: List<FamilyTreeNode> = emptyList(),
    val relationships: List<FamilyRelationship> = emptyList()
)

data class FamilyRelationship(
    val parentId: String,
    val childId: String,
    val relationshipType: String = "parent-child"
)