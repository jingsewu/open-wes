import React, { useCallback, useRef } from "react"
import { Schema } from "amis/lib/types"
import schema2component from "@/utils/schema2component"

interface AmisEditorFormProps {
    data: Record<string, unknown>
    onChange?: (data: Record<string, unknown>) => void
}

function buildSchema(disabled: boolean): Schema {
    return {
        type: "form",
        wrapWithPanel: false,
        submitOnChange: false,
        body: [
            {
                type: "editor",
                name: "source",
                // AMIS bundles no Groovy grammar; Java highlighting is the closest match.
                language: "java",
                size: "lg",
                disabled,
                allowFullscreen: false,
                options: {
                    automaticLayout: true,
                    minimap: { enabled: false },
                    scrollBeyondLastLine: false,
                    wordWrap: "on",
                    fontSize: 13,
                    tabSize: 4
                }
            }
        ]
    }
}

const EditableAmisEditor = schema2component(buildSchema(false)) as React.FC<AmisEditorFormProps>
const ReadOnlyAmisEditor = schema2component(buildSchema(true)) as React.FC<AmisEditorFormProps>

interface ScriptEditorProps {
    initialValue: string
    readOnly?: boolean
    onChange?: (value: string) => void
}

/**
 * AMIS editor renderer over the Monaco build AMIS already bundles. The seed value is captured once
 * per mount; the parent changes files or reloads by remounting this component with a new key.
 */
const ScriptEditor: React.FC<ScriptEditorProps> = ({ initialValue, readOnly, onChange }) => {
    const initialData = useRef<Record<string, unknown>>({ source: initialValue }).current
    const onChangeRef = useRef(onChange)
    onChangeRef.current = onChange

    const handleFormChange = useCallback((data: Record<string, unknown>) => {
        const next = data.source
        if (onChangeRef.current) {
            onChangeRef.current(typeof next === "string" ? next : "")
        }
    }, [])

    return (
        <div style={{ maxHeight: 520, overflow: "auto", border: "1px solid #d9d9d9", borderRadius: 2 }}>
            {readOnly ? (
                <ReadOnlyAmisEditor data={initialData} />
            ) : (
                <EditableAmisEditor data={initialData} onChange={handleFormChange} />
            )}
        </div>
    )
}

export default ScriptEditor
