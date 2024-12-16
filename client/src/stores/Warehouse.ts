import {types} from "mobx-state-tree";

const Warehouse = types
    .model('Warehouse', {
        code: ''
    })
    .views((self) => ({
        get warehouseCode() {
            return self.code || localStorage.getItem('warehouseCode');
        }
    }))
    .actions((self) => {
        return {
            setWarehouseCode(warehouseCode: string) {
                self.code = warehouseCode;
                try {
                    if (warehouseCode) {
                        localStorage.setItem('warehouseCode', warehouseCode);
                    } else {
                        localStorage.setItem('warehouseCode', '');
                    }
                } catch (error) {
                    console.error("Failed to set warehouseCode in localStorage:", error);
                }
            }
        }
    });

export default Warehouse;
