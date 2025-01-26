import React, { useState } from 'react';
import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { DashboardWidget as DashboardWidgetType } from '../types';
import { ResultVisualizations } from './ResultVisualizations';
import { Edit2, Trash2, GripVertical, Lightbulb, Maximize2, Minimize2 } from 'lucide-react';

interface DashboardWidgetProps {
  widget: DashboardWidgetType;
  onUpdate: (widget: DashboardWidgetType) => void;
  onDelete: () => void;
  onResize: (size: { w: number; h: number }) => void;
}

export function DashboardWidget({ widget, onUpdate, onDelete, onResize }: DashboardWidgetProps) {
  const [isEditing, setIsEditing] = useState(false);
  const [showInsights, setShowInsights] = useState(false);
  const [showSizeControls, setShowSizeControls] = useState(false);

  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
  } = useSortable({ id: widget.id });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
  };

  const handleResize = (widthChange: number, heightChange: number) => {
    const currentSize = widget.size || { w: 4, h: 1 };
    const newSize = {
      w: Math.max(1, Math.min(12, currentSize.w + widthChange)),
      h: Math.max(1, currentSize.h + heightChange),
    };
    onResize(newSize);
  };

  return (
      <div
          ref={setNodeRef}
          style={style}
          className="relative h-full rounded-lg border border-gray-200 bg-white shadow-sm"
      >
        <div className="flex items-center justify-between border-b border-gray-200 p-4">
          <div className="flex items-center gap-2">
            <button {...attributes} {...listeners} className="cursor-grab">
              <GripVertical className="h-5 w-5 text-gray-400" />
            </button>
            {isEditing ? (
                <input
                    type="text"
                    value={widget.title}
                    onChange={(e) => onUpdate({ ...widget, title: e.target.value })}
                    className="rounded-md border border-gray-300 px-2 py-1"
                    onBlur={() => setIsEditing(false)}
                    autoFocus
                />
            ) : (
                <h3 className="font-medium text-gray-900">{widget.title}</h3>
            )}
          </div>
          <div className="flex items-center gap-2">
            {widget.insights && widget.insights.length > 0 && (
                <button
                    onClick={() => setShowInsights(!showInsights)}
                    className="rounded p-1 text-yellow-500 hover:bg-yellow-50"
                    title="Show insights"
                >
                  <Lightbulb className="h-5 w-5" />
                </button>
            )}
            <button
                onClick={() => setShowSizeControls(!showSizeControls)}
                className="rounded p-1 text-gray-500 hover:bg-gray-50"
                title="Resize widget"
            >
              {showSizeControls ? (
                  <Minimize2 className="h-5 w-5" />
              ) : (
                  <Maximize2 className="h-5 w-5" />
              )}
            </button>
            <button
                onClick={() => setIsEditing(true)}
                className="rounded p-1 text-gray-500 hover:bg-gray-50"
                title="Edit title"
            >
              <Edit2 className="h-5 w-5" />
            </button>
            <button
                onClick={onDelete}
                className="rounded p-1 text-red-500 hover:bg-red-50"
                title="Delete widget"
            >
              <Trash2 className="h-5 w-5" />
            </button>
          </div>
        </div>

        {showSizeControls && (
            <div className="absolute right-4 top-16 z-10 rounded-lg border border-gray-200 bg-white p-2 shadow-lg">
              <div className="space-y-2">
                <div className="flex items-center gap-2">
                  <span className="text-sm text-gray-600">Width:</span>
                  <button
                      onClick={() => handleResize(-1, 0)}
                      className="rounded px-2 py-1 text-sm hover:bg-gray-100"
                      disabled={widget.size?.w === 1}
                  >
                    -
                  </button>
                  <span className="text-sm">{widget.size?.w || 4}</span>
                  <button
                      onClick={() => handleResize(1, 0)}
                      className="rounded px-2 py-1 text-sm hover:bg-gray-100"
                      disabled={widget.size?.w === 12}
                  >
                    +
                  </button>
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-sm text-gray-600">Height:</span>
                  <button
                      onClick={() => handleResize(0, -1)}
                      className="rounded px-2 py-1 text-sm hover:bg-gray-100"
                      disabled={widget.size?.h === 1}
                  >
                    -
                  </button>
                  <span className="text-sm">{widget.size?.h || 1}</span>
                  <button
                      onClick={() => handleResize(0, 1)}
                      className="rounded px-2 py-1 text-sm hover:bg-gray-100"
                  >
                    +
                  </button>
                </div>
              </div>
            </div>
        )}

        {showInsights && widget.insights && (
            <div className="border-b border-gray-200 bg-yellow-50 p-4">
              <h4 className="mb-2 font-medium text-yellow-800">AI Insights</h4>
              <ul className="list-inside list-disc space-y-1 text-sm text-yellow-700">
                {widget.insights.map((insight, index) => (
                    <li key={index}>{insight}</li>
                ))}
              </ul>
            </div>
        )}

        <div className="h-[calc(100%-4rem)] overflow-auto p-4">
          {widget.type === 'chart' && widget.result && (
              <ResultVisualizations
                  result={widget.result}
                  activeChart={widget.visualization || 'line'}
              />
          )}
          {widget.type === 'table' && widget.result && (
              <div className="overflow-x-auto">
                <table className="w-full min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                  <tr>
                    {widget.result.columns.map((column, i) => (
                        <th
                            key={i}
                            className="group relative px-6 py-3 text-left"
                        >
                          <div className="flex items-center gap-2">
                        <span className="text-xs font-medium uppercase tracking-wider text-gray-500">
                          {column.name}
                        </span>
                            {column.description && (
                                <div className="relative">
                                  <div className="invisible absolute bottom-full left-1/2 mb-2 -translate-x-1/2 rounded-lg bg-gray-900 px-2 py-1 text-xs text-white opacity-0 transition-all group-hover:visible group-hover:opacity-100">
                                    <div className="font-medium">{column.dataType}</div>
                                    <div className="text-gray-300">{column.description}</div>
                                  </div>
                                </div>
                            )}
                          </div>
                        </th>
                    ))}
                  </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-200 bg-white">
                  {widget.result.data.map((row, i) => (
                      <tr key={i}>
                        {Object.values(row).map((cell: any, j) => (
                            <td
                                key={j}
                                className="whitespace-nowrap px-6 py-4 text-sm text-gray-900"
                            >
                              {cell?.toString() ?? ''}
                            </td>
                        ))}
                      </tr>
                  ))}
                  </tbody>
                </table>
              </div>
          )}
          {widget.type === 'kpi' && widget.result && (
              <div className="text-center">
                <div className="text-3xl font-bold text-gray-900">
                  {widget.result.data[0][widget.result.columns[0].name]}
                </div>
                <div className="mt-1 text-sm text-gray-500">
                  {widget.result.columns[0].description}
                </div>
              </div>
          )}
        </div>
      </div>
  );
}
