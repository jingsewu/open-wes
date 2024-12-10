import classNames from 'classnames/bind';
import React, { memo } from 'react';

import style from '../styles/Conveyor.module.scss';

const cx = classNames.bind(style);

const Conveyor = ({ children, width, height }: any) => {
  return (
    <div
      className={cx('conveyor')}
      style={{
        background: `url('/assets/station/conveyor.png') no-repeat`,
        backgroundSize: 'cover',
        width: width ?? 604,
        height: height ?? 257,
      }}
    >
      {children}
    </div>
  );
};

export default memo(Conveyor);
