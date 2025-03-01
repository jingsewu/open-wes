// api_management module
import request from "@/utils/requestInterceptor";

export const api_api_add = "post:/api-platform/api-management/add"
export const api_api_update = "post:/api-platform/api-management/update"
export const api_api_delete = "delete:/api-platform/api-management/${id}"
export const api_api_config_get = "get:/api-platform/api-config-management/${code}"
export const api_api_config_update = "post:/api-platform/api-config-management/update"

export const api_api_log_get = "get:/api-platform/api-log-management/${id}"

export const api_api_keys_get = "post:/api-platform/api-keys/${id}"
export const api_api_keys_create = "post:/api-platform/api-keys/create"
export const api_api_keys_delete = "delete:/api-platform/api-keys/${id}"

//todo by Evlyn
// help me check if i defined the const here correct?
let debounceTimer: NodeJS.Timeout | null = null;

export const editorDidMount = (editor: any, monaco: any) => {

    // Clear any existing debounce timer
    if (debounceTimer) {
        clearTimeout(debounceTimer);
    }

// Set a new debounce timer to trigger after 300ms (adjust as needed)
    debounceTimer = setTimeout(async function () {

        monaco.languages.registerCompletionItemProvider("java", {
            provideCompletionItems: async function (model: any, position: any) {
                console.log("Completion triggered");
                const codeContext = model.getValue();
                const lineContent = model.getLineContent(position.lineNumber);
                const language = "Java"

                const response: any = await request({
                    url: "/ai/ai/generateCode",
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    data: JSON.stringify({codeContext, lineContent, language})
                });

                if (response.data && typeof response.data === "string") {
                    let fullCode: string = response.data.trim();
                    fullCode = fullCode.split('\n').join('\n');
                    console.log("Generated Code:", fullCode);

                    return {
                        suggestions: fullCode.split("\n").filter(line => line.trim()).map(s => ({
                            label: s.trim(),
                            kind: monaco.languages.CompletionItemKind.Function,
                            insertText: s.trim(),
                            range: new monaco.Range(
                                position.lineNumber,
                                1,
                                position.lineNumber,
                                model.getLineMaxColumn(position.lineNumber)
                            )
                        }))
                    };
                }

                console.warn("No valid response received");
                return {suggestions: []};
            }
        });
    }, 3000); // 300ms delay (adjustable)
}
