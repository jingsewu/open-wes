package org.openwes.script.plugin.core.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.common.utils.id.IdGenerator;
import org.openwes.script.plugin.api.enums.RevisionOriginEnum;

@Data
@Entity
@Table(name = "p_script_plugin_revision", indexes = {
        @Index(unique = true, name = "uk_script_revision_no", columnList = "pluginId,revisionNo")})
public class ScriptPluginRevision {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    private Long id;

    @Column(nullable = false)
    private Long pluginId;

    @Column(nullable = false)
    private Integer revisionNo;

    /** JSON object: script file name -> groovy source. */
    @Column(nullable = false, columnDefinition = "json")
    private String scripts;

    /** JSON array of ExtensionDeclarationDTO at creation time. */
    @Column(nullable = false, columnDefinition = "json")
    private String extensionPointsSnapshot;

    @Column(columnDefinition = "json")
    private String configSchemaSnapshot;

    @Column(nullable = false, length = 64)
    private String contentHash;

    @Column(nullable = false, length = 512)
    private String changeSummary = "";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RevisionOriginEnum origin;

    @Column(length = 64)
    private String aiModel;

    @Column(length = 32)
    private String promptVersion;

    @Column(nullable = false, length = 60)
    private String createdBy = "";

    @Column(nullable = false)
    private Long createdAt;

    @Comment("revisions are immutable; rollback only moves ScriptPlugin.currentRevisionId")
    public void markImmutable() {
        // intentionally empty: documents the invariant that no update path exists for revisions
    }
}
