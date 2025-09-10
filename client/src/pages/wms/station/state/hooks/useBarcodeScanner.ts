// useBarcodeScanner.ts
import { useEffect, useRef } from "react";

const useBarcodeScanner = (onScanComplete: (value: string) => void) => {
    const inputBuffer = useRef("");
    const lastKeyPressTime = useRef(Date.now());
    const lastKey = useRef("");
    const isFirstScanPress = useRef(true);
    const saveFirstKey = useRef("");
    const isListening = useRef(true);

    const Reg=/^[a-zA-Z0-9 /-]+$/


    useEffect(() => {
        const handleKeyDown = (event: KeyboardEvent) => {
            if (!isListening.current) return;
            const currentTime = Date.now();
            const timeSinceLastKeyPress = currentTime - lastKeyPressTime.current;
            const isScanPress = timeSinceLastKeyPress < 50;
            console.log("isScanPress",isScanPress,event.key,inputBuffer.current)
            if (isScanPress) {
                const isLetterOrDigit = Reg.test(event.key);
                if (document.activeElement) {
                    (document.activeElement as HTMLElement).blur();
                }

                if (isFirstScanPress.current) {
                    saveFirstKey.current = lastKey.current;
                    isFirstScanPress.current = false;
                }
                if (event.key === "Enter") {
                    isFirstScanPress.current = true;
                    const is = Reg.test(saveFirstKey.current);
                    const midValue = `${is ? saveFirstKey.current : ""}${inputBuffer.current}`.replace(/Shift/g,"")
                    // const midValue = inputBuffer.current
                    onScanComplete(midValue);
                    saveFirstKey.current = "";
                    inputBuffer.current = "";
                } else if(isLetterOrDigit) {
                    inputBuffer.current += event.key;
                }
            }

            lastKeyPressTime.current = currentTime;
            lastKey.current = event.key;
        };

        window.addEventListener("keydown", handleKeyDown);
        // 添加全局焦点变化监听
        const handleFocusChange = () => {
            if (document.activeElement?.tagName === 'INPUT' || 
                document.activeElement?.tagName === 'TEXTAREA') {
                // 输入框获得焦点，暂停扫码枪监听
                isListening.current = false;
            } else {
                // 没有输入框获得焦点，恢复扫码枪监听
                isListening.current = true;
            }
        };

        // 初始化监听状态
        handleFocusChange();
        
        // 添加焦点变化监听
        window.addEventListener('focusin', handleFocusChange);
        window.addEventListener('focusout', handleFocusChange);
        return () => {
            window.removeEventListener("keydown", handleKeyDown);
            window.removeEventListener('focusin', handleFocusChange);
            window.removeEventListener('focusout', handleFocusChange);
        };
    }, [onScanComplete]);

    return { inputBuffer };
};

export {useBarcodeScanner};