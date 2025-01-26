import React from 'react';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
  ArcElement,
} from 'chart.js';
import { Line, Bar, Pie } from 'react-chartjs-2';
import { QueryResult } from '../types';

ChartJS.register(
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    BarElement,
    ArcElement,
    Title,
    Tooltip,
    Legend
);

interface ResultVisualizationsProps {
  result: QueryResult;
  activeChart: 'line' | 'bar' | 'pie' | null;
}

export function ResultVisualizations({ result, activeChart }: ResultVisualizationsProps) {
  const isNumeric = (value: any) => !isNaN(parseFloat(value)) && isFinite(value);

  // Find numeric columns for y-axis based on dataType
  const numericColumns = result.columns.filter(column =>
      ['number', 'integer', 'decimal', 'float', 'double'].includes(column.dataType.toLowerCase())
  );

  // Use first non-numeric column as labels (x-axis)
  const labelColumn = result.columns.find(column =>
      !['number', 'integer', 'decimal', 'float', 'double'].includes(column.dataType.toLowerCase())
  ) || result.columns[0];

  const chartData = {
    labels: result.data.map(row => row[labelColumn.name]),
    datasets: numericColumns.map((column, index) => ({
      label: column.name,
      data: result.data.map(row => row[column.name]),
      borderColor: `hsl(${index * (360 / numericColumns.length)}, 70%, 50%)`,
      backgroundColor: `hsla(${index * (360 / numericColumns.length)}, 70%, 50%, 0.5)`,
    })),
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top' as const,
        tooltip: {
          callbacks: {
            title: (tooltipItems: any) => {
              const dataIndex = tooltipItems[0].dataIndex;
              return chartData.labels[dataIndex];
            },
            label: (context: any) => {
              const column = numericColumns[context.datasetIndex];
              return `${column.name}: ${context.raw} (${column.description})`;
            },
          },
        },
      },
    },
  };

  if (!activeChart) return null;

  return (
      <div className="h-[400px] w-full">
        {activeChart === 'line' && <Line data={chartData} options={options} />}
        {activeChart === 'bar' && <Bar data={chartData} options={options} />}
        {activeChart === 'pie' && <Pie data={chartData} options={options} />}
      </div>
  );
}
