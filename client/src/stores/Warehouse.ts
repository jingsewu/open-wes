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
                localStorage.setItem('warehouseCode', warehouseCode);
            }
        }
    });

export default Warehouse;
