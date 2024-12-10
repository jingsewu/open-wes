import type { ReactNode } from 'react';
import React from 'react';
import styles from './style.module.scss';

interface IProps {
  separator?: string;
  value: {
    name: string | ReactNode;
    active: boolean;
  }[];
}

const StepDescription = (props: IProps) => {
  const { value, separator = '>' } = props;

  return (
    <nav className={styles.container}>
      {value.map(({ name, active }, index) => {
        return (
          <ol
            className={`${styles['item']} ${active ? styles['active'] : ''}`}
            style={{ display: 'inline' }}
            key={index}
          >
            {name}
            {index !== value.length - 1 && <span className={styles['separator']}>{separator}</span>}
          </ol>
        );
      })}
    </nav>
  );
};

export default StepDescription;
