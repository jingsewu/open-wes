import axios from 'axios';

export interface APIResponse<T = any> {
  code: string;
  data: T;
  message: string;
  msg: string;
}

const axiosInstance = axios.create({
  baseURL: '/gw/station',
});

axiosInstance.interceptors.response.use<APIResponse>(
  (response) => {
    const { status, data } = response;
    if (status !== 200) {
      return {
        code: '-1',
        data: {},
        message: '网络访问失败',
      };
    }

    return data;
  },
  (error) => {
    console.log('%c =====> http Error: error', 'color:red;font-size:20px;', error);
    return {
      code: '-1',
      data: {},
      message: '网络访问失败',
    };
  },
);

export default axiosInstance;
