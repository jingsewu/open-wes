import React, { useEffect, useState } from "react"
import { Alert, Button, Input, Popconfirm, Select, Space, message } from "antd"
import { useTranslation } from "react-i18next"
import { Scripts } from "./types"
import ScriptEditor from "./ScriptEditor"

interface ScriptFileEditorProps {
    scripts: Scripts
    onChange: (next: Scripts) => void
    readOnly: boolean
    /** Changes when the underlying revision changes, forcing a reseed and editor remount. */
    seedKey: string
}

const ScriptFileEditor: React.FC<ScriptFileEditorProps> = ({ scripts, onChange, readOnly, seedKey }) => {
    const { t } = useTranslation()
    const [activeFile, setActiveFile] = useState<string | null>(null)
    const [newFileName, setNewFileName] = useState("")
    const [editorVersion, setEditorVersion] = useState(0)

    useEffect(() => {
        const names = Object.keys(scripts)
        setActiveFile((previous) => {
            if (previous !== null && names.indexOf(previous) >= 0) {
                return previous
            }
            return names.length > 0 ? names[0] : null
        })
        setEditorVersion((version) => version + 1)
        setNewFileName("")
    }, [seedKey])

    // The current revision is fetched after this editor mounts, so the seed effect above runs
    // while `scripts` is still empty. Select the first file once the revision content arrives;
    // otherwise an existing revision renders as "no script files".
    useEffect(() => {
        if (activeFile !== null) {
            return
        }
        const names = Object.keys(scripts)
        if (names.length > 0) {
            setActiveFile(names[0])
        }
    }, [scripts, activeFile])

    const fileNames = Object.keys(scripts)

    const selectFile = (name: string) => {
        setActiveFile(name)
        setEditorVersion((version) => version + 1)
    }

    const addFile = () => {
        const name = newFileName.trim()
        if (name === "") {
            return
        }
        if (Object.prototype.hasOwnProperty.call(scripts, name)) {
            message.error(t("pluginPlatform.scripts.fileExists"))
            return
        }
        onChange({ ...scripts, [name]: "" })
        setActiveFile(name)
        setNewFileName("")
        setEditorVersion((version) => version + 1)
    }

    const deleteFile = () => {
        if (activeFile === null) {
            return
        }
        const next: Scripts = { ...scripts }
        delete next[activeFile]
        onChange(next)
        const remaining = Object.keys(next)
        setActiveFile(remaining.length > 0 ? remaining[0] : null)
        setEditorVersion((version) => version + 1)
    }

    const handleEditorChange = (value: string) => {
        if (activeFile === null) {
            return
        }
        onChange({ ...scripts, [activeFile]: value })
    }

    return (
        <Space direction="vertical" style={{ width: "100%" }} size="middle">
            <Space wrap>
                <Select
                    style={{ minWidth: 240 }}
                    value={activeFile === null ? undefined : activeFile}
                    onChange={selectFile}
                    options={fileNames.map((name) => ({ label: name, value: name }))}
                    placeholder={t("pluginPlatform.scripts.empty")}
                />
                {!readOnly && (
                    <>
                        <Input
                            style={{ width: 200 }}
                            value={newFileName}
                            onChange={(event) => setNewFileName(event.target.value)}
                            placeholder={t("pluginPlatform.scripts.newFile")}
                            onPressEnter={addFile}
                        />
                        <Button onClick={addFile} disabled={newFileName.trim() === ""}>
                            {t("pluginPlatform.scripts.addFile")}
                        </Button>
                        <Popconfirm
                            title={t("pluginPlatform.scripts.deleteFileConfirm", { file: activeFile || "" })}
                            onConfirm={deleteFile}
                            disabled={activeFile === null}
                            okText={t("pluginPlatform.common.confirm")}
                            cancelText={t("pluginPlatform.common.cancel")}
                        >
                            <Button danger disabled={activeFile === null}>
                                {t("pluginPlatform.scripts.deleteFile")}
                            </Button>
                        </Popconfirm>
                    </>
                )}
            </Space>
            {activeFile === null ? (
                <Alert type="info" showIcon message={t("pluginPlatform.scripts.empty")} />
            ) : (
                <ScriptEditor
                    key={activeFile + ":" + editorVersion}
                    initialValue={scripts[activeFile] || ""}
                    readOnly={readOnly}
                    onChange={readOnly ? undefined : handleEditorChange}
                />
            )}
        </Space>
    )
}

export default ScriptFileEditor
