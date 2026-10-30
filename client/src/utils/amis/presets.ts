import type { Schema } from "amis/lib/types"

/**
 * Shared AMIS schema presets.
 *
 * New CRUD pages should compose these helpers instead of copying the same
 * page/crud/toolbar/column boilerplate. See docs/standards/amis.md.
 */

export type SchemaNode = Schema
/**
 * AMIS types require `type` on every Schema, but table columns and drawer
 * bodies omit it and let the renderer infer the default. `SchemaLike` keeps
 * `type` optional so column helpers stay type-safe without inventing one.
 */
export type SchemaLike = Omit<Schema, "type"> & { type?: string }

/**
 * AMIS accepts either a URL string or an api object. The app's API constants
 * use upper-case methods ("POST"), which the strict SchemaApi union rejects;
 * this looser contract matches what the runtime actually accepts.
 */
export type ApiLike = string | {
    method?: string
    url?: string
    [key: string]: unknown
}

export interface ColumnOptions {
    searchable?: boolean | SchemaLike
    width?: number
    fixed?: "left" | "right"
    sortable?: boolean
    /** Whether the column is visible by default. Hidden columns stay in the column toggler. */
    toggled?: boolean
}

export type StatusTone = "success" | "warning" | "danger" | "info" | "muted"

function withOptions(base: SchemaLike, options: ColumnOptions): SchemaLike {
    return {
        ...base,
        ...(options.searchable !== undefined
            ? { searchable: options.searchable }
            : {}),
        ...(options.width !== undefined ? { width: options.width } : {}),
        ...(options.fixed ? { fixed: options.fixed } : {}),
        ...(options.sortable !== undefined
            ? { sortable: options.sortable }
            : {}),
        ...(options.toggled !== undefined ? { toggled: options.toggled } : {})
    }
}

/** Machine-readable identifier (LPN, SKU, container code, order number). */
export function codeColumn(
    name: string,
    label: string,
    options: ColumnOptions = {}
): SchemaLike {
    return withOptions(
        {
            name,
            label,
            className: "owes-code",
            copyable: true
        },
        options
    )
}

/** Numeric measure. Right-aligned with tabular figures. */
export function numberColumn(
    name: string,
    label: string,
    options: ColumnOptions = {}
): SchemaLike {
    return withOptions(
        {
            name,
            label,
            align: "right",
            className: "owes-number"
        },
        options
    )
}

/** Millisecond timestamp rendered at minute precision; seconds are opt-in. */
export function dateTimeColumn(
    name: string,
    label: string,
    options: ColumnOptions & { seconds?: boolean } = {}
): SchemaLike {
    const format = options.seconds
        ? "YYYY-MM-DD HH\\:mm\\:ss"
        : "YYYY-MM-DD HH\\:mm"
    const { seconds: _seconds, ...columnOptions } = options
    return withOptions(
        {
            name,
            label,
            tpl: `\${${name}/1000|date:${format}}`
        },
        columnOptions
    )
}

/**
 * Status column rendered with a semantic dot and tone.
 * Labels are locale keys (or literal strings when the value is data, not UI
 * copy); tones map each enum value to a semantic colour.
 */
export function statusColumn(
    name: string,
    label: string,
    options: ColumnOptions & {
        labels: Record<string, string>
        tones?: Record<string, StatusTone>
    }
): SchemaLike {
    const { labels, tones, ...columnOptions } = options
    const source: Record<
        string,
        { label: string; className: string }
    > = {}
    Object.entries(labels).forEach(([value, text]) => {
        source[value] = {
            label: text,
            className: `owes-status owes-status--${
                tones?.[value] ?? "muted"
            }`
        }
    })
    return withOptions(
        {
            name,
            label,
            type: "status",
            source
        },
        columnOptions
    )
}

/** Long free text: clamped in the cell, full value in a hover popover. */
export function remarkColumn(
    name = "remark",
    label = "table.remark",
    options: ColumnOptions = {}
): SchemaLike {
    return withOptions(
        {
            type: "tpl",
            name,
            label,
            tpl: `\${${name}|truncate:30}`,
            popOver: {
                trigger: "hover",
                position: "left-top",
                showIcon: false,
                body: {
                    type: "tpl",
                    tpl: `\${${name}}`
                }
            }
        },
        options
    )
}

/**
 * Required field with a domain-specific message instead of AMIS's generic
 * "cannot be empty" copy. The message names the field and the expected action,
 * e.g. "请扫描或输入容器编码".
 *
 *   { name: "containerCode", label: "table.containerCode", ...requiredField("validation.container.codeRequired") }
 */
export function requiredField(messageKey: string): {
    required: true
    validationErrors: { isRequired: string }
} {
    return {
        required: true,
        validationErrors: { isRequired: messageKey }
    }
}

/** Standard operation column. Keep at most three visible actions. */
export function operationColumn(
    buttons: SchemaLike[],
    options: { width?: number; fixed?: boolean } = {}
): SchemaLike {
    return {
        type: "operation",
        label: "table.operation",
        width: options.width ?? 48 + buttons.length * 56,
        ...(options.fixed === false ? {} : { fixed: "right" }),
        buttons,
        toggled: true
    }
}

/**
 * Create/update audit columns shared by master-data tables.
 *
 * Audit trails are diagnostic, not operational: they are hidden by default and
 * can be revealed from the CRUD column settings. Pass `{ visible: true }` for
 * audit-focused pages (logs, history) where the block is the point.
 */
export function auditColumns(
    options: { seconds?: boolean; visible?: boolean } = {}
): SchemaLike[] {
    const { visible: _visible, ...columnOptions } = options
    const toggled = options.visible ?? false
    return [
        { name: "createUser", label: "table.createdBy", toggled },
        dateTimeColumn("createTime", "table.creationTime", {
            ...columnOptions,
            toggled
        }),
        { name: "updateUser", label: "table.modifiedBy", toggled },
        dateTimeColumn("updateTime", "table.updated", {
            ...columnOptions,
            toggled
        })
    ]
}

export interface CrudPageOptions {
    title: string
    name: string
    searchIdentity: string
    columns: SchemaLike[]
    /** Columns sent to the search backend; defaults to `columns`. */
    showColumns?: SchemaLike[]
    api: ApiLike
    orderBy?: string
    /** Page-level actions, rendered right-aligned in the page header. */
    pageActions?: SchemaLike[]
    /** Table utilities, rendered in the CRUD toolbar after "reload". */
    tableActions?: SchemaLike[]
    /** Explicit filter controls. Omit only when there are four or fewer searchable columns. */
    filters?: SchemaLike[]
    rowActions?: SchemaLike[]
    bulkActions?: SchemaLike[]
    empty?: string | SchemaLike
    selectable?: boolean
    operationWidth?: number
}

export function crudPage(options: CrudPageOptions): Schema {
    const showColumns = options.showColumns ?? options.columns
    const hasHiddenByDefault = options.columns.some(
        (column) => column.toggled === false
    )
    const columns = [
        ...options.columns,
        ...(options.rowActions?.length
            ? [
                  operationColumn(options.rowActions, {
                      width: options.operationWidth
                  })
              ]
            : [])
    ]

    const crud: Schema = {
        type: "crud",
        name: options.name,
        syncLocation: false,
        api: options.api,
        defaultParams: {
            searchIdentity: options.searchIdentity,
            showColumns,
            ...(options.orderBy
                ? { searchObject: { orderBy: options.orderBy } }
                : {})
        },
        autoFillHeight: true,
        // Hidden-by-default columns are only reachable through the toggler, so
        // force it on; otherwise keep AMIS's auto threshold (5+ columns).
        columnsTogglable: hasHiddenByDefault ? true : "auto",
        ...(options.filters
            ? {
                  filterTogglable: options.filters.length > 4,
                  filterDefaultVisible: true,
                  filter: {
                      title: "common.filter",
                      mode: "inline",
                      body: options.filters,
                      actions: [
                          { type: "reset", label: "button.reset" },
                          {
                              type: "submit",
                              level: "primary",
                              label: "common.search"
                          }
                      ]
                  }
              }
            : {
                  autoGenerateFilter: {
                      columnsNum: 3,
                      showBtnToolbar: true
                  }
              }),
        headerToolbar: ["reload", ...(options.tableActions ?? [])],
        footerToolbar: ["switch-per-page", "statistics", "pagination"],
        ...(options.empty ? { placeholder: options.empty } : {}),
        ...(options.selectable || options.bulkActions?.length
            ? { multiple: true }
            : {}),
        ...(options.bulkActions?.length
            ? { bulkActions: options.bulkActions }
            : {}),
        columns
    }

    return {
        type: "page",
        title: options.title,
        toolbar: options.pageActions ?? [],
        data: {
            dictionary: "${ls:dictionary}"
        },
        body: [crud]
    }
}

export interface FormDrawerOptions {
    title: string
    body: SchemaLike[]
    api?: ApiLike
    initApi?: ApiLike
    reload?: string
    size?: "sm" | "md" | "lg" | "xl"
    columnCount?: 1 | 2 | 3
    submitLabel?: string
    /** Forms with more than three fields keep the drawer open on outside clicks. */
    closeOnOutside?: boolean
}

/**
 * Standard create/edit drawer with deterministic form layout and footer.
 *
 * The footer buttons live on the drawer, not on the form: AMIS renders a
 * drawer body form with `wrapWithPanel: false`, which drops the form's own
 * `actions` array. A drawer-level confirm action submits the form and
 * reloads the target.
 */
export function formDrawer(options: FormDrawerOptions): SchemaLike {
    const columnCount =
        options.columnCount ??
        (options.body.length > 6 ? 2 : 1)
    return {
        size: options.size ?? (columnCount === 1 ? "md" : "lg"),
        title: options.title,
        closeOnEsc: true,
        closeOnOutside:
            options.closeOnOutside ?? options.body.length <= 3,
        actions: [
            {
                type: "button",
                actionType: "cancel",
                label: "common.cancel"
            },
            {
                type: "button",
                actionType: "confirm",
                level: "primary",
                label: options.submitLabel ?? "common.save",
                ...(options.reload ? { reload: options.reload } : {})
            }
        ],
        body: {
            type: "form",
            mode: "horizontal",
            labelWidth: 108,
            columnCount,
            messages: {
                validateFailed: "validation.formFailed"
            },
            ...(options.api ? { api: options.api } : {}),
            ...(options.initApi ? { initApi: options.initApi } : {}),
            body: options.body
        }
    }
}

/** Primary create action for the page header. */
export function addButton(options: {
    target: string
    drawer: SchemaLike
    label?: string
}): Schema {
    return {
        type: "button",
        actionType: "drawer",
        level: "primary",
        icon: "fa fa-plus",
        label: options.label ?? "button.add",
        target: options.target,
        drawer: options.drawer
    }
}

/** Row-level edit action. */
export function editButton(
    drawer: SchemaLike,
    label = "button.modify"
): Schema {
    return {
        type: "button",
        actionType: "drawer",
        level: "link",
        className: "owes-action",
        label,
        drawer
    }
}

/** Row-level destructive action. Never use a filled danger button in a table row. */
export function deleteButton(options: {
    api: ApiLike
    reload?: string
}): Schema {
    return {
        type: "button",
        actionType: "ajax",
        level: "link",
        className: "owes-action owes-action--danger",
        label: "button.delete",
        confirmText: "toast.sureDelete",
        confirmTitle: "button.delete",
        ...(options.reload ? { reload: options.reload } : {}),
        api: options.api
    }
}

/** Standard export action. AMIS reads the lower-case `filename` property. */
export function exportButton(options: {
    api: ApiLike
    filename: string
    label?: string
    method?: string
}): Schema {
    return {
        type: "export-excel",
        label: options.label ?? "button.export",
        ...(options.method ? { method: options.method } : {}),
        api: options.api,
        filename: options.filename
    }
}
