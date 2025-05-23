import request from "@/utils/requestInterceptor";

const qz = require("qz-tray");

export interface PrintData {
    printRecordId: number;
    content: string;
    workStationId?: number;
    printer?: string;
    copies?: number;
}

class QZPrinter {
    private isConnected = false;

    /**
     * Initialize QZ Tray connection
     */
    public async initialize(): Promise<void> {
        try {
            // Set up security if needed
            // qz.security.setCertificatePromise((resolve) => {
            //     resolve({ pass: 'your-password-here' }); // Replace with your actual password
            // });

            await qz.websocket.connect();
            this.isConnected = true;
            console.log("QZ Tray connected successfully");
        } catch (error) {
            console.error("QZ Tray connection error:", error);
            throw error;
        }
    }

    /**
     * Check if QZ Tray is connected
     */
    public isReady(): boolean {
        return this.isConnected && qz.websocket.isActive();
    }

    public async printAndUpdateRecord(printData: PrintData): Promise<void> {
        try {
            if (!this.isReady()) {
                await this.initialize();
            }

            // Format the print job
            const printers = await qz.printers.find();
            const printerName = printData.printer || printers[0];

            const config = qz.configs.create(printerName, {
                copies: printData.copies || 1
            });

            const data = [
                {type: 'text', format: 'plain', data: printData.content}
            ];

            // Execute print
            await qz.print(config, data);

            // Update record status on success
            await request({
                method: 'patch',
                url: `/wms/print/record/${printData.printRecordId}/status`,
                data: {status: 'PRINT_SUCCESS'}
            });

            console.log('Print job completed and record updated');

        } catch (error) {
            console.error('Printing failed:', error);

            // Update record status on failure
            await request({
                method: 'patch',
                url: `/wms/print/record/${printData.printRecordId}/status`,
                data: {
                    status: 'PRINT_FAIL',
                    errorMessage: error.message
                }
            });

            throw error;
        }
    }

    /**
     * Disconnect from QZ Tray
     */
    public async disconnect(): Promise<void> {
        if (qz.websocket.isActive()) {
            await qz.websocket.disconnect();
            this.isConnected = false;
            console.log('QZ Tray disconnected');
        }
    }
}

// Export a singleton instance
export const qzPrinter = new QZPrinter();
