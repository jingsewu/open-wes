import { useEffect } from 'react';
import { cloneDeep } from 'lodash-es';

/**
 * @description: 用于解决window页面失去焦点后，页面所有隐藏input框无法正常聚焦的问题
 * @param setWorkStationEvent
 */
export function useWindowFocus(setWorkStationEvent:any) {
  useEffect(() => {
    const windowFocusListener = (event: any) => {
        /** 只有当全页面失焦才触发页面重新渲染 */
        if (event.target === window) {
            setWorkStationEvent((prevState: any) => cloneDeep(prevState))
            console.log(
                "%c =====> 在页面重新聚焦时，重新渲染页面",
                "color:red;font-size:20px;"
            )
        }
    }

    // 监听
    window.addEventListener('focus', windowFocusListener);

    return () => {
      window.removeEventListener('focus', windowFocusListener);
    };
  }, []);
}
