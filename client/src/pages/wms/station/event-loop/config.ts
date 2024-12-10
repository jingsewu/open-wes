import type { WorkStationEventLoopConfig } from './types';

const WORK_STATION_EVENT_LOOP_CONFIG: WorkStationEventLoopConfig = {
  pollingInterval: 500,
  queryURL: '/station/station/operation/getCurrentOperation',
  confirmURL: '/station/station/operation/operationConfirm',
  stationCode: '',
  sendEventURL: '/station/station/event/sendEvent',
  getWorkStationInfoURL: '/station/station/getStationStatus',
};

export default WORK_STATION_EVENT_LOOP_CONFIG;
