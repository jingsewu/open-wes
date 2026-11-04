import React, { useMemo } from "react"
import { Schema } from "amis/lib/types"
import schema2component from "@/utils/schema2component"

const diffSchema: Schema = {
    type: "form",
    wrapWithPanel: false,
    submitOnChange: false,
    body: [
        {
            type: "diff-editor",
            name: "diff",
            language: "java",
            disabled: true,
            // AMIS renders diffValue as the left/original pane and value as the right/modified pane.
            value: "${right}",
            diffValue: "${left}",
            options: {
                automaticLayout: true,
                minimap: { enabled: false },
                scrollBeyondLastLine: false,
                readOnly: true
            }
        }
    ]
}

interface AmisDiffFormProps {
    data: Record<string, string>
}

const AmisDiffForm = schema2component(diffSchema) as React.FC<AmisDiffFormProps>

interface DiffViewerProps {
    left: string
    right: string
    /** Changes only when the compared revisions or file change, forcing a fresh render. */
    viewKey: string
}

const DiffViewer: React.FC<DiffViewerProps> = ({ left, right, viewKey }) => {
    const data = useMemo(() => ({ left, right }), [left, right])
    return (
        <div style={{ border: "1px solid #d9d9d9", borderRadius: 2, overflow: "hidden" }}>
            <AmisDiffForm key={viewKey} data={data} />
        </div>
    )
}

export default DiffViewer
