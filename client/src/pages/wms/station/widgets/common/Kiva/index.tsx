import classNames from 'classnames/bind';
import React, { memo } from 'react';
import style from '../styles/Kiva.module.scss';

const cx = classNames.bind(style);

const Kiva = ({ children, childrenClassName }: any) => {
  return (
    <div className={cx('kiva')}>
      <div className={childrenClassName}>{children}</div>
      <img width={208} alt='KIVA' src='/assets/station/KIVa.png' />
    </div>
  );
};

export default memo(Kiva);
