import { useDebounceFn } from "ahooks"
import type { AutoCompleteProps } from "antd"
import { AutoComplete } from "antd"
import classNames from "classnames/bind"
import React, { useState } from "react"

// import { getRecipientOrderUnFinishSkuInfosMatched } from '@/services/station/inbound';

import styles from "./index.module.scss"

const cx = classNames.bind(styles)

const SkuAutoComplete: React.ForwardRefRenderFunction<
    any,
    AutoCompleteProps
> = (props, ref) => {
    const [options, setOptions] = useState<{ value: string }[]>([])
    const { children, value } = props

    const onSearch = async (searchText: string) => {
        if (!searchText) {
            setOptions([])
            return
        }

        // const { data } = await getRecipientOrderUnFinishSkuInfosMatched({
        //   matchParam: encodeURI(searchText),
        //   pageIndex: 1,
        //   pageSize: 100,
        // });
        // const options = data.results?.map((item) => {
        //   return {
        //     value: item.skuCode,
        //     label: `${item.skuCode}  ${item.skuName}`,
        //   };
        // });
        // setOptions(options);
    }

    const onSearchDebounce = useDebounceFn(onSearch, {
        wait: 500
    }).run

    return (
        <AutoComplete
            ref={ref}
            className={cx("scan-input")}
            options={options}
            onSearch={onSearchDebounce}
            backfill={true}
            filterOption={(inputValue, option) => {
                return !!value
            }}
            {...(props as any)}
        >
            {children}
        </AutoComplete>
    )
}

export default React.forwardRef(SkuAutoComplete)
