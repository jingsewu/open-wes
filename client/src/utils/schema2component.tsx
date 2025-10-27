import { Schema } from "amis/lib/types"
import AMisRenderer from "../components/AMisRenderer"
import * as React from "react"

export default function (schema: Schema) {
    return (props: any) => {
        return <AMisRenderer schema={schema} {...props} />
    }
}
